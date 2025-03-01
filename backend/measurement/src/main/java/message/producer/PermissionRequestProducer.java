package message.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class PermissionRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PermissionRequestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> requestAllPermissions() {
        return Mono.fromRunnable(() -> {
            String payload = "{\"replyTopic\":\"permissions-response\", \"timestamp\":\"" + System.currentTimeMillis() + "\"}";
            kafkaTemplate.send("permissions-all", payload);
            System.out.println("📨 Solicitação de sincronização enviada para o User Service");
        });
    }



}
