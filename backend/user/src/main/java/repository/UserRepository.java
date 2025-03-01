package repository;

import dto.UserRequestDTO;
import model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findByEmail(String email);

    Mono<UserRequestDTO> getUserByEmail(String username);

}
