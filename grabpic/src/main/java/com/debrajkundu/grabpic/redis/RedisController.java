package com.debrajkundu.grabpic.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/flush")
    public ResponseEntity<Void> flushAllKeys() {
        redisService.flushAllKeys();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getAllKeys() {
        return ResponseEntity.ok(redisService.getAllKeys());
    }
}
