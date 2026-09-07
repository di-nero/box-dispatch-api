package com.example.box_dispatch_api.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI boxDispatchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Box Dispatch API")
                        .version("1.0")
                        .description("REST API for managing dispatch boxes and their items."));
    }
}
