#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 */
def call(Map config, String buildStatus = 'STARTED') {
    // build status of null means successful
    buildStatus =  buildStatus ?: 'SUCCESSFUL'

    // Default values
    def color = 'RED'
    def buildPhrase = "${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def subject = "${buildStatus}: Job ${buildPhrase}"
    def summary = "${subject} (${env.BUILD_URL})"
    def details = """<p>${buildStatus}: Job ${buildPhrase}:</p>
    <p>Check console output at <a href='${env.BUILD_URL}'>${buildPhrase}</a></p>"""

    // Override default values based on build status
    if (buildStatus == 'STARTED') {
        color = 'YELLOW'
    } else if (buildStatus == 'SUCCESSFUL') {
        color = 'GREEN'
    }

    // Send notifications
//    hipchatSend (color: color, notify: true, message: summary)

    emailext (
            to: config.emailRecipients,
            mimeType: 'text/html',
            subject: subject,
            body: details,
            recipientProviders: [requestor(), developers()]
    )
}
