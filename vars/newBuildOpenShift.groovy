#!groovy

def call(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def newBuild = null

    stage('OpenShift Build') {
        openshift.withCluster() {
            openshift.withProject() {
                try {
                    def builds = null
                    // use oc new-build to build the image using the clone_url and ref
                    lock(resource: 'openshift.newBuild', inversePrecedence: true) {
                        newBuild = openshift.newBuild("${config.url}#${config.branch}", "--name=${config.branch}")
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
