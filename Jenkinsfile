#!groovy

// vim: ft=groovy

@Library('Utils')

import com.redhat.*

properties([disableConcurrentBuilds()])

node {
    def source = ""

    //echo sh(returnStdout: true, script: 'env')
    println(scm.dump())
    println(scm.branches[0])
    println(scm.browser.url)
    println(this.dump())

    if (env.CHANGE_URL) {
        println(env.CHANGE_URL)

        def newBuild = null
        def changeUrl = env.CHANGE_URL

        // Query the github repo api to return the clone_url and the ref (branch name)
        def githubUri = changeUrl.replaceAll("github.com/", "api.github.com/repos/")
        githubUri = githubUri.replaceAll("pull", "pulls")
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "github" , usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh("curl -u ${env.USERNAME}:${env.PASSWORD} -o ${env.WORKSPACE}/github.json ${githubUri}")
        }
        def pull = readJSON file: 'github.json'

        if (pull.head.repo == null) {
            error("Unable to read GitHub JSON file")
        }

        openshiftNewBuild {
            cloneUrl = pull.head.repo.clone_url
            branch = pull.head.ref
        }

    }
    else {
        openshiftNewBuild {
            cloneUrl = scm.browser.url
            branch = scm.branches[0]
        }
    }
}



