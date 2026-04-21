package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;

import java.util.List;

public interface ServiceReadingsAssembler {

    List<ServiceReadingsDto> assemble(List<LiturgicalReadingAssignment> assignments);
}
