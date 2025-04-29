package belyaikin.telemoodle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="users")
public final class User {
    @Id
    private long telegramId;
    private String moodleToken;
}
