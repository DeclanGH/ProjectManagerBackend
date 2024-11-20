package io.github.declangh.projectmanagerbackend.component;

import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class SprintEntityDtoPublisher {

    private final Sinks.Many<SprintEntityDto> sprintEntityDtoSink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(SprintEntityDto sprintEntityDto) {
        sprintEntityDtoSink.tryEmitNext(sprintEntityDto);
    }

    @Bean
    public Flux<SprintEntityDto> backlogStateChangedInSprint() {
        return sprintEntityDtoSink.asFlux();
    }
}
