package com.davidrt301.priority_tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class MonitoringPriorityTasksApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitoringPriorityTasksApplication.class, args);
	}

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}
