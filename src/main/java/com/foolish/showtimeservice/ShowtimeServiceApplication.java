package com.foolish.showtimeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.foolish.showtimeservice.repository")
@EntityScan("com.foolish.showtimeservice.model")
@EnableDiscoveryClient
public class ShowtimeServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ShowtimeServiceApplication.class, args);
  }

}
