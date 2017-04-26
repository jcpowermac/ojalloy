#!groovy

def call(String imageStream) {
    stage('OpenShift Get ImageStream') {
        openshift.withCluster() {
            openshift.withProject() {
                try {
                    def is = openshift.selector("is/${imageStream}").object()
                    return is.status.dockerImageRepository
                }
                catch(all) {
                    error(all)
                }
            }
        }
    }
}
