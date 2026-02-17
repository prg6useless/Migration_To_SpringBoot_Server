package moviemate.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import moviemate.server.model.Movie;

@RestController
public class MovieController {

    @GetMapping("/movies")
    public List<Movie> getAllMovies() {
        return List.of(
                new Movie("Inception 4asdfd6"),
                new Movie("Avengers"));
    }
}