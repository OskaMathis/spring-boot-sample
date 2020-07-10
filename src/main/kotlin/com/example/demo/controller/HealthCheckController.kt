package com.example.demo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {
    @GetMapping(value = ["/health_check"])
    fun healthCheck(): Any {
        return object {}
    }
}
