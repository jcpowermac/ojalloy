#!groovy

// vim: ft=groovy

@Library('Utils')

import com.redhat.*

properties([disableConcurrentBuilds()])

node {
    def source = ""

    String scmBranch = scm.branches[0]
    String scmUrl = scm.browser.url

    /* if CHANGE_URL is defined then this is a pull request
     * additional steps are required to determine the git url 
     * and branch name to pass to new-build.
     *
     * Otherwise just use the scm.browser.url and scm.branches[0] 
     * for new-build.
     */

    def utils = new com.redhat.Utils()

    if (env.CHANGE_URL) {

        def pull = null

        stage('echo') {
            echo sh(returnStdout: true, script: 'env')
        }

        stage('Github Url and Ref') {
            def changeUrl = env.CHANGE_URL

            // Query the github repo api to return the clone_url and the ref (branch name)
            //def githubUri = changeUrl.replaceAll("github.com/", "api.github.com/repos/")
            //githubUri = githubUri.replaceAll("pull", "pulls")

            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "github", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                pull = utils.getGitHubPR(env.USERNAME, env.PASSWORD, changeUrl)
                //println(map.toString())
                //sh("curl -u ${env.USERNAME}:${env.PASSWORD} -o ${env.WORKSPACE}/github.json ${githubUri}")
            }
            //pull = readJSON file: 'github.json'

            //if (pull.head.repo == null) {
            //    error("Unable to read GitHub JSON file")
            //}
        }

        newBuildOpenShift {
            url = pull.url
            branch = pull.ref
        }

    }
    else {
        newBuildOpenShift{
            url = scmUrl
            branch = scmBranch
        }
    }
}



