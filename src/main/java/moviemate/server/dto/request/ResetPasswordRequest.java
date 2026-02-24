package moviemate.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResetPasswordRequest {
    private Integer id;

    @JsonProperty("newPassword")
    private String newPassword;
}
