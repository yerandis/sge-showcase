package com.yerandis.sge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOrigin("http://localhost:5173");
//        config.addAllowedOrigin("http://localhost:3000");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("DELETE");
//        config.addAllowedMethod("PATCH");
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedHeader("*");
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/api/**", source.getCorsConfigurations().isEmpty()
//                ? new UrlBasedCorsConfigurationSource().getCorsConfigurations().getOrDefault("/**", config)
//                : config);
//
//        // WebSocket — SockJS hace peticiones HTTP a /ws/info, /ws/iframe, etc.
//        // antes de establecer la conexión WebSocket real
////        source.registerCorsConfiguration("/ws/**", config);
//
//        // Forma simple y correcta:
//        UrlBasedCorsConfigurationSource finalSource = new UrlBasedCorsConfigurationSource();
//        finalSource.registerCorsConfiguration("/api/**", config);
//        return new CorsFilter(finalSource);
//    }

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
