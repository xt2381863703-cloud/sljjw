package com.mingrencha.reservation.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  @Bean
  public TopicExchange parkingEventsExchange() {
    return new TopicExchange("parking.events", true, false);
  }
}
