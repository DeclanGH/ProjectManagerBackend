package io.github.declangh.projectmanagerbackend.controller.subscriptionresolver;

import io.github.declangh.projectmanagerbackend.component.SprintEntityDtoPublisher;
import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class SprintSubscriptionResolver {

    private final SprintEntityDtoPublisher sprintEntityDtoPublisher;

    public SprintSubscriptionResolver(SprintEntityDtoPublisher sprintEntityDtoPublisher) {
        this.sprintEntityDtoPublisher = sprintEntityDtoPublisher;
    }

    @SubscriptionMapping
    public Flux<SprintEntityDto> backlogStateChangedInSprint() {
        return sprintEntityDtoPublisher.backlogStateChangedInSprint();
    }
}
