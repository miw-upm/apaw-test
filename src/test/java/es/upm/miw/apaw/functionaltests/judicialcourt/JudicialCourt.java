package es.upm.miw.apaw.functionaltests.judicialcourt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudicialCourt {
    private UUID id;
    private String name;
    private Integer number;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private JudicialCourtType type;
    private JudicialCourtStatus status;
    private List<UserSnapshot> lawyers;
}
