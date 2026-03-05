package com.todo.management;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class MutableClock extends Clock {
  private Instant instant;
  private final ZoneId zone;

  public MutableClock(Instant instant, ZoneId zone) {
    this.instant = instant;
    this.zone = zone;
  }

  public void setInstant(Instant instant) {
    this.instant = instant;
  }

  public void advanceSeconds(long seconds) {
    this.instant = this.instant.plusSeconds(seconds);
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return new MutableClock(this.instant, zone);
  }

  @Override
  public Instant instant() {
    return instant;
  }
}
