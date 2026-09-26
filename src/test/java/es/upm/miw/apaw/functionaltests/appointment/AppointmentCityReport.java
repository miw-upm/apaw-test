package es.upm.miw.apaw.functionaltests.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCityReport {
    private UUID clientId;
    private UserSnapshot client;
    private String city;
    private long totalAppointments;
}