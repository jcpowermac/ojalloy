#!groovy

def call(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def newBuild = null
    def contextDir = config['contextDir'] ?: ""

    stage('OpenShift Build') {
        openshift.withCluster() {
            openshift.withProject() {
                try {
                    def builds = null

                    /* If the OpenShift oc new-build command is ran in succession it can cause a
                     * race if the base imagestream does not already exist.  In normal situations
                     * this is not a problem but in Jenkins when multiple jobs could be occurring
                     * simultaneously this will happen.  Adding a lock in this section resolves
                     * that issue.
                     */

                    lock(resource: 'openshift.newBuild', inversePrecedence: true) {
                        /* Use oc new-build to build the image using the clone_url and ref
                         * TODO: Determine a method to new-build with a "Dockerfile" with a
                         * TODO: different filename e.g. Dockerfile.rhel7.
                         */

                        newBuild = openshift.newBuild("${config.url}#${config.branch}",
                                "--name=${config.branch}",
                                "--context-dir=${contextDir}")
                        echo "newBuild created: ${newBuild.count()} objects : ${newBuild.names()}"
                        builds = newBuild.narrow("bc").related("builds")
                        timeout(5) {
                            builds.watch {
                                if (it.count() == 0) {
                                    return false
                                }
                                echo "Detected new builds created by buildconfig: ${it.names()}"
                                return true
                            }
                        }
                    }

                    timeout(10) {
                        builds.untilEach(1) {
                            return it.object().status.phase == "Complete"
                        }
                    }
                }
                finally {
                    if (newBuild) {
                        def result = newBuild.narrow("bc").logs()
                        echo "status: ${result.status}"
                        echo "${result.actions[0].cmd}"

                        if (result.status != 0) {
                            echo "${result.out}"
                            error("Image Build Failed")
                        }
                        // After built we do not need the BuildConfig or the ImageStream
                        newBuild.delete()
                    }
                }
            }
        }
    }
}
