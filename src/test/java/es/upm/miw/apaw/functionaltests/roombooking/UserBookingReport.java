package es.upm.miw.apaw.functionaltests.roombooking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingReport {
    private UserSnapshot userSnapshot;
    private Long totalBookings;
    private Long totalAttendees;
}