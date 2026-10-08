package es.upm.miw.apaw.functionaltests.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    private UUID id;
    private String title;
    private String description;
    private LocalDate createdDate;
    private LocalDate submittedDate;
    private String language;
    private List<SurveyQuestion> surveyQuestions;
    private UserSnapshot userSnapshot;
}
