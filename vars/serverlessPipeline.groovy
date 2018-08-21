#!/usr/bin/env groovy
import org.cru.jenkins.lib.EnvironmentLoader


/**
 * Defines a jenkins pipeline for serverless projects.
 *
 * Config options:
 *  confirmAllBranches - if all branches should be subject to deployment confirmation prompts,
 *      and not just production; defaults to false
 *  defaultEnvironment - the environment to use if the branch is neither 'master' nor 'staging';
 *      defaults to 'staging'
 *  ecsConfigBranch - the ecs_config branch to use; defaults to master
 *  emailRecipients - comma-separated list of email addresses that should be notified,
 *      in addition to the build initiator and the developers that contributed to changes in the build;
 *      defaults to an empty list.
 *  hipchatRoom - the hipchat room notifications should be sent to, in addition to emails;
 *      default is no hipchat notifications.
 *  notifyOnSuccess - if job notifications should be sent on successful builds; defaults to false
 *  project - the ecs_config PROJECT_NAME; defaults to the git repo name
 *  testBuildFailureNotifications - if the build should immediately fail with a fake failure,
 *      to test notifications; defaults to false
 */
def call(Map config = [:]) {

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
  def environment = environmentFromBranch(config)

  def loader = new EnvironmentLoader(
    projectName: config.project,
    environment: environment,
    ecsConfigBranch: config.ecsConfigBranch,
    deploymentWork: '.deployment',
    this
  )

  loader.bash "SLS_DEBUG=* npx serverless deploy --stage ${environment} --verbose;"

  notifyRollbarOfDeployment(loader)
}
