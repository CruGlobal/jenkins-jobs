#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 */
def call(String buildStatus = 'STARTED') {
    // build status of null means successful
    buildStatus =  buildStatus ?: 'SUCCESSFUL'

    // Default values
    def color = 'RED'
    def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
    def summary = "${subject} (${env.BUILD_URL})"
    def details = """<p>${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

    // Override default values based on build status
    if (buildStatus == 'STARTED') {
        color = 'YELLOW'
    } else if (buildStatus == 'SUCCESSFUL') {
        color = 'GREEN'
    }

    // Send notifications
    hipchatSend (color: color, notify: true, message: summary)

    emailext (
            to: 'bitwiseman@bitwiseman.com',
            subject: subject,
            body: details,
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}
