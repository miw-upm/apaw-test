package es.upm.miw.apaw.functionaltests.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationSurvey {
    private String title;
    private String description;
    private String language;
    private List<UUID> surveyQuestionIds;
    private UUID userId;
}
