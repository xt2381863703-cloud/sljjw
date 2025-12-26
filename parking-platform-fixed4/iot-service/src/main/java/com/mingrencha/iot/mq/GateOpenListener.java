package com.mingrencha.iot.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class GateOpenListener {

  @RabbitListener(queues = "iot.gate.open.q")
  public void onGateOpen(Map<String,Object> evt){
    log.info("[IOT] Gate open command received: {}", evt);
  }
}
