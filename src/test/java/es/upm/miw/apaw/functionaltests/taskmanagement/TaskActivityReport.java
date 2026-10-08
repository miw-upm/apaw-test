package es.upm.miw.apaw.functionaltests.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskActivityReport {

    private String taskTitle;
    private UserSnapshot owner;
    private long totalCommentCount;
    private long importantCommentCount;
    private long editedCommentCount;
    private long attachmentCommentCount;
}
