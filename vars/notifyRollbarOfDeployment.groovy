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
        curl https://api.rollbar.com/api/1/deploy/ \\
          --form access_token="\${ROLLBAR_ACCESS_TOKEN}" \\
          --form environment="\${ENVIRONMENT}" \\
          --form revision="${gitCommit}" \\
          --form local_username="${localUsername}"
        """.stripIndent()

    loader.bash(script)
}

@NonCPS
private Object getUserId() {
    def userCause = currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause)
    def localUsername = userCause ? userCause.userId : "N/A"
    localUsername
}
