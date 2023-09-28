package com.kgignatyev.temporal.visualwf.demo.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class DemoApiController {

    @GetMapping("/")
    fun index(): String {
        return "Greetings from Visual Workflow Demo!"
    }
}
