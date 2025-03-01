package config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfigLogger {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    @PostConstruct
    public void printDatabaseUrl() {
        System.out.println("R2DBC Connection URL: " + r2dbcUrl);
    }
}
