package es.upm.miw.apaw.functionaltests.judicialcourt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationJudicialCourt {
    private String name;
    private Integer number;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    private String email;
    private UUID typeId;
    private List<UUID> lawyerIds;
}
