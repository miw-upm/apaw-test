package es.upm.miw.apaw.functionaltests.courthearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Court {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private CourtType type;
    private List<CourtHearing> courtHearings;
}