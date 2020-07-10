package com.example.demo.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController(
    private val mapper: ObjectMapper
) {
    @GetMapping(value = [""])
    fun get(@RequestParam requestParams: Map<String, String>): Any {
        return mapper.readTree(mapper.writeValueAsString(requestParams))
    }

    @PostMapping(value = [""])
    fun post(@RequestBody requestBody: Any): Any {
        return requestBody
    }
}
