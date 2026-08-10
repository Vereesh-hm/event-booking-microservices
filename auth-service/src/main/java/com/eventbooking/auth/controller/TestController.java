package com.eventbooking.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("JWT authentication successful");
    }
    
    @GetMapping("/user")
    public ResponseEntity<String> userEndpoint() {
        return ResponseEntity.ok("USER endpoint accessed");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint() {
    	System.out.println("ADMIN ENDPOINT REACHED");
        return ResponseEntity.ok("ADMIN endpoint accessed");
    }
}