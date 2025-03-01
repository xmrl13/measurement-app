package service;

import dto.*;
import enums.Role;
import jakarta.validation.Valid;
import message.service.PermissionService;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
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

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionService permissionService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Timeout único para todos os métodos
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public UserService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PermissionService permissionService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.permissionService = permissionService;
    }

    /**
     * Autentica o usuário, gerando token caso as credenciais estejam corretas.
     */
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
                        .body("Credenciais inválidas"))
                .timeout(TIMEOUT)
                .onErrorResume(this::handleTimeout);
    }

    /**
     * Cria um novo usuário, caso permitido.
     */
    public Mono<ResponseEntity<String>> createUser(UserRequestDTO userRequestDTO, String token) {
        log.info("CREATE USER START | E-mail: {}", userRequestDTO.getEmail());

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
                                        .map(saved -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body("Usuário criado com sucesso"));
                            }));
                })
                .timeout(TIMEOUT)
                .onErrorResume(this::handleTimeout)
                .doFinally(signalType -> log.info("CREATE USER END"));
    }

    /**
     * Deleta um usuário, se permitido.
     */
    public Mono<ResponseEntity<String>> deleteUser(EmailDTO emailDTO, String token) {
        log.info("DELETE USER START | E-mail: {}", emailDTO.getEmail());

        return permissionService.hasPermission(token, "delete_user")
                .flatMap(canDelete -> {
                    if (!canDelete) {
                        log.warn("Usuário sem permissão para deletar: {}", emailDTO.getEmail());
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("Sem permissão para deletar usuário"));
                    }
                    return userRepository.findByEmail(emailDTO.getEmail())
                            .flatMap(existingUser ->
                                    userRepository.delete(existingUser)
                                            .then(Mono.just(ResponseEntity.ok("Usuário deletado com sucesso"))))
                            .switchIfEmpty(Mono.just(ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body("Usuário não encontrado: " + emailDTO.getEmail())));
                })
                .timeout(TIMEOUT)
                .onErrorResume(this::handleTimeout)
                .doFinally(signalType -> log.info("DELETE USER END"));
    }

    /**
     * Atualiza os dados de um usuário existente, se permitido.
     */
    public Mono<ResponseEntity<?>> updateUser(@Valid UserUpdateDTO userUpdateDTO) {
        log.info("UPDATE USER START | E-mail antigo: {}", userUpdateDTO.getOldEmail());

        return userRepository.findByEmail(userUpdateDTO.getOldEmail())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(userUpdateDTO.getSecretPhrase(), user.getSecretPhrase())) {
                        log.warn("Frase secreta incorreta para atualização de usuário: {}", userUpdateDTO.getOldEmail());
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body("Frase secreta incorreta"));
                    }

                    // Atualiza nome
                    if (userUpdateDTO.getName() != null) {
                        user.setName(userUpdateDTO.getName());
                    }

                    // Atualiza e-mail
                    if (userUpdateDTO.getNewEmail() != null &&
                            !user.getEmail().equals(userUpdateDTO.getNewEmail())) {

                        return userRepository.findByEmail(userUpdateDTO.getNewEmail())
                                .flatMap(exist -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("Email já cadastrado")))
                                .switchIfEmpty(Mono.defer(() -> {
                                    user.setEmail(userUpdateDTO.getNewEmail());
                                    return userRepository.save(user)
                                            .map(saved -> ResponseEntity.ok().build());
                                }));
                    }

                    // Atualiza senha
                    if (userUpdateDTO.getNewPassword() != null) {
                        if (!passwordEncoder.matches(userUpdateDTO.getNewPassword(), user.getPassword())) {
                            user.setPassword(passwordEncoder.encode(userUpdateDTO.getNewPassword()));
                        } else {
                            log.warn("Nova senha deve ser diferente da atual para: {}", userUpdateDTO.getOldEmail());
                            return Mono.just(ResponseEntity
                                    .status(HttpStatus.BAD_REQUEST)
                                    .body("A nova senha deve ser diferente da atual"));
                        }
                    }

                    // Salva alterações
                    return userRepository.save(user).map(saved -> ResponseEntity.ok().build());
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Usuário não encontrado: " + userUpdateDTO.getOldEmail())))
                .timeout(TIMEOUT)
                .onErrorResume(this::handleTimeout)
                .doFinally(signalType -> log.info("UPDATE USER END"));
    }

    /**
     * Retorna um usuário pelo Email, armazenando em cache (permissão READ_USER exigida).
     * Retorna um UserResponse como DTO.
     */
    @Cacheable(value = "users", key = "#email")
    public Mono<ResponseEntity<?>> getUserByEmail(String email, String token) {
        log.info("GET USER BY EMAIL START | E-mail: {}", email);

        return permissionService.hasPermission(token, "READ_USER")
                .flatMap(canRead -> {
                    if (!canRead) {
                        log.warn("Usuário sem permissão para ler dados: {}", email);
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(null));
                    }
                    return userRepository.findByEmail(email)
                            .flatMap(user -> {
                                UserDetailsDTO dto = new UserDetailsDTO();
                                dto.setName(user.getName());
                                dto.setEmail(user.getEmail());
                                dto.setRole(Role.valueOf(user.getRole().name()));

                                return Mono.just(ResponseEntity.ok(dto));
                            })
                            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
                })
                .timeout(TIMEOUT)
                .onErrorResume(this::handleTimeout)
                .doFinally(signalType -> log.info("GET USER BY EMAIL END"));
    }

    /**
     * Método auxiliar para checar a senha.
     */
    private boolean passwordMatches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

    /**
     * Trata o Timeout ou outro erro inesperado.
     */
    private <T> Mono<ResponseEntity<T>> handleTimeout(Throwable throwable) {
        if (throwable instanceof java.util.concurrent.TimeoutException) {
            log.error("Tempo limite atingido!");
            return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body((T) "Tempo limite atingido. Tente novamente mais tarde."));
        }
        log.error("Erro inesperado: {}", throwable.getMessage(), throwable);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body((T) "Erro ao processar requisição."));
    }
}