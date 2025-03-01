package message.consumer;

import message.dto.PermissionEventDTO;
import message.model.KafkaStatus;
import message.producer.PermissionRequestProducer;
import message.repository.KafkaStatusRepository;
import message.service.PermissionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PermissionConsumer {

    private final PermissionService permissionService;
    private final KafkaStatusRepository kafkaStatusRepository;
    private final PermissionRequestProducer permissionRequestProducer;

    public PermissionConsumer(PermissionService permissionService, KafkaStatusRepository kafkaStatusRepository, PermissionRequestProducer permissionRequestProducer) {
        this.permissionService = permissionService;
        this.kafkaStatusRepository = kafkaStatusRepository;
        this.permissionRequestProducer = permissionRequestProducer;
    }

    @KafkaListener(topics = "permissions-update", groupId = "measurement-group")
    public void handlePermissionUpdate(PermissionEventDTO event) {
        kafkaStatusRepository.findFirstByOrderByIdAsc()
                .flatMap(status -> {
                    if (!status.isPermissionsLoaded() && !status.isSyncInProgress()) {
                        status.setSyncInProgress(true);
                        return kafkaStatusRepository.save(status)
                                .then(requestFullSync());
                    }
                    return processEventIfReady(event, status);
                })
                .subscribe();
    }

    private Mono<Void> requestFullSync() {
        System.out.println("🚀 Iniciando sincronização completa...");
        return permissionRequestProducer.requestAllPermissions()
                .doOnSuccess(v -> System.out.println("📨 Solicitação de sincronização enviada"));
    }

    private Mono<Void> processEventIfReady(PermissionEventDTO event, KafkaStatus status) {
        if (status.isPermissionsLoaded()) {
            return permissionService.processPermissionEvent(event).then();
        }
        System.out.println("⏳ Evento armazenado para processamento posterior: " + event.getEventType());
        return Mono.empty();
    }

    @KafkaListener(topics = "permissions-response", groupId = "client-group")
    public void handleSyncResponse(List<PermissionEventDTO> permissions) {
        System.out.println("📦 Recebida sincronização com " + permissions.size() + " permissões");

        permissionService.saveAllPermissions(permissions)
                .then(kafkaStatusRepository.findFirstByOrderByIdAsc())
                .flatMap(status -> {
                    status.setPermissionsLoaded(true);
                    status.setSyncInProgress(false);
                    return kafkaStatusRepository.save(status);
                })
                .doOnSuccess(s -> System.out.println("✅ Sincronização concluída! Status atualizado"))
                .doOnError(e -> System.err.println("🔥 Falha na sincronização: " + e.getMessage()))
                .subscribe();
    }
}