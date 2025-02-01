package com.vider.quantum.engine;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableProcessApplication
@EnableScheduling
public class Application {

  public static void main(String... args) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    SpringApplication.run(Application.class, args);
  }

}