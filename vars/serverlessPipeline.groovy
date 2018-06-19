#!/usr/bin/env groovy

/**
 * Defines a jenkins pipeline for serverless projects
 */
def call(Map config) {

    node('linux') {
        checkout scm

        stage('Install') {
            sh "npm install"
        }

        if (environmentFromBranch() == 'production') {
            echo "production deployments not implemented yet"
        } else {
            stage('Deploy') {
                String projectName = config.project ?: repositoryName()
                performDeploy(projectName, config.ecsConfigBranch ?: 'master')
            }
        }
    }
}

private String repositoryName() {
    def url = sh(returnStdout: true, script: 'git config remote.origin.url').trim()
    return url.tokenize('/.')[-2]
}

private void performDeploy(String projectName, String ecsConfigBranch) {
    def environment = environmentFromBranch()
    String ecsConfigDir = 'deploy_ecs_config'
    withEnv([("ECS_CONFIG=${env.WORKSPACE}/${ecsConfigDir}"),
             "PROJECT_NAME=${projectName}",
             "ENVIRONMENT=${environment}"]) {

        dir(ecsConfigDir) {
            git url: 'https://github.com/CruGlobal/ecs_config.git',
                branch: ecsConfigBranch,
                credentialsId: 'Cru-Jenkins-GitHub-User'
        }

        sh """\
            #!/usr/bin/env bash
            set -e
            
            source $ECS_CONFIG/bin/load_environment.sh;
            load_environment;
            npx serverless deploy --stage ${environment} --verbose;
            """.stripIndent()
    }
}
