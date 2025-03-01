package message.repository;

import message.model.RolePermissionModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RolePermissionRepository extends ReactiveCrudRepository<RolePermissionModel, Long> {

    Mono<RolePermissionModel> findByRoleAndAction(String role, String action);

}
