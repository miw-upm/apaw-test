package es.upm.miw.apaw.functionaltests.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentLocationPatch {
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String room;
    private Integer floor;
}