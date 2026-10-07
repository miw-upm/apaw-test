package es.upm.miw.apaw.functionaltests.meeting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingParticipantReport {
    private UserSnapshot userSnapshot;
    private long meetingCount;
    private long legalIssueCount;
    private double averageLegalIssuePriority;
}
