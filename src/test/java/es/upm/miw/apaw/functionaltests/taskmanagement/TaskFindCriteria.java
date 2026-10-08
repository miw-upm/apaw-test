package es.upm.miw.apaw.functionaltests.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskFindCriteria {

    private Integer priority;
    private Boolean overdue;
    private CommentType type;
    private String ownerFirstName;
}
