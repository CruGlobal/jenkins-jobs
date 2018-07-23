#!/usr/bin/env groovy

/**
 * Determines the environment name based on the branch name
 */
String call(Map config = [:]) {
  def branchName = env.BRANCH_NAME
  if (branchName == 'master') {
    return 'production'
  } else if (branchName == 'staging') {
    return 'staging'
  } else { // for when we're developing/testing the deployment mechanism on a topic branch
    return config.defaultEnvironment ?: 'staging'
  }
}
