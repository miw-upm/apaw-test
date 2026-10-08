package es.upm.miw.apaw.functionaltests.euregulation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EURegulationPatch {
    private String regulationName;
    private String officialReferenceNumber;
    private LegalInstrumentType instrumentType;
    private ApplicationArea applicationArea;
    private LegalStatus legalStatus;
    private IssuingBody issuingBody;
    private LocalDate transpositionDeadline;
    private String officialJournalLink;
    private String summary;
}
