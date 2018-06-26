#!/usr/bin/env groovy

/**
 * Runs a bash script with the appropriate ecs_config env variables
 * loaded into the shell environment.
 *
 * Note: this will be faster if you ensure the deploymentWork directory is preserved between builds.
 */
def call(Map config) {

    sh "mkdir -p ${config.deploymentWork}"
    def projectName = config.projectName ?: repositoryName()
    String ecsConfigDir = "${config.deploymentWork}/ecs_config"
    withEnv([("ECS_CONFIG=${env.WORKSPACE}/${ecsConfigDir}"),
             "PROJECT_NAME=${projectName}",
             "ENVIRONMENT=${config.environment}"]) {

        dir(ecsConfigDir) {
            def ecsConfigBranch = config.ecsConfigBranch ?: 'master'
            git url: 'https://github.com/CruGlobal/ecs_config.git',
                branch: ecsConfigBranch,
                credentialsId: 'Cru-Jenkins-GitHub-User'
        }

        bash """\
            source $ECS_CONFIG/bin/load_environment.sh;
            load_environment;
            ${config.script}
            """.stripIndent()
    }
}
