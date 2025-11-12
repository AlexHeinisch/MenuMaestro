package dev.heinisch.menumaestro.configuration;

import dev.heinisch.menumaestro.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow cross-origin requests from localhost:4200
        registry.addMapping("/**")  // Apply to all endpoints
                .allowedOriginPatterns(corsProperties.getAllowedCrossOriginPatterns().toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")  // Specify allowed HTTP methods
                .allowedHeaders("*")
                .exposedHeaders("*");  // Allow all headers

    }
}
