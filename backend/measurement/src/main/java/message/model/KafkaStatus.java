package message.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("kafka_status")
@Getter
@Setter
public class KafkaStatus {

    @Id
    private Long id;
    @Column("permissions_loaded")
    private boolean permissionsLoaded;
    @Column("sync_in_progress")
    private boolean syncInProgress;  // Novo campo

    public KafkaStatus() {
        this.permissionsLoaded = false;
        this.syncInProgress = false;
    }

}