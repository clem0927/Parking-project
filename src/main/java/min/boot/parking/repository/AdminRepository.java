package min.boot.parking.repository;

import min.boot.parking.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin,String> {
    boolean existsById(String id);
}
