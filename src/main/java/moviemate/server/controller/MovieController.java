package moviemate.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import moviemate.server.dto.response.MessageResponse;
import moviemate.server.dto.response.MovieResponse;
import moviemate.server.model.Movie;
import moviemate.server.service.MovieService;

@RestController
@RequestMapping("api/v1/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

    private final MovieService movieService;

    // TO DO : testing remaining in postman
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> saveMpvie(@RequestBody Movie movie) {
        String message = movieService.saveMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(message));
    }

    @GetMapping("/")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok().body(movieService.getAllMovies());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<List<MovieResponse>> getMovieBySlug(@PathVariable String slug) {
        return ResponseEntity.ok().body(movieService.getMovieBySlug(slug));
    }

    @PutMapping("/{slug}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovieBySlug(@PathVariable String slug, @RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.updateMovieBySlug(movie, slug));
    }

    @PatchMapping("/{slug}/seats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovieSeatsBySlug(@PathVariable String slug, @RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.updateMovieSeatsBySlug(movie, slug));
    }

    @PatchMapping("/{slug}/release-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovieReleaseDateBySlug(@PathVariable String slug,
            @RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.updateMovieReleaseDateBySlug(movie, slug));
    }

    @DeleteMapping("/{slug}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMovieBySlug(@PathVariable String slug) {
        String message = movieService.deleteMovieBySlug(slug);
        return ResponseEntity.ok(new MessageResponse(message));
    }
}