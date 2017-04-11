#!groovy

def call(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()


    def newBuild = null
    def url = null
    def branch = null
    if(!config.containsKey('url') ) {
        url = scm.browser.url
    }
    if(!config.containsKey('branch')) {
        branch = scm.branches[0]
    }

    openshift.withCluster() {
        openshift.withProject() {
            try {
                // use oc new-build to build the image using the clone_url and ref
                newBuild = openshift.newBuild("${config.cloneUrl}#${config.branch}")
                echo "newBuild created: ${newBuild.count()} objects : ${newBuild.names()}"
                def builds = newBuild.narrow("bc").related("builds")

                timeout(10) {
                    builds.watch {
                        if (it.count() == 0) {
                            return false
                        }
                        echo "Detected new builds created by buildconfig: ${it.names()}"
                        return true
                    }
                    builds.untilEach(1) {
                        return it.object().status.phase == "Complete"
                    }
                }
            }
            finally {
                if (newBuild) {
                    def result = newBuild.narrow("bc").logs()
                    echo "Result of logs operation:"
                    echo "  status: ${result.status}"
                    echo "  stderr: ${result.err}"
                    echo "  number of actions to fulfill: ${result.actions.size()}"
                    echo "  first action executed: ${result.actions[0].cmd}"

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
