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
public class Appointment {
    private UUID id;
    private String title;
    private LocalDateTime scheduledDate;
    private Integer durationMinutes;
    private LocalDateTime creationDate;
    private String notes;
    private Boolean virtual;
    private AppointmentStatus status;
    private AppointmentLocation location;
    private UserSnapshot client;
}