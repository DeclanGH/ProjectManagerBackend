package io.github.declangh.projectmanagerbackend.controller.mutationresolver;

import io.github.declangh.projectmanagerbackend.component.SprintEntityDtoPublisher;
import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import io.github.declangh.projectmanagerbackend.service.SprintService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SprintMutationResolver {

    private final SprintService sprintService;
    private final SprintEntityDtoPublisher sprintEntityDtoPublisher;

    public SprintMutationResolver(SprintService sprintService, SprintEntityDtoPublisher sprintEntityDtoPublisher) {
        this.sprintService = sprintService;
        this.sprintEntityDtoPublisher = sprintEntityDtoPublisher;
    }

    @MutationMapping
    public SprintEntityDto createSprint(@Argument @NonNull String userEmail,
                                        @Argument @NonNull Long projectId,
                                        @Argument @NonNull Long groupId,
                                        @Argument @NonNull String sprintName,
                                        @Argument @NonNull String startDate,
                                        @Argument @NonNull String endDate) {
        return sprintService.createSprint(userEmail, projectId, groupId, sprintName, startDate, endDate);
    }

    @MutationMapping
    public SprintEntityDto updateBacklogState(@Argument @NonNull final String userEmail,
                                              @Argument @NonNull final Long projectId,
                                              @Argument @NonNull final Long groupId,
                                              @Argument @NonNull final Long sprintId,
                                              @Argument @NonNull final Long backlogId,
                                              @Argument @NonNull final BacklogState backlogState) {
        SprintEntityDto sprintEntityDto = sprintService.updateBacklogState(
                userEmail, projectId, groupId, sprintId, backlogId, backlogState);
        sprintEntityDtoPublisher.publish(sprintEntityDto);
        return sprintEntityDto;
    }

    @MutationMapping
    public SprintEntityDto closeSprint(@Argument @NonNull final String userEmail,
                                       @Argument @NonNull final Long projectId,
                                       @Argument @NonNull final Long groupId,
                                       @Argument @NonNull final Long sprintId) {
        SprintEntityDto sprintEntityDto = sprintService.closeSprint(userEmail, projectId, groupId, sprintId);
        sprintEntityDtoPublisher.publish(sprintEntityDto);
        return sprintEntityDto;
    }
}
