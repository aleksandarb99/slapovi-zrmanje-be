package com.slapovizrmanje.api.controller;

import com.slapovizrmanje.api.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification")
public class VerificationController {

    private final VerificationService verificationService;
    @GetMapping("/verify")
    public void verify(@RequestParam String email, @RequestParam String id) {
        log.info("VERIFICATION SERVICE - Verify.");
        verificationService.verify(email, id);
    }
}
