package io.github.declangh.projectmanagerbackend.controller.subscriptionresolver;

import io.github.declangh.projectmanagerbackend.component.SprintEntityDtoPublisher;
import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class SprintSubscriptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(SprintSubscriptionResolver.class);

    private final SprintEntityDtoPublisher sprintEntityDtoPublisher;

    public SprintSubscriptionResolver(SprintEntityDtoPublisher sprintEntityDtoPublisher) {
        this.sprintEntityDtoPublisher = sprintEntityDtoPublisher;
    }

    @SubscriptionMapping
    public Flux<SprintEntityDto> backlogStateChangedInSprint(@Argument @NonNull Long sprintId) {
        return sprintEntityDtoPublisher.backlogStateChangedInSprint()
                .filter(sprintEntityDto -> sprintEntityDto.getId().equals(sprintId));
    }
}
