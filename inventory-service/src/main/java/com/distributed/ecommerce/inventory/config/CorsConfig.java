package com.distributed.ecommerce.inventory.config;package com.distributed.ecommerce.inventory.config;package com.distributed.ecommerce.inventory.config;package com.distributed.ecommerce.inventory.config;



import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;import org.springframework.context.annotation.Configuration;



@Configurationimport org.springframework.web.servlet.config.annotation.CorsRegistry;

public class CorsConfig implements WebMvcConfigurer {

    import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;import org.springframework.context.annotation.Configuration;import org.springframework.beans.factory.annotation.Value;

    @Override

    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")

                .allowedOriginPatterns("*")@Configurationimport org.springframework.web.servlet.config.annotation.CorsRegistry;import org.springframework.context.annotation.Configuration;

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                .allowedHeaders("*")public class CorsConfig implements WebMvcConfigurer {

                .allowCredentials(true)

                .maxAge(3600);    import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;import org.springframework.web.servlet.config.annotation.CorsRegistry;

    }

}    @Override

    public void addCorsMappings(CorsRegistry registry) {import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

        registry.addMapping("/**")

                .allowedOriginPatterns("*")@Configuration

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                .allowedHeaders("*")public class CorsConfig implements WebMvcConfigurer {@Configuration

                .allowCredentials(true)

                .maxAge(3600);    public class CorsConfig implements WebMvcConfigurer {

    }

}    @Override    

    public void addCorsMappings(CorsRegistry registry) {    @Value("${cors.allowed-origins:http://localhost:3000,http://192.168.207.157:3000}")

        registry.addMapping("/**")    private String allowedOrigins;

                .allowedOriginPatterns("*")    

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")    @Override

                .allowedHeaders("*")    public void addCorsMappings(CorsRegistry registry) {

                .allowCredentials(true)        registry.addMapping("/**")

                .maxAge(3600);                .allowedOriginPatterns(allowedOrigins.split(","))

    }                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

}                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
