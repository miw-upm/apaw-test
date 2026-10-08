package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationDeadline {
    private String title;
    private String courtFileNumber;
    private LocalDate notificationDate;
    private Integer days;
    private DayCountType dayCountType;
    private String region;
    private String city;
    private UUID userId;
}
