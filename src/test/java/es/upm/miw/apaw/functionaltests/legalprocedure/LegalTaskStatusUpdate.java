package es.upm.miw.apaw.functionaltests.legalprocedure;

import java.util.UUID;

public record LegalTaskStatusUpdate(UUID id, TaskStatus taskStatus) {
}
