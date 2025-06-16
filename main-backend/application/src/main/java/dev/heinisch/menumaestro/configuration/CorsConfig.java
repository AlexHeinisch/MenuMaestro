package dev.heinisch.menumaestro.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow cross-origin requests from localhost:4200
        registry.addMapping("/**")  // Apply to all endpoints
                .allowedOrigins("http://localhost:4200", "https://24ws-ase-pr-qse-05.apps.student.inso-w.at","https://mr.24ws-ase-pr-qse-05.apps.student.inso-w.at","https://prod.24ws-ase-pr-qse-05.apps.student.inso-w.at")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")  // Specify allowed HTTP methods
                .allowedHeaders("*")
                .exposedHeaders("*");  // Allow all headers

    }
}
