package controller;

import dto.ItemDTO;
import dto.ItemRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import service.ItemService;

@RestController
@RequestMapping("api/itens")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }


    @PostMapping
    public Mono<ResponseEntity<String>> createItem(@RequestBody ItemDTO itemDTO, @RequestHeader("Authorization") String token) {
        return itemService.createItem(itemDTO, token);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteItem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        return itemService.deleteItem(id, token);
    }

    @GetMapping
    public Mono<ResponseEntity<?>> getAllItens(@RequestHeader("Authorization") String token) {
        return itemService.getAllItens(token);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getItem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        return itemService.getItem(id, token);
    }
}
