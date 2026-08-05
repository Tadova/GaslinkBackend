package com.gaslink.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GasLink API Documentation")
                        .version("1.0.0")
                        .description("""
                                Complete API documentation for GasLink - Gas delivery and vendor management platform.
                                """)
                        .contact(new Contact()
                                .name("GasLink Team")
                                .email("support@gaslink.com")
                                .url("https://gaslink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.gaslink.com")
                                .description("Production Server")
                ))
//                .tags(List.of(
//                        new Tag().name("Authentication").description("Authentication endpoints - Register, Login, OTP, Refresh Token"),
//                        new Tag().name("User Management").description("User profile management"),
//                        new Tag().name("Address Management").description("User address management"),
//                        new Tag().name("Vendor Management").description("Vendor registration and management"),
//                        new Tag().name("Inventory Management").description("Vendor inventory management"),
//                        new Tag().name("Order Management").description("Order creation and management"),
//                        new Tag().name("Payment").description("Payment processing with Paystack"),
//                        new Tag().name("Reviews").description("Vendor reviews and ratings"),
//                        new Tag().name("Subscriptions").description("Vendor subscription plans"),
//                        new Tag().name("Notifications").description("User notifications"),
//                        new Tag().name("Messages").description("Order chat messages"),
//                        new Tag().name("Admin").description("Administrative endpoints")
//                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token. Format: Bearer <token>"))
                        .addSecuritySchemes("paystackWebhook", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("x-paystack-signature")
                                .description("Paystack webhook signature")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}