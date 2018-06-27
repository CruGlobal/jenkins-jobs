package org.cru.jenkins.lib

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import org.junit.Before
import org.junit.Test

class DeploymentsTest {

  private static final ZoneId EASTERN_TIME = ZoneId.of("America/New_York")
  public static final LocalDate THURSDAY = LocalDate.of(2018, Month.JUNE, 21)
  public static final LocalTime MID_MORNING = LocalTime.of(10, 30)

  Deployments deployments = new Deployments()

  @Before
  void setup() {
    deployments.getBinding().setVariable("echo", { string -> println string })
  }

  @Test
  void testAfterHoursConfirmationRequired_duringHours() {

    mockClock(THURSDAY, MID_MORNING)
    assertFalse(deployments.afterHoursConfirmationRequired())
  }

  @Test
  void testAfterHoursConfirmationRequired_duringWeekend() {

    LocalDate date = THURSDAY.plusDays(2)
    mockClock(date, MID_MORNING)
    assertTrue(deployments.afterHoursConfirmationRequired())
  }

  @Test
  void testAfterHoursConfirmationRequired_earlyMorning() {

    LocalTime time = MID_MORNING.minusHours(4)
    mockClock(THURSDAY, time)
    assertTrue(deployments.afterHoursConfirmationRequired())
  }

  @Test
  void testAfterHoursConfirmationRequired_lateAfternoon() {

    LocalTime time = MID_MORNING.plusHours(4)
    mockClock(THURSDAY, time)
    assertTrue(deployments.afterHoursConfirmationRequired())
  }

  private void mockClock(LocalDate date, LocalTime time) {
    Instant instant = LocalDateTime.of(date, time).atZone(EASTERN_TIME).toInstant()
    ClockHolder.clock = Clock.fixed(instant, EASTERN_TIME)
  }
}
