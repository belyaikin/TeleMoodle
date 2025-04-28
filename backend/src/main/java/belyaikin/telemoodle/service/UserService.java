package belyaikin.telemoodle.service;

import belyaikin.telemoodle.model.User;
import belyaikin.telemoodle.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User create(User user) {
        return repository.save(user);
    }

    public User getByTelegramId(long id) {
        return repository.findById(id).orElse(null);
    }

    public boolean isUserRegistered(long id) {
        return getByTelegramId(id) != null;
    }

    public List<User> getAll() {
        return repository.findAll();
    }
}
