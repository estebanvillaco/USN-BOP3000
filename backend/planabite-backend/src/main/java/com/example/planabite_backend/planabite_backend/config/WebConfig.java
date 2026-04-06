package com.example.planabite_backend.planabite_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
<<<<<<< HEAD
                .allowedOrigins("http://localhost:5173")
=======
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
>>>>>>> f19a9760d6658d1b444331042040346fb576e4b2
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
