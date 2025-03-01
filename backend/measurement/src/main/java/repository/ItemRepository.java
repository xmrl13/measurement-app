package repository;


import model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    Mono<Item> findByNameAndUnit(String name, String unit);

    @Query("SELECT * FROM app.itens")
    Flux<Item> findAll();

}
