package message.repository;

import message.model.KafkaStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface KafkaStatusRepository extends ReactiveCrudRepository<KafkaStatus, Long> {
    Mono<KafkaStatus> findFirstBy();
    Mono<KafkaStatus> findFirstByOrderByIdAsc();
}
