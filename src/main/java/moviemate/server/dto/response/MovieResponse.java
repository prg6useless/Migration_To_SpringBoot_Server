package moviemate.server.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieResponse {
    Integer id;
    String title;
    String slug;
    String duration;
    String synopsis;
    String poster;
    Integer rating;
    Integer seats;
    Integer price;
    LocalDateTime releaseDate;
    LocalDateTime endDate;
    Integer createdBy;
    Integer updatedBy;
}