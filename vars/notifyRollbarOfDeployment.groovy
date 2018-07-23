#!/usr/bin/env groovy

import org.cru.jenkins.lib.EnvironmentLoader

/**
 * Sends a deployment tracking notification to Rollbar.
 * It assumes this is a git project.
 */
def call(EnvironmentLoader loader) {

  Object localUsername = getUserId()
  def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

  script = """
    if [ -z "\${ROLLBAR_ACCESS_TOKEN}"];
    then
      echo "No ROLLBAR_ACCESS_TOKEN; skipping rollbar deployment notification"
      exit 0;
    fi

    curl https://api.rollbar.com/api/1/deploy/ \\
      --form access_token="\${ROLLBAR_ACCESS_TOKEN}" \\
      --form environment="\${ENVIRONMENT}" \\
      --form revision="${gitCommit}" \\
      --form local_username="${localUsername}" \\
      --form comment="${env.BUILD_URL}"
    """.stripIndent()

  loader.bash(script)
}

@NonCPS
private Object getUserId() {
  def build = currentBuild.rawBuild
  def userCause = build.getCause(hudson.model.Cause$UserIdCause)
  if (userCause != null) {
    return userCause.userId
  } else {
    def branchCause = build.getCause(jenkins.branch.BranchEventCause)
    return branchCause ? "github" : "N/A"
  }
}
