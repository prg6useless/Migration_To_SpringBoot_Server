package moviemate.server.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Integer id;
    private String name;
    private String email;
    private List<String> roles;
}
