package message.service;

import message.dto.PermissionEventDTO;
import message.model.RolePermissionModel;
import message.repository.RolePermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.JwtUtils;

import java.util.List;

@Service
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    public PermissionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public Mono<ResponseEntity<String>> processPermissionEvent(PermissionEventDTO event) {
        System.out.println("🔄 Processando evento: " + event.getEventType());
        return switch (event.getEventType()) {
            case "ADDED" -> addPermission(event);
            case "UPDATED" -> updatePermission(event);
            case "REMOVED" -> removePermission(event);
            default ->
                    Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de evento inválido: " + event.getEventType()));
        };
    }

    public Mono<ResponseEntity<String>> addPermission(PermissionEventDTO event) {
        return rolePermissionRepository.findByRoleAndAction(event.getRole(), event.getAction())
                .flatMap(existing -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Permissão já existe")))
                .switchIfEmpty(rolePermissionRepository.save(new RolePermissionModel(event.getRole(), event.getAction(), event.isActive()))
                        .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("Permissão criada com sucesso")));
    }

    public Mono<ResponseEntity<String>> updatePermission(PermissionEventDTO event) {
        return rolePermissionRepository.findByRoleAndAction(event.getRole(), event.getAction())
                .flatMap(existingPermission -> {
                    existingPermission.setActive(event.isActive());
                    return rolePermissionRepository.save(existingPermission)
                            .thenReturn(ResponseEntity.ok("Permissão atualizada"));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permissão não encontrada")));
    }

    public Mono<ResponseEntity<String>> removePermission(PermissionEventDTO event) {
        return rolePermissionRepository.findByRoleAndAction(event.getRole(), event.getAction())
                .flatMap(existingPermission -> rolePermissionRepository.delete(existingPermission)
                        .thenReturn(ResponseEntity.ok("Permissão removida")))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permissão não encontrada")));
    }

    public Mono<Void> saveAllPermissions(List<PermissionEventDTO> permissions) {
        return rolePermissionRepository.deleteAll()
                .thenMany(Flux.fromIterable(permissions))
                .map(perm -> new RolePermissionModel(
                        perm.getRole().toUpperCase(),
                        perm.getAction().toUpperCase(),
                        perm.isActive()
                ))
                .flatMap(rolePermissionRepository::save)
                .then()
                .doOnSuccess(v -> System.out.println("💾 Todas as permissões foram persistidas"));
    }

    public Mono<Boolean> hasPermission(String token, String actionString) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String roleName = JwtUtils.getRoleFromToken(token);
        if (roleName == null) {
            return Mono.just(false);
        }

        return rolePermissionRepository.findByRoleAndAction(roleName.toUpperCase(), actionString.toUpperCase()).map(RolePermissionModel::isActive).defaultIfEmpty(false);
    }

}
