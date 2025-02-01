package com.vider.quantum.engine.sample.controller;


import com.vider.quantum.engine.sample.service.DelegateSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/delegate-sample")
@RequiredArgsConstructor
public class DelegateSampleController {

    private final DelegateSampleService delegateSampleService;

    @GetMapping("{name}")
    public String delegateSample(@PathVariable("name") String name) {
        return delegateSampleService.getResult(name);
    }

}
