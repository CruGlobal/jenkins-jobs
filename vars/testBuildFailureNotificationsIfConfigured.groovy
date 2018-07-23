#!/usr/bin/env groovy

/**
 * Fails the build if the config option testBuildFailureNotifications is truthy.
 */
void call(Map config) {
  if (config.testBuildFailureNotifications) {
    error("Fake failure in order to test notifications")
  }
}
