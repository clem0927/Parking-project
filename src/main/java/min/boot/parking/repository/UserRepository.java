package min.boot.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import min.boot.parking.entity.User;

public interface UserRepository extends JpaRepository<User, String>{
    boolean existsById(String id);
}
