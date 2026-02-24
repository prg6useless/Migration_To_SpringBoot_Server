package moviemate.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserByAdminRequest {
    private String name;
    private String email;
    private Boolean isActive;
}