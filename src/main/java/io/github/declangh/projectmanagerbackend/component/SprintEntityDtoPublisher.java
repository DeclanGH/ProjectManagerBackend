package io.github.declangh.projectmanagerbackend.component;

import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class SprintEntityDtoPublisher {
    private static final Logger logger = LoggerFactory.getLogger(SprintEntityDtoPublisher.class);

    private final Sinks.Many<SprintEntityDto> sprintEntityDtoSink = Sinks.many().replay().latest();

    public void publish(@NonNull final SprintEntityDto sprintEntityDto) {
        sprintEntityDtoSink.tryEmitNext(sprintEntityDto);
    }

    public Flux<SprintEntityDto> backlogStateChangedInSprint() {
        return sprintEntityDtoSink.asFlux();
    }
}
