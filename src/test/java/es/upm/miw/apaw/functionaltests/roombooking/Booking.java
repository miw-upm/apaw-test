package es.upm.miw.apaw.functionaltests.roombooking;

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
public class Booking {
    private UUID id;
    private String name;
    private Integer estimatedAttendees;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime createdAt;
    private Room room;
    private UserSnapshot userSnapshot;
}