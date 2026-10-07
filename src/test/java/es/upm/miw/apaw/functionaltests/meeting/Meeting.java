package es.upm.miw.apaw.functionaltests.meeting;

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
public class Meeting {
    private UUID id;
    private String title;
    private LocalDateTime meetingDate;
    private String location;
    private Integer durationMinutes;
    private Boolean online;
    private String description;
    private List<LegalIssue> legalIssues;
    private MeetingStatus meetingStatus;
    private List<UserSnapshot> participants;
}
