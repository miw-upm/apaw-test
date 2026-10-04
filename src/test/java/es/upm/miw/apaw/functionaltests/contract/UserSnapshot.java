package es.upm.miw.apaw.functionaltests.contract;

import lombok.*;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {
    private UUID id;
    private String mobile;
    private String firstName;
}
