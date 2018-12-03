package com.lhiot.healthygood.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(HealthyGoodConfig.class)
public class HealthyGoodServerAutoConfiguration {

}
