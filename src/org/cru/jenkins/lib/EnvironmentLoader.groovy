package org.cru.jenkins.lib

/**
 * A tool for running one or more bash scripts with the appropriate ecs_config env variables
 * loaded into the shell environment.
 *
 * Note: this will be faster if you ensure the deploymentWork directory is preserved between builds.
 */
class EnvironmentLoader implements Serializable {

  private final Object steps
  private final Map config
  private boolean checkedOut

  EnvironmentLoader(Map config, steps) {
    this.steps = steps
    this.config = config
  }

  void bash(String script) {
    steps.sh "mkdir -p ${config.deploymentWork}"

    def projectName = config.projectName ?: steps.repositoryName()
    String ecsConfigDir = "${config.deploymentWork}/ecs_config"
    steps.withEnv([
      "ECS_CONFIG=${steps.env.WORKSPACE}/${ecsConfigDir}",
      "PROJECT_NAME=${projectName}",
      "ENVIRONMENT=${config.environment}"
    ]) {

      checkoutIfNecessary(ecsConfigDir)

      steps.bash """\
        source \${ECS_CONFIG}/bin/load_environment.sh;
        load_environment;
        ${script}
        """.stripIndent()
    }

  }

  private void checkoutIfNecessary(ecsConfigDir) {
    if (checkedOut) {
      return
    }
    steps.dir(ecsConfigDir) {
      def ecsConfigBranch = config.ecsConfigBranch ?: 'master'
      steps.git url: 'https://github.com/CruGlobal/ecs_config.git',
        branch: ecsConfigBranch,
        credentialsId: 'Cru-Jenkins-GitHub-User'
    }
    checkedOut = true
  }
}
