package es.upm.miw.apaw.functionaltests.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyFindCriteria {
    private String language;
    private Boolean submitted;
    private SurveyQuestionType surveyQuestionType;
    private String userCity;
}
