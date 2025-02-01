package com.vider.quantum.engine.sample.controller;

import com.vider.quantum.engine.sample.service.SpinSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/spin-sample")
@RequiredArgsConstructor
public class SpinSampleController {

    private final SpinSampleService spinSampleService;

    @GetMapping("{currency}")
    public String delegateSample(@PathVariable("currency") String currency) {
        return spinSampleService.getResult(currency);
    }

}
