package com.kustacks.kuring.worker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:constants.properties", ignoreResourceNotFound = true)
public class CustomConfig {
}
