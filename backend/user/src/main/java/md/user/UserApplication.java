package md.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(scanBasePackages = {"controller", "dto", "model", "repository", "service", "client", "config",
        "security.config", "security.exceptions", "security.jwt", "security.service", "message", "message.dto",
        "message.publisher", "message.service", "message.consumer", "message.controller", "message.config"})
@EnableR2dbcRepositories(basePackages = "repository")
@EntityScan(basePackages = "model")
@EnableDiscoveryClient
@EnableCaching
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);

    }
}
