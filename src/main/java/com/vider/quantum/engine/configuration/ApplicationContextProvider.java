package com.vider.quantum.engine.configuration;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.setApplicationContext(applicationContext);
    }

    public static ApplicationContext getApplicationContext() {
        return ApplicationContextHolder.getApplicationContext();
    }
}
