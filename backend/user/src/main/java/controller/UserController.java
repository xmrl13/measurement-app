package controller;

import dto.EmailDTO;
import dto.UserLoginRequest;
import dto.UserRequestDTO;
import dto.UserUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import service.UserService;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-by-email")
    public Mono<ResponseEntity<?>> getById(@RequestParam("email") String email, @RequestHeader("Authorization") String token) {
        return userService.getUserByEmail(email, token);
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody UserLoginRequest request) {
        return userService.authenticate(request);
    }

    @PostMapping()
    public Mono<ResponseEntity<String>> createUser(@RequestBody UserRequestDTO userRequestDTO, @RequestHeader("Authorization") String token) {
        return userService.createUser(userRequestDTO, token);
    }

    @DeleteMapping()
    public Mono<ResponseEntity<String>> deleteByEmail(@Valid @RequestBody EmailDTO emailDTO, @RequestHeader("Authorization") String token) {
        return userService.deleteUser(emailDTO, token);
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<?>> updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateUser(userUpdateDTO);
    }
}
