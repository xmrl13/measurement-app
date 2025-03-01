package message.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.dto.PermissionEventDTO;
import message.service.PermissionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PermissionConsumer {

    private final PermissionService permissionService;

    public PermissionConsumer(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @KafkaListener(
            topics = "permissions-all",
            groupId = "user-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void handlePermissionRequest(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(message);
            String replyTopic = jsonNode.get("replyTopic").asText();

            System.out.println("📡 Solicitação recebida para enviar todas as permissões. Responder para: " + replyTopic);
            permissionService.sendAllPermissions(replyTopic).subscribe();
            System.out.println("Tentando em handle consumer");
        } catch (Exception e) {
            System.out.println("falahando em handle permission consumer");
            System.err.println("❌ Erro ao processar solicitação de permissões: " + e.getMessage());
        }
    }

}
