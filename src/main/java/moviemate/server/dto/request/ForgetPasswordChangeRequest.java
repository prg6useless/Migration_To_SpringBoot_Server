package moviemate.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgetPasswordChangeRequest {
    private String email;
    private String otp;
    private String newPassword;
}
