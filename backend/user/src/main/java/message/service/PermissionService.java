package message.service;

import message.dto.PermissionEventDTO;
import message.producer.PermissionProducer;
import model.RolePermissionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import repository.RolePermissionRepository;
import security.jwt.JwtTokenProvider;

@Service
public class PermissionService {

    private final PermissionProducer permissionProducer;
    private final RolePermissionRepository rolePermissionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public PermissionService(PermissionProducer permissionProducer,
                             RolePermissionRepository rolePermissionRepository,
                             JwtTokenProvider jwtTokenProvider) {
        this.permissionProducer = permissionProducer;
        this.rolePermissionRepository = rolePermissionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Mono<ResponseEntity<String>> processPermissionEvent(PermissionEventDTO event) {
        return switch (event.getEventType()) {
            case "ADDED" -> addPermission(event);
            case "UPDATED" -> updatePermission(event);
            case "REMOVED" -> removePermission(event);
            default -> Mono.just(ResponseEntity.badRequest().body("Evento desconhecido"));
        };
    }

    public Mono<Void> sendAllPermissions(String replyTopic) {
        return rolePermissionRepository.findAll()
                .map(permission -> convertToDTO(permission, "LOAD_ALL_PERMISSIONS"))
                .collectList()
                .doOnSuccess(permissions -> {
                    if (!permissions.isEmpty()) {
                        permissionProducer.sendPermissionsAll(replyTopic, permissions);
                    } else {
                        System.out.println("⚠️ Nenhuma permissão para enviar");
                    }
                })
                .then();
    }

    private Mono<ResponseEntity<String>> addPermission(PermissionEventDTO event) {
        return rolePermissionRepository.findByRoleAndAction(event.getRole(), event.getAction())
                .flatMap(existing ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Permissão já existe"))
                )
                .switchIfEmpty(
                        rolePermissionRepository.save(new RolePermissionModel(event.getRole(), event.getAction(), event.isActive()))
                                .flatMap(saved -> {
                                    PermissionEventDTO savedEvent = convertToDTO(saved, "ADDED");
                                    permissionProducer.sendPermissionUpdate(savedEvent);
                                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body("Permissão criada"));
                                })
                );
    }

    private Mono<ResponseEntity<String>> updatePermission(PermissionEventDTO event) {
        return rolePermissionRepository.findByRoleAndAction(event.getRole(), event.getAction())
                .flatMap(existing -> {
                    existing.setActive(event.isActive());
                    return rolePermissionRepository.save(existing)
                            .flatMap(updated -> {
                                PermissionEventDTO updatedEvent = convertToDTO(updated, "UPDATED");
                                permissionProducer.sendPermissionUpdate(updatedEvent);
                                return Mono.just(ResponseEntity.ok("Permissão atualizada"));
                            });
                })
                .switchIfEmpty(
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permissão não encontrada"))
                );
    }

    private Mono<ResponseEntity<String>> removePermission(PermissionEventDTO event) {
        return rolePermissionRepository.findByRoleAndAction(event.getRole(), event.getAction())
                .flatMap(existing ->
                        rolePermissionRepository.delete(existing)
                                .then(Mono.defer(() -> {
                                    PermissionEventDTO removedEvent = convertToDTO(existing, "REMOVED");
                                    permissionProducer.sendPermissionUpdate(removedEvent);
                                    return Mono.just(ResponseEntity.ok("Permissão removida"));
                                }))
                )
                .switchIfEmpty(
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permissão não encontrada"))
                );
    }

    private PermissionEventDTO convertToDTO(RolePermissionModel model, String eventType) {
        return new PermissionEventDTO(
                model.getRole(),
                model.getAction(),
                eventType,
                model.isActive()
        );
    }

    public Mono<Boolean> hasPermission(String token, String actionString) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String roleName = jwtTokenProvider.getRoleFromToken(token);
        if (roleName == null) {
            return Mono.just(false);
        }

        String uppercaseRole = roleName.toUpperCase();
        actionString = actionString.toUpperCase();

        return rolePermissionRepository.findByRoleAndAction(uppercaseRole, actionString)
                .map(RolePermissionModel::isActive)
                .defaultIfEmpty(false);
    }

}