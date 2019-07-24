#!/usr/bin/env groovy
import org.cru.jenkins.lib.EnvironmentLoader


/**
 * Defines a jenkins pipeline for serverless projects.
 *
 * Config options:
 *  assumeRole - ARN of the role to assume during deployment. Leave empty to default to the
 *      jenkins EC2 role.
 *  confirmAllBranches - if all branches should be subject to deployment confirmation prompts,
 *      and not just production; defaults to false
 *  defaultEnvironment - the environment to use if the branch is neither 'master' nor 'staging';
 *      defaults to 'staging'
 *  ecsConfigBranch - the ecs_config branch to use; defaults to master
 *  emailRecipients - comma-separated list of email addresses that should be notified,
 *      in addition to the build initiator and the developers that contributed to changes in the build;
 *      defaults to an empty list.
 *  notifyOnSuccess - if job notifications should be sent on successful builds; defaults to false
 *  packageManager - Package Manager to use during Install stage: yarn, npm (default)
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
        switch(config.packageManager) {
          case 'yarn':
            // Use of npx here will install yarn if not present
            sh "npx yarn install"
            break
          case 'npm':
          default:
            sh "npm install"
        }
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
  def script = ''

  if(config.assumeRole) {
    // Custom aws cli to assume role because aws-js-sdk doesn't support credential_source config directive
    // See https://github.com/serverless/serverless/issues/3833#issuecomment-471318601
    script = """\
      # Use custom credentials file
      export AWS_SHARED_CREDENTIALS_FILE=\"\$(pwd)/.credentials\";

      # Assume deployment role manually
      assumed_role_creds=\$(aws sts assume-role --role-arn $config.assumeRole \\
        --role-session-name serverless --output json | \\
        jq -r '.Credentials.AccessKeyId, .Credentials.SecretAccessKey, .Credentials.SessionToken');
      IFS=\" \" readarray -t parts <<< \"\$assumed_role_creds\";

      # Populate credentials file with access keys and session token
      aws configure set aws_access_key_id \"\${parts[0]}\" --profile serverless;
      aws configure set aws_secret_access_key \"\${parts[1]}\" --profile serverless;
      aws configure set aws_session_token \"\${parts[2]}\" --profile serverless;

      # Define profile (by setting at least region) in ~/.aws/config otherwise serverless won't find it
      export AWS_SDK_LOAD_CONFIG=1;
      aws configure set region us-east-1 --profile serverless;
      SLS_DEBUG=* npx serverless deploy --stage ${environment} --verbose --aws-profile serverless;
    """.stripIndent()
  } else {
    script = "SLS_DEBUG=* npx serverless deploy --stage ${environment} --verbose;"
  }
  loader.bash script

  notifyRollbarOfDeployment(loader)
}
