package com.governance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/policies")
    public ResponseEntity<Map<String, Object>> fallbackPolicies() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FALLBACK");
        response.put("message", "Policy service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> fallbackAudit() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FALLBACK");
        response.put("message", "Audit service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}