package repository;

import model.RolePermissionModel;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RolePermissionRepository extends ReactiveCrudRepository<RolePermissionModel, Long> {

    // Busca todas as permissões ativas
    @Query("SELECT * FROM role_permissions WHERE active = true")
    Flux<RolePermissionModel> findAllActive();

    // Busca uma permissão específica
    @Query("SELECT * FROM role_permissions WHERE role = :role AND action = :action LIMIT 1")
    Mono<RolePermissionModel> findByRoleAndAction(String role, String action);

}
