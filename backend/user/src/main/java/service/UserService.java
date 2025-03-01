package service;

import dto.EmailDTO;
import dto.UserLoginRequest;
import dto.UserRequestDTO;
import dto.UserUpdateDTO;
import jakarta.validation.Valid;
import message.service.PermissionService;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import repository.UserRepository;
import security.jwt.JwtTokenProvider;

import java.time.Duration;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionService permissionService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger log = LoggerFactory.getLogger(UserService.class);


    public UserService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider, PermissionService permissionService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.permissionService = permissionService;
    }

    public Mono<ResponseEntity<String>> authenticate(UserLoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.getEmail())
                .flatMap(user -> {
                    if (!passwordMatches(loginRequest.getPassword(), user.getPassword())) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body("Credenciais inválidas"));
                    }
                    String token = jwtTokenProvider.generateToken(
                            user.getEmail(),
                            user.getRole().name()
                    );
                    return Mono.just(ResponseEntity.ok(token));
                })
                .defaultIfEmpty(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Credenciais inválidas"));
    }


    public Mono<ResponseEntity<String>> createUser(UserRequestDTO userRequestDTO, String token) {
        log.info("CREATE USER START");
        log.info("Iniciando criação de usuário para e-mail: {}", userRequestDTO.getEmail());

        return permissionService.hasPermission(token, "create_user")
                .flatMap(canCreate -> {
                    if (!canCreate) {
                        log.warn("Usuário sem permissão para criar usuário: {}", userRequestDTO.getEmail());
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Sem permissão para criar usuário"));
                    }

                    return userRepository.findByEmail(userRequestDTO.getEmail())
                            .flatMap(existing -> {
                                log.warn("E-mail já cadastrado: {}", userRequestDTO.getEmail());
                                return Mono.just(ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body("Usuário com esse e-mail já existe"));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.info("Criando novo usuário: {}", userRequestDTO.getEmail());

                                User user = new User();
                                user.setName(userRequestDTO.getName());
                                user.setEmail(userRequestDTO.getEmail());
                                user.setRole(userRequestDTO.getRole());

                                user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
                                user.setSecretPhrase(passwordEncoder.encode(userRequestDTO.getSecretPhrase()));

                                return userRepository.save(user)
                                        .map(saved -> {
                                            log.info("Usuário criado com sucesso: {}", saved.getEmail());
                                            return ResponseEntity
                                                    .status(HttpStatus.CREATED)
                                                    .body("Usuário criado com sucesso");
                                        });
                            }));
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.error("Timeout: A criação de usuário demorou mais que 5 segundos!");
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.REQUEST_TIMEOUT)
                                .body("Tempo limite atingido. Tente novamente mais tarde."));
                    }
                    log.error("Erro inesperado ao criar usuário: {}", throwable.getMessage(), throwable);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro ao criar usuário."));
                })
                .doFinally(signalType -> log.info("CREATE USER END"));
    }

    public Mono<ResponseEntity<String>> deleteUser(EmailDTO emailDTO, String token) {
        log.info("DELETE USER START");
        log.info("Iniciando exclusão de usuário com e-mail: {}", emailDTO.getEmail());

        return permissionService.hasPermission(token, "delete_user")
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Erro na verificação de permissão: {}", e.getMessage());
                    return Mono.just(false);
                })
                .flatMap(canDelete -> {
                    if (!canDelete) {
                        log.warn("Usuário sem permissão para deletar: {}", emailDTO.getEmail());
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Sem permissão para deletar usuário"));
                    }
                    return userRepository.findByEmail(emailDTO.getEmail())
                            .timeout(Duration.ofSeconds(5))
                            .onErrorResume(e -> {
                                log.error("Erro ao buscar usuário no banco: {}", e.getMessage());
                                return Mono.error(new RuntimeException("Erro ao buscar usuário."));
                            })
                            .flatMap(existingUser ->
                                    userRepository.delete(existingUser)
                                            .then(Mono.just(ResponseEntity
                                                    .ok("Usuário deletado com sucesso")))
                                            .doOnSuccess(response -> log.info("Usuário deletado com sucesso: {}", emailDTO.getEmail()))
                            )
                            .switchIfEmpty(Mono.just(ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body("Usuário não encontrado: " + emailDTO.getEmail())));
                })
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.error("Tempo limite atingido ao deletar usuário");
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.REQUEST_TIMEOUT)
                                .body("Tempo limite atingido. Tente novamente mais tarde."));
                    }
                    log.error("Erro inesperado ao deletar usuário: {}", throwable.getMessage(), throwable);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro ao deletar usuário."));
                })
                .doFinally(signalType -> log.info("DELETE USER END"));
    }

    public Mono<ResponseEntity<?>> updateUser(@Valid UserUpdateDTO userUpdateDTO) {
        log.info("UPDATE USER START");
        log.info("Iniciando atualização de usuário com e-mail antigo: {}", userUpdateDTO.getOldEmail());

        return userRepository.findByEmail(userUpdateDTO.getOldEmail())
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Erro ao buscar usuário no banco: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Erro ao buscar usuário."));
                })
                .flatMap(user -> {
                    if (!passwordEncoder.matches(userUpdateDTO.getSecretPhrase(), user.getSecretPhrase())) {
                        log.warn("Frase secreta incorreta para atualização de usuário: {}", userUpdateDTO.getOldEmail());
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body("Frase secreta incorreta"));
                    }

                    if (userUpdateDTO.getName() != null) {
                        user.setName(userUpdateDTO.getName());
                    }

                    if (userUpdateDTO.getNewEmail() != null) {
                        if (user.getEmail().equals(userUpdateDTO.getNewEmail())) {
                            log.warn("Novo e-mail deve ser diferente do atual: {}", userUpdateDTO.getNewEmail());
                            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body("O novo e-mail deve ser diferente do e-mail já cadastrado."));
                        }

                        return userRepository.findByEmail(userUpdateDTO.getNewEmail())
                                .timeout(Duration.ofSeconds(5))
                                .onErrorResume(e -> {
                                    log.error("Erro ao verificar se o e-mail já está cadastrado: {}", e.getMessage());
                                    return Mono.error(new RuntimeException("Erro ao verificar e-mail."));
                                })
                                .flatMap(exist -> {
                                    log.warn("E-mail já cadastrado: {}", userUpdateDTO.getNewEmail());
                                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                            .body("Email já cadastrado"));
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    user.setEmail(userUpdateDTO.getNewEmail());
                                    return userRepository.save(user)
                                            .timeout(Duration.ofSeconds(5))
                                            .doOnSuccess(saved -> log.info("Usuário atualizado com novo e-mail: {}", userUpdateDTO.getNewEmail()))
                                            .map(saved -> ResponseEntity.status(HttpStatus.OK).build());
                                }));
                    }

                    if (userUpdateDTO.getNewPassword() != null) {
                        if (!passwordEncoder.matches(userUpdateDTO.getNewPassword(), user.getPassword())) {
                            user.setPassword(passwordEncoder.encode(userUpdateDTO.getNewPassword()));
                        } else {
                            log.warn("Nova senha deve ser diferente da atual para: {}", userUpdateDTO.getOldEmail());
                            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("A nova senha deve ser diferente da atual"));
                        }
                    }

                    return userRepository.save(user)
                            .timeout(Duration.ofSeconds(5))
                            .doOnSuccess(saved -> log.info("Usuário atualizado com sucesso: {}", userUpdateDTO.getOldEmail()))
                            .map(saved -> ResponseEntity.ok().build());
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Usuário não encontrado: " + userUpdateDTO.getOldEmail())))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.error("Tempo limite atingido ao atualizar usuário");
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.REQUEST_TIMEOUT)
                                .body("Tempo limite atingido. Tente novamente mais tarde."));
                    }
                    log.error("Erro inesperado ao atualizar usuário: {}", throwable.getMessage(), throwable);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro ao atualizar usuário."));
                })
                .doFinally(signalType -> log.info("UPDATE USER END"));
    }

    private boolean passwordMatches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}