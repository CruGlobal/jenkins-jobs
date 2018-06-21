package org.cru.jenkins.lib

import groovy.transform.Field

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

import static java.time.DayOfWeek.SATURDAY
import static java.time.DayOfWeek.SUNDAY



@Field
static final EnumSet<DayOfWeek> WEEKEND_DAYS = EnumSet.of(SATURDAY, SUNDAY)
@Field
static final LocalTime AUTODEPLOY_HOURS_BEGIN = LocalTime.of(7, 0)
@Field
static final LocalTime AUTODEPLOY_HOURS_END = LocalTime.of(13, 0)

/**
 * A workaround to make the clock replaceable in a unit test,
 * but also work correctly on Jenkins.
 */
class ClockHolder {
    static clock = Clock.system(ZoneId.of("America/New_York"))
}

/**
 * Returns true if this is not a reasonable time to automatically deploy to production.
 * Specifically, returns true if any of the following are true:
 *  - today is a weekend
 *  - it is before 7am (eastern time)
 *  - it is after 1pm (eastern time)
 */
boolean afterHoursConfirmationRequired() {

    ZonedDateTime now = ZonedDateTime.now(ClockHolder.clock)
    def timestampSentence = "It is currently ${now}."
    LocalDate today = now.toLocalDate()
    LocalTime currentTime = now.toLocalTime()
    def required = confirmationRequired(today, currentTime)
    maybeNot = required ? "" : "not "
    echo "${timestampSentence} After-hours confirmation is ${maybeNot}required"
    return required
}

private boolean confirmationRequired(LocalDate today, LocalTime currentTime) {
    return WEEKEND_DAYS.contains(today.dayOfWeek) ||
        currentTime.isBefore(AUTODEPLOY_HOURS_BEGIN) ||
        currentTime.isAfter(AUTODEPLOY_HOURS_END)
}

/**
 * Sends an email requesting a confirmation to deploy, since it is now after-hours.
 * It is sent to all of:
 *   - the project recipients
 *   - the developers of commits in this build
 *   - the one who initiated the build
 */
void sendConfirmationRequest() {
    def subject = "Deployment confirmation required: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = """
        <p>
        ${env.JOB_NAME} #${env.BUILD_NUMBER} was built,
        but was not automatically deployed since it is now after-hours.
        </p>
        <p>
        To confirm this deployment, click 'Proceed' here, within 15 minutes:
        <a href='${env.BUILD_URL}input/'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>
        </p>
        """.stripIndent()

    emailext (
        to: 'matt.drees+recipient@cru.org',
        mimeType: 'text/html',
        subject: subject,
        body: body,
        recipientProviders: [requestor(), developers()]
    )

}
