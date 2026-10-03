package es.upm.miw.apaw.functionaltests.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFindCriteria {
    private Priority priority;
    private Boolean sent;
    private String eventType;
    private String recipientEmail;
}
