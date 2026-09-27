package es.upm.miw.apaw.functionaltests.leases;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {
    private UUID id;
    private String mobile;
    private String firstName;
}
