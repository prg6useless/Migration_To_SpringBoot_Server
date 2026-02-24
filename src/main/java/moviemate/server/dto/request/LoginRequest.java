package moviemate.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // generates getters, setters, toString, etc.
@NoArgsConstructor
@AllArgsConstructor

public class LoginRequest {
    private String email;
    private String password;
}
