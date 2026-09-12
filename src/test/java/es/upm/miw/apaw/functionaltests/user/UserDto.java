package es.upm.miw.apaw.functionaltests.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String mobile;
    private String firstName;
    private String familyName;
    private String email;
    private String address;
    private Boolean active;
}
