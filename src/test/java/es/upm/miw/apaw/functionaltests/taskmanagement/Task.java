package es.upm.miw.apaw.functionaltests.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private UUID id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer priority;
    private Boolean completion;
    private BigDecimal estimatedHours;
    private List<TaskComment> comments;
    private UserSnapshot owner;
}
