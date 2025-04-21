package belyaikin.telemoodle.repository;

import belyaikin.telemoodle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByTelegramId(long id);
}
