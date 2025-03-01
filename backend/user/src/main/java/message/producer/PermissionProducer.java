package message.producer;

import message.dto.PermissionEventDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionProducer {

    private final KafkaTemplate<String, PermissionEventDTO> kafkaTemplate;

    public PermissionProducer(KafkaTemplate<String, PermissionEventDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPermissionsAll(String replyTopic, List<PermissionEventDTO> permissions) {
        for (PermissionEventDTO permission : permissions) {
            kafkaTemplate.send(replyTopic, permission);
        }
        System.out.println("📡 Enviadas " + permissions.size() + " permissões para " + replyTopic);
    }


    public void sendPermissionUpdate(PermissionEventDTO event) {
        kafkaTemplate.send("permissions-update", event);
    }
}
