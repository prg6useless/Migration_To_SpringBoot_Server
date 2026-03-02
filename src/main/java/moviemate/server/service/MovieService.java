package moviemate.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moviemate.server.dto.response.MovieResponse;
import moviemate.server.exception.MovieNotFoundException;
import moviemate.server.model.Movie;
import moviemate.server.repository.MovieRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    // Map Movie → MovieResponse
    private MovieResponse toMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .duration(movie.getDuration())
                .synopsis(movie.getSynopsis())
                .poster(movie.getPoster())
                .rating(movie.getRating())
                .seats(movie.getSeats())
                .price(movie.getPrice())
                .releaseDate(movie.getReleaseDate())
                .endDate(movie.getEndDate())
                // Check for null to avoid NullPointerException
                .createdBy(movie.getCreatedBy() != null ? movie.getCreatedBy().getId() : null)
                .updatedBy(movie.getUpdatedBy() != null ? movie.getUpdatedBy().getId() : null)
                .build();
    }

    // create movie
    public String saveMovie(Movie movie) {
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
        movieRepository.save(movie);
        log.info("Movie with id: {} saved successfully", movie.getId());
        return "Movie saved successfully";
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toMovieResponse)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> getMovieBySlug(String slug) {
        return movieRepository.findBySlug(slug)
                .stream()
                .map(this::toMovieResponse)
                .collect(Collectors.toList());
    }

    // udpate movie (only admin access)
    public MovieResponse updateMovieBySlug(Movie movieUpdates, String slug) {
        Movie existingMovie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));

        if (movieUpdates.getSlug() != null) {
            existingMovie.setSlug(movieUpdates.getSlug());
        }
        if (movieUpdates.getTitle() != null) {
            existingMovie.setTitle(movieUpdates.getTitle());
        }
        if (movieUpdates.getPrice() != null) {
            existingMovie.setPrice(movieUpdates.getPrice());
        }
        if (movieUpdates.getRating() != null) {
            existingMovie.setRating(movieUpdates.getRating());
        }
        if (movieUpdates.getSeats() != null) {
            existingMovie.setSeats(movieUpdates.getSeats());
        }
        existingMovie.setUpdatedAt(LocalDateTime.now());

        Movie updatedMovie = movieRepository.save(existingMovie);

        log.info("Movie with slug: {} updated successfully", updatedMovie.getSlug());
        return toMovieResponse(existingMovie);
    }

    // udpate movie seats (only admin access)
    public MovieResponse updateMovieSeatsBySlug(Movie movieUpdates, String slug) {
        Movie existingMovie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));

        if (movieUpdates.getSeats() != null) {
            existingMovie.setSeats(movieUpdates.getSeats());
        }
        existingMovie.setUpdatedAt(LocalDateTime.now());

        Movie updatedMovie = movieRepository.save(existingMovie);

        log.info("Movie seats with slug: {} updated successfully", updatedMovie.getSlug());
        return toMovieResponse(existingMovie);
    }

    // udpate movie release date (only admin access)
    public MovieResponse updateMovieReleaseDateBySlug(Movie movieUpdates, String slug) {
        Movie existingMovie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));

        if (movieUpdates.getReleaseDate() != null) {
            existingMovie.setReleaseDate(movieUpdates.getReleaseDate());
        }
        existingMovie.setUpdatedAt(LocalDateTime.now());

        Movie updatedMovie = movieRepository.save(existingMovie);

        log.info("Movie release date with slug: {} updated successfully", updatedMovie.getSlug());
        return toMovieResponse(existingMovie);
    }

    // delete movie by slug (only admin access)
    public String deleteMovieBySlug(String slug) {
        if (!movieRepository.existsBySlug(slug)) {
            throw new MovieNotFoundException("Movie not found with slug: " + slug);
        }
        movieRepository.deleteBySlug(slug);
        log.info("Movie with slug: {} deleted successfully", slug);
        return "Movie deleted successfully";
    }
}