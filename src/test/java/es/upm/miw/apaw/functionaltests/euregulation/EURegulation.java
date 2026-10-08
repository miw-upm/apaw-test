package es.upm.miw.apaw.functionaltests.euregulation;

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
public class EURegulation {
    private UUID id;
    private String regulationName;
    private Integer sequentialId;
    private String officialReferenceNumber;
    private LegalInstrumentType instrumentType;
    private ApplicationArea applicationArea;
    private LegalStatus legalStatus;
    private LocalDate entryIntoForceDate;
    private IssuingBody issuingBody;
    private LocalDate transpositionDeadline;
    private String officialJournalLink;
    private String summary;
}
