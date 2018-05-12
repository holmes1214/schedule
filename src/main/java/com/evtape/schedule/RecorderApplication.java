package com.evtape.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {"com.evtape"})
@EnableTransactionManagement
@EnableScheduling
public class RecorderApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(RecorderApplication.class);
		ConfigurableApplicationContext context = application.run(RecorderApplication.class, args);
	}
}
