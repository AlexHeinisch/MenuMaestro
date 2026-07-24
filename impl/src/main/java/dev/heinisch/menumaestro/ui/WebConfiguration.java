package dev.heinisch.menumaestro.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.Set;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    // The service worker, its manifest, and the app shell must always be revalidated so clients
    // pick up new deployments; the Angular service worker's own update check depends on
    // ngsw.json/ngsw-worker.js never being served stale from an HTTP cache.
    private static final Set<String> NO_CACHE_PATHS = Set.of(
            "/ui/index.html", "/ui/manifest.webmanifest", "/ui/ngsw.json", "/ui/ngsw-worker.js"
    );

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource : new ClassPathResource("/static/ui/index.html");
                    }
                });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Cache-Control is decided here (rather than via ResourceHandlerRegistry.setCacheControl)
        // so a single rule set wins for every /ui/** response: registering a second, wildcard-scoped
        // resource handler for the hashed bundles runs its own header logic after this interceptor's
        // preHandle and would silently overwrite the no-cache paths above (ngsw-worker.js is both a
        // no-cache path and a *.js file).
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String uri = request.getRequestURI();
                if (NO_CACHE_PATHS.contains(uri)) {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                } else if (uri.startsWith("/ui/") && (uri.endsWith(".js") || uri.endsWith(".css"))) {
                    response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
                }
                return true;
            }
        }).addPathPatterns("/ui/**");
    }
}
