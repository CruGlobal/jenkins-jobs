#!/usr/bin/env groovy

import org.cru.jenkins.lib.Deployments


/**
 * Defines a jenkins pipeline for serverless projects
 */
def call(Map config) {

    node('linux') {
        withNotifications(config) {
            checkout scm

            Deployments deployments = new Deployments()
            deployments.cleanWorkingTree(except: '.deployment')

            stage('Install') {
                sh "npm install"
            }

            confirmDeploymentIfNecessary(config)
            stage('Deploy') {
                def projectName = config.project ?: deployments.repositoryName()
                def ecsConfigBranch = config.ecsConfigBranch ?: 'master'
                performDeploy(projectName, ecsConfigBranch)
            }
        }
    }
}

private void confirmDeploymentIfNecessary(config) {
    def environment = environmentFromBranch()
    if (environment == 'production' || config.confirmAllBranches) {
        Deployments deployments = new Deployments()
        stage('Confirm Deployment') {
            if (deployments.afterHoursConfirmationRequired()) {
                deployments.sendConfirmationRequest(config)
                timeout(time: 15, unit: 'MINUTES') {
                    input "OK to deploy to ${environment}, after hours?"
                }
            }
        }
    }
}

private void performDeploy(String projectName, String ecsConfigBranch) {
    def environment = environmentFromBranch()
    def deploymentWork = '.deployment'
    sh "mkdir -p ${deploymentWork}"
    String ecsConfigDir = "${deploymentWork}/ecs_config"
    withEnv([("ECS_CONFIG=${env.WORKSPACE}/${ecsConfigDir}"),
             "PROJECT_NAME=${projectName}",
             "ENVIRONMENT=${environment}"]) {

        dir(ecsConfigDir) {
            git url: 'https://github.com/CruGlobal/ecs_config.git',
                branch: ecsConfigBranch,
                credentialsId: 'Cru-Jenkins-GitHub-User'
        }

        bash """\
            source $ECS_CONFIG/bin/load_environment.sh;
            load_environment;
            SLS_DEBUG=* npx serverless deploy --stage ${environment} --verbose;
            """.stripIndent()
    }
}
