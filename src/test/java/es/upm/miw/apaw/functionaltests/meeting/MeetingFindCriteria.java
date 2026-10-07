package es.upm.miw.apaw.functionaltests.meeting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingFindCriteria {
    private Integer minDurationMinutes;
    private Boolean opened;
    private Integer maxLegalIssuePriority;
    private String participantFirstName;
}
