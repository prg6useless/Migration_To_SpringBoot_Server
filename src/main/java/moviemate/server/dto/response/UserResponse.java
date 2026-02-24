package moviemate.server.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private String image;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private List<String> roles;
}