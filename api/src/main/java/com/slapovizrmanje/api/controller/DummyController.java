package com.slapovizrmanje.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DummyController {

    @GetMapping("/helloWorld")
    public String getHelloWorld() {
        return "Hello World";
    }
}
