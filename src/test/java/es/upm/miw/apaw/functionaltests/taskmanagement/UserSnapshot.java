package es.upm.miw.apaw.functionaltests.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {

    private UUID id;
    private String firstName;
    private String familyName;
    private String email;
}
