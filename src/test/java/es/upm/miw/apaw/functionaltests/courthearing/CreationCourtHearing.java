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
public class CreationCourtHearing {
    private LocalDateTime date;
    private String roomNumber;
    private Integer durationMinutes;
    private Boolean openToPublic;
    private Boolean remote;
    private CourtHearingType type;
    private UUID courtId;
    private List<UUID> attendeeIds;
}