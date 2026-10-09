package es.upm.miw.apaw.functionaltests.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    private UUID id;
    private String invoiceNumber;
    private LocalDate issueDate;
    private BigDecimal taxableBase;
    private BigDecimal vatRate;
    private Boolean paid;
    private PaymentType paymentType;
    private List<LegalService> services;
    private UserSnapshot customer;
}
