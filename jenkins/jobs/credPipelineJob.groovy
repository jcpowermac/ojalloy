#!groovy

@Library('Utils')
import com.redhat.*

node {
    def id = null
    def utils = new Utils()

    /* The Jenkins root url is configured under
     * Manage Jenkins -> Configure System -> Jenkins Location
     * This is not configured properly in a ephemeral deployment.
     * Use the OpenShift route object to determine the correct url.
     */
    stage('Configure URL') {
        openshift.withCluster() {
            def route = openshift.selector('route', 'jenkins').object()
            utils.configureRootUrl("https://${route.spec.host}")
        }
    }
    stage('OpenShift -> Jenkins credentials') {
        openshift.withCluster() {
            def secret = openshift.selector( "secret/github" ).object()
            id = utils.createCredentialsFromOpenShift(secret, "github") 
        }
    }
    stage('Run Seed Job') {            
        build job: 'seed', parameters: [[$class: 'StringParameterValue', name: 'CRED_ID', value: id]]
    }
}
