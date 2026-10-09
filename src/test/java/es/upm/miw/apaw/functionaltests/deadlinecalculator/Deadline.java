package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deadline {
    private UUID id;
    private String title;
    private String courtFileNumber;
    private LocalDate notificationDate;
    private Integer days;
    private DayCountType dayCountType;
    private String region;
    private String city;
    private DeadlineStatus status;
    private LocalDateTime createdAt;
    private LocalDate dueDate;
    private List<NonWorkingDay> nonWorkingDays;
    private UserSnapshot lawyer;
}
