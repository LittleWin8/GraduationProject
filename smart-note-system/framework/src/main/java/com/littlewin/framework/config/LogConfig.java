package com.littlewin.framework.config;

import com.littlewin.common.log.aspect.LogAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(LogAspect.class)
public class LogConfig {
}