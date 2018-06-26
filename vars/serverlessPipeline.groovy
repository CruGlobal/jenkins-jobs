#!/usr/bin/env groovy


/**
 * Defines a jenkins pipeline for serverless projects
 */
def call(Map config) {

    node('linux') {
        withNotifications(config) {
            checkout scm

            cleanWorkingTree(except: '.deployment')
            testBuildFailureNotificationsIfConfigured(config)

            stage('Install') {
                sh "npm install"
            }

            confirmDeploymentIfNecessary(config)

            stage('Deploy') {
                performDeploy(config)
            }
        }
    }
}

private void performDeploy(config) {
    def environment = environmentFromBranch()

    bashWithLoadedEnvironment(
        projectName: config.project,
        environment: environment,
        ecsConfigBranch: config.ecsConfigBranch,
        deploymentWork: '.deployment',
        script: "SLS_DEBUG=* npx serverless deploy --stage ${environment} --verbose;"
    )
}
