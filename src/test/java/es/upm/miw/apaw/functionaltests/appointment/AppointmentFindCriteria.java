package es.upm.miw.apaw.functionaltests.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentFindCriteria {
    private AppointmentStatus status;
    private Boolean upcoming;
    private String city;
    private String clientMobile;
}