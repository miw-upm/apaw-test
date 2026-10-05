package es.upm.miw.apaw.functionaltests.roombooking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingFindCriteria {
    private Integer estimatedAttendees;
    private Boolean ongoing;
    private Boolean videoconferenceEquipped;
    private String userEmail;
}