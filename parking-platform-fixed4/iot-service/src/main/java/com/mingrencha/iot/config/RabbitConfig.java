package com.mingrencha.iot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  @Bean
  public TopicExchange parkingEventsExchange() {
    return new TopicExchange("parking.events", true, false);
  }

  @Bean
  public Queue gateQueue() {
    return QueueBuilder.durable("iot.gate.open.q").build();
  }

  @Bean
  public Binding gateBinding(Queue gateQueue, TopicExchange parkingEventsExchange) {
    return BindingBuilder.bind(gateQueue).to(parkingEventsExchange).with("gate.open");
  }
}
