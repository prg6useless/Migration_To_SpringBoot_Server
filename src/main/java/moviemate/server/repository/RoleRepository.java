package moviemate.server.repository;

import moviemate.server.model.Role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository is an interface that provides access to data in a database
 */
public interface RoleRepository extends JpaRepository<Role, Integer>{
    Optional<Role> findByName(String name);
}
