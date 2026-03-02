package moviemate.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import moviemate.server.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Optional<Movie> findBySlug(String slug);

    // Check if it exists by slug
    boolean existsBySlug(String slug);

    // Delete by slug (Must be @Transactional for delete operations)
    @Transactional
    void deleteBySlug(String slug);
}
