package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.reading.v2.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;

import java.util.List;

public interface ServiceReadingsAssembler {

    List<ServiceReadingsDto> assemble(List<LiturgicalReadingAssignment> assignments);
}
