package dev.bnacar.distributedratelimiter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("docker")
public class ManagementCorsConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_ORIGINS = {
            "https://*.onrender.com",
            "https://*.render.com",
            "https://*.vercel.app",
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080"
    };

    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"};
    private static final String[] ALLOWED_HEADERS = {
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
            "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-Api-Key"
    };
    private static final String[] EXPOSED_HEADERS = {
            "X-Correlation-ID", "X-Trace-ID", "X-Span-ID",
            "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/actuator/**")
                .allowedOriginPatterns(ALLOWED_ORIGINS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .exposedHeaders(EXPOSED_HEADERS)
                .allowCredentials(true)
                .maxAge(3600);
    }
}