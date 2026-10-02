package es.upm.miw.apaw.functionaltests.courthearing;

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
public class CourtHearing {
    private UUID id;
    private LocalDateTime date;
    private String roomNumber;
    private String transcript;
    private Integer durationMinutes;
    private Boolean openToPublic;
    private Boolean remote;
    private CourtHearingType type;
    private CourtHearingStatus status;
    private List<UserSnapshot> attendees;
}