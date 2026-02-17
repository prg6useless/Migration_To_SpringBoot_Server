package moviemate.server.repository;

import moviemate.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository is an interface that provides access to data in a database
 */
public interface UserRepository extends JpaRepository<User, Integer> {

}
