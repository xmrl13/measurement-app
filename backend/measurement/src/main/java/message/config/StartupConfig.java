package message.config;

import message.model.KafkaStatus;
import message.repository.KafkaStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupConfig {

    @Bean
    CommandLineRunner initSyncStatus(KafkaStatusRepository repository) {
        return args -> repository.findFirstByOrderByIdAsc()
                .switchIfEmpty(repository.save(new KafkaStatus()))
                .subscribe(
                        s -> System.out.println("🟢 Status de sincronização inicializado"),
                        e -> System.err.println("🔴 Falha na inicialização do status: " + e)
                );
    }
}
