package com.todo.management;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
public class TestClockConfig {

  @Bean
  public MutableClock mutableClock() {
    return new MutableClock(Instant.parse("2026-03-03T10:00:00Z"), ZoneOffset.UTC);
  }

  @Bean
  @Primary
  public Clock clock(MutableClock mutableClock) {
    return mutableClock;
  }
}
