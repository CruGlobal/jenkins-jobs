#!/usr/bin/env groovy

/**
 * Wraps a block of code with exception-driven notification logic.
 * The given config is passed on to sendNotifications.
 */
def call(Map config = [:], closure) {
  try {
    closure()
  } catch (e) {
    // If there was an exception thrown, the build failed
    currentBuild.result = "FAILED"
    throw e
  } finally {
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
      if (config.notifyOnSuccess) {
        sendNotifications(config, currentBuild.result)
      }
    } else {
      sendNotifications(config, currentBuild.result)
    }
  }
}
