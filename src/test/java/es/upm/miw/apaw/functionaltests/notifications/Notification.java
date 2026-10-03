package es.upm.miw.apaw.functionaltests.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private UUID id;
    private String title;
    private String message;
    private LocalDate createdAt;
    private LocalDate sentAt;
    private NotificationTemplate notificationTemplate;
    private Priority priority;
    private NotificationStatus notificationStatus;
    private NotificationRecipient recipient;
}
