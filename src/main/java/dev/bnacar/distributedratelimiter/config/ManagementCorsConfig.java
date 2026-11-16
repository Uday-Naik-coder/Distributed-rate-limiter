package dev.bnacar.distributedratelimiter.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("docker")
public class ManagementCorsConfig {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "https://*.onrender.com",
            "https://*.render.com",
            "https://*.vercel.app",
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080"
    );

    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
            "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-Api-Key"
    );
    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
            "X-Correlation-ID", "X-Trace-ID", "X-Span-ID",
            "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset"
    );

    @Bean
    public FilterRegistrationBean<CorsFilter> managementCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(ALLOWED_ORIGINS);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setExposedHeaders(EXPOSED_HEADERS);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/actuator/**", config);

        CorsFilter filter = new CorsFilter(source);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.setUrlPatterns(Arrays.asList("/actuator/*"));
        return bean;
    }
}