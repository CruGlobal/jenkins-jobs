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
                def projectName = config.project ?: repositoryName()
                def ecsConfigBranch = config.ecsConfigBranch ?: 'master'
                performDeploy(projectName, ecsConfigBranch)
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
