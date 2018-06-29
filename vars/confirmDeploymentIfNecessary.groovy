#!/usr/bin/env groovy
import org.cru.jenkins.lib.Deployments

/**
 * Adds a 'Confirm Deployment' stage, if necessary.
 * The stage is added if this is a production branch, or if the confirmAllBranches option is true.
 */
void call(Map config) {
  def environment = environmentFromBranch(config)
  if (environment == 'production' || config.confirmAllBranches) {
    Deployments deployments = new Deployments()
    stage('Confirm Deployment') {
      if (deployments.afterHoursConfirmationRequired()) {
        deployments.sendConfirmationRequest(config)
        timeout(time: 15, unit: 'MINUTES') {
          input "OK to deploy to ${environment}, after hours?"
        }
      }
    }
  }
}
