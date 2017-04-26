#!groovy

def call(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def pod = null
    def podObject = null
    def deletePod = config['deletePod'] ?: false
    String env = ""

    if(config['env']) {
        config.env.each {
            env += "--env=\'${it}\' "
        }
    }

    stage('OpenShift Run') {
        openshift.withCluster() {
            openshift.withProject() {
                try {
                    openshift.run("${config.branch}", "--image=${config.image}", "--restart=Never",
                            "--image-pull-policy=Always", "${env}")
                    pod = openshift.selector("pod/${config.branch}")

                    timeout(10) {
                        pod.watch {
                            podObject = it.object()
                            if (podObject.status.phase == 'Succeeded' || podObject.status.phase == 'Failed') {
                                return true
                            } else {
                                return false
                            }
                        }
                    }
                }
                finally {
                    if (pod) {
                        def result = pod.logs()
                        println(result)
                        echo "status: ${result.status}"
                        echo "${result.actions[0].cmd}"

                        if (result.status != 0) {
                            echo "${result.out}"
                            error("Pod failed")
                        }
                        if (deletePod) {
                            pod.delete()
                        }
                    }
                }
            }
        }
    }
}
