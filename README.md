![Build Status](https://jenkins.apps.virtomation.com/buildStatus/icon?job=jcpowermac/ojalloy/master)

### ojalloy - OpenShift Jenkins better together

Current Goal: Build and test container images on OpenShift with Jenkins and supporting
multibranches (pull requests).

1. Uses ephermeral Jenkins and the configuration is stored in OpenShift project
2. S2I to include jobs and additional plugins

#### Quickstart

1. Add Jenkinsfile to your GitHub project (use the example in this project).
2. Create a new project `oc new-project <project-name>`
3. Add the template to OpenShift `oc create -f https://raw.githubusercontent.com/jcpowermac/ojalloy/master/jenkins/openshift/template.yaml`
4. Process template
  ```
  oc process docker-image-testing \
  JENKINS_ORG_FOLDER_NAME=jcowermac \
  JENKINS_GITHUB_OWNER=jcpowermac \
  JENKINS_GITHUB_REPO=ojalloy \
  JENKINS_GITHUB_CRED_ID=github \
  GITHUB_USERNAME=jcpowermac \
  GITHUB_TOKEN=token | oc create -f -
  ```
5. Add Jenkins to the project `oc process openshift//jenkins-ephemeral NAMESPACE=<project-name> MEMORY_LIMIT=2Gi | oc create -f -`
6. Add the pipeline `oc create -f https://raw.githubusercontent.com/jcpowermac/ojalloy/master/jenkins/openshift/pipeline.yaml`
7. And finally start the pipeline `oc start-build createcred-pipeline`


#### Note
This project is being tested by the `Jenkinsfile` in my OpenShift environment.
The SSL certificates are from Let's Encrypt using the [openshift-acme project](https://github.com/tnozicka/openshift-acme) to generate them.
