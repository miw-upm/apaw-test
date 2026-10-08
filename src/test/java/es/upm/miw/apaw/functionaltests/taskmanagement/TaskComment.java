package es.upm.miw.apaw.functionaltests.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskComment {

    private UUID id;
    private String content;
    private LocalDateTime creationDate;
    private Boolean edition;
    private Boolean attachment;
    private CommentType type;
    private UserSnapshot author;
}
