package es.upm.miw.apaw.functionaltests.roombooking;

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
public class Room {
    private UUID id;
    private String name;
    private Integer capacity;
    private Integer floor;
    private Boolean videoconferenceEquipped;
    private LocalDateTime createdAt;
}