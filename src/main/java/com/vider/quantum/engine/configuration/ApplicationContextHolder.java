package com.vider.quantum.engine.configuration;

import lombok.Getter;
import org.springframework.context.ApplicationContext;

public final class ApplicationContextHolder {

    @Getter
    private static ApplicationContext applicationContext;

    private ApplicationContextHolder() {

    }

    public static void setApplicationContext(ApplicationContext ac) {
        applicationContext = ac;
    }

}
