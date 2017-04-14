#!groovy

// vim: ft=groovy

@Library('Utils')

import com.redhat.*

properties([disableConcurrentBuilds()])

node {
    def source = ""
    def dockerfiles = null
    def utils = new com.redhat.Utils()
    String scmBranch = scm.branches[0]
    String scmUrl = scm.browser.url


    /* Checkout source and find all the Dockerfiles.
     * This will not include Dockerfiles with extensions. Currently the issue
     * with using a Dockerfile with an extension is the oc new-build command
     * does not offer an option to provide the dockerfilePath.
     */
    stage('checkout') {
        checkout scm
        dockerfiles = findFiles(glob: '**/Dockerfile')
    }
    /* if CHANGE_URL is defined then this is a pull request
     * additional steps are required to determine the git url
     * and branch name to pass to new-build.
     *
     * Otherwise just use the scm.browser.url and scm.branches[0]
     * for new-build.
     */
    if (env.CHANGE_URL) {
        def pull = null

        stage('Github Url and Ref') {

            // Query the github repo api to return the clone_url and the ref (branch name)
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "github", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                pull = utils.getGitHubPR(env.USERNAME, env.PASSWORD, env.CHANGE_URL)
            }
        }

        for (int i = 0; i < dockerfiles.size; i++) {
            newBuildOpenShift {
                url = pull.url
                branch = pull.ref
                contextDir = dockerfiles[i].path.replace(dockerfiles[i].name, "")
            }
        }
    } else {
        for (int i = 0; i < dockerfiles.size; i++) {
            newBuildOpenShift {
                url = scmUrl
                branch = scmBranch
                contextDir = dockerfiles[i].path.replace(dockerfiles[i].name, "")
            }
        }
    }
}



