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

        for (def f : dockerfiles) {
            newBuildOpenShift {
                url = pull.url
                branch = pull.ref
                contextDir = f.path.replace(f.name, "")
            }
        }
    } else {
        for (def f : dockerfiles) {
            newBuildOpenShift {
                url = scmUrl
                branch = scmBranch
                contextDir = f.path.replace(f.name, "")
            }
        }
    }
}



