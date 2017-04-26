#!groovy
@Library('Utils')
import com.redhat.*

properties([disableConcurrentBuilds()])

node {
    def source = ""
    def dockerfiles = null
    def utils = new com.redhat.Utils()
    String scmRef = scm.branches[0]
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
     * Otherwise just use the scm.browser.url and scm.branches[0]
     * for new-build.
     */
    if (env.CHANGE_URL) {
        def pull = null
        stage('Github Url and Ref') {

            // Query the github repo api to return the clone_url and the ref (branch name)
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "github", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                pull = utils.getGitHubPR(env.USERNAME, env.PASSWORD, env.CHANGE_URL)
                scmUrl = pull.url
                scmRef = pull.ref
                deleteBuild = true
            }
        }
    }
    for (int i = 0; i < dockerfiles.size(); i++) {
        /* Execute oc new-build on each dockerfile available
         * in the repo.  The context-dir is the path removing the
         * name (i.e. Dockerfile)
         */
        String path = dockerfiles[i].path.replace(dockerfiles[i].name, "")
        def newBuild = newBuildOpenShift {
            url = scmUrl
            branch = scmRef
            contextDir = path
            deleteBuild = false
        }
        /* The name of the ImageStream should be the same as the branch
         * name.
         */
        String isRepo = getImageStreamRepo(scmRef)

        runOpenShift {
            deletePod = true
            branch = scmRef
            image = isRepo
            env = ["foo=goo"]
        }

        newBuild.delete()

    }
}

// vim: ft=groovy
