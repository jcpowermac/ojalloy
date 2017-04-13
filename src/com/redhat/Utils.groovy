#!groovy
package com.redhat

import com.cloudbees.groovy.cps.NonCPS
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*

import jenkins.model.JenkinsLocationConfiguration


@NonCPS
Boolean configureRootUrl(String url) {
    try {
        JenkinsLocationConfiguration jlc = JenkinsLocationConfiguration.get()
        jlc.setUrl(url)
        jlc.save()
        return true
    }
    catch (error) {
        println(error.toString())
        return false
    }
}

@NonCPS 
String createCredentialsFromOpenShift(HashMap secret, String id) {
    String username = new String(secret.data.username.decodeBase64())
    String password = new String(secret.data.password.decodeBase64())

    return createCredentials(id, username, password, "secret from openshift")
}


@NonCPS
String createCredentials(String id = null, String username, String password, String description) {

    if( id == null ) {
        id = java.util.UUID.randomUUID().toString()
    }


    Credentials c = (Credentials) new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, description, username, password)

    SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), c)

    return id
}
