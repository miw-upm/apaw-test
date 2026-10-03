package es.upm.miw.apaw.functionaltests.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    private UUID id;
    private String eventType;
    private String subjectTemplate;
    private String bodyTemplate;
    private Channel channel;
}
