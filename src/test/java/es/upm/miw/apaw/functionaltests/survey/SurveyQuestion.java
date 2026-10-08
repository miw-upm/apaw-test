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
public class SurveyQuestion {
    private UUID id;
    private String text;
    private SurveyQuestionType surveyQuestionType;
    private Boolean required;
    private Integer maxLength;
    private List<String> options;
}
