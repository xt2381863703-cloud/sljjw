package com.mingrencha.reservation.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLockService {
  private final StringRedisTemplate redis;

  public String tryLock(String key, Duration ttl){
    String token = UUID.randomUUID().toString();
    Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttl);
    return Boolean.TRUE.equals(ok) ? token : null;
  }

  public void unlock(String key, String token){
    // simple compare-and-delete Lua
    String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    org.springframework.data.redis.core.script.DefaultRedisScript<Long> script = new org.springframework.data.redis.core.script.DefaultRedisScript<>();
    script.setResultType(Long.class);
    script.setScriptText(lua);
    redis.execute(script, java.util.List.of(key), token);
}
}
