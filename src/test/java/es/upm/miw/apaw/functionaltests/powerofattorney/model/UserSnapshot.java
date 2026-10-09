package es.upm.miw.apaw.functionaltests.powerofattorney.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {
    private UUID id;
    private String mobile;
    private String firstName;
    private String familyName;
    private String email;
    private String identity;
    private String city;
}
