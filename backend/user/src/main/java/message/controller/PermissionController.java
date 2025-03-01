package message.controller;

import message.dto.PermissionEventDTO;
import message.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/users/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping()
    public Mono<ResponseEntity<String>> addPermission(@RequestBody PermissionEventDTO permissionEventDTO) {
        permissionEventDTO.setEventType("ADDED");
        return permissionService.processPermissionEvent(permissionEventDTO);
    }

    @DeleteMapping()
    public Mono<ResponseEntity<String>> removePermission(@RequestBody PermissionEventDTO permissionEventDTO) {
        permissionEventDTO.setEventType("REMOVED");
        return permissionService.processPermissionEvent(permissionEventDTO);
    }

    @PutMapping()
    public Mono<ResponseEntity<String>> updatePermission(@RequestBody PermissionEventDTO permissionEventDTO) {
        permissionEventDTO.setEventType("UPDATED");
        return permissionService.processPermissionEvent(permissionEventDTO);
    }
}