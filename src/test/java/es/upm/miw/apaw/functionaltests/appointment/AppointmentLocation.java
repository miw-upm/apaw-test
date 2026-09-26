package es.upm.miw.apaw.functionaltests.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentLocation {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String room;
    private Integer floor;
    private LocalDateTime creationDate;
}