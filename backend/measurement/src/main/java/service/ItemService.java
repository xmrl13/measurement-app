package service;

import dto.ItemDTO;
import message.repository.RolePermissionRepository;
import message.service.PermissionService;
import model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import repository.ItemRepository;
import utils.JwtUtils;


@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final PermissionService permissionService;

    public ItemService(ItemRepository itemRepository, PermissionService permissionService) {
        this.itemRepository = itemRepository;
        this.permissionService = permissionService;
    }

    public Mono<ResponseEntity<String>> createItem(ItemDTO itemDTO, String token) {
        String role = JwtUtils.getRoleFromToken(token);
        return permissionService.hasPermission(role, "CREATE_ITEM")
                .flatMap(hasPermission -> {
                    if (!hasPermission) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado."));
                    }
                    Item item = new Item(itemDTO.getName(), itemDTO.getUnit());
                    return itemRepository.save(item)
                            .map(savedItem -> ResponseEntity.status(HttpStatus.CREATED)
                                    .body("Item criado com sucesso: " + savedItem.getId()));
                });
    }

    public Mono<ResponseEntity<String>> deleteItem(Long id, String token) {
        String role = JwtUtils.getRoleFromToken(token);
        return permissionService.hasPermission(role, "DELETE_ITEM")
                .flatMap(hasPermission -> {
                    if (!hasPermission) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado."));
                    }
                    return itemRepository.findById(id)
                            .flatMap(existingItem -> itemRepository.delete(existingItem)
                                    .then(Mono.just(ResponseEntity.ok("Item deletado com sucesso."))))
                            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body("Item não encontrado.")));
                });
    }

    public Mono<ResponseEntity<?>> getAllItens(String token) {
        String role = JwtUtils.getRoleFromToken(token);
        return permissionService.hasPermission(role, "READ_ITEM")
                .flatMap(hasPermission -> {
                    if (!hasPermission) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado."));
                    }
                    return itemRepository.findAll()
                            .collectList()
                            .map(ResponseEntity::ok);
                });
    }

    public Mono<ResponseEntity<?>> getItem(Long id, String token) {
        String role = JwtUtils.getRoleFromToken(token);

        return permissionService.hasPermission(role, "READ_ITEM")
                .flatMap(hasPermission -> {
                    if (!hasPermission) {
                        // Retorna Mono<ResponseEntity<?>>
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado."));
                    }
                    // Aqui também retornamos Mono<ResponseEntity<?>>
                    return itemRepository.findById(id)
                            // Forçamos o tipo <ResponseEntity<?>>
                            .<ResponseEntity<?>>map(item -> ResponseEntity.ok(new ItemDTO(item.getName(), item.getUnit())))
                            // Caso não encontre, definimos o retorno default (mesmo tipo)
                            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item não encontrado."));
                });
    }
}
