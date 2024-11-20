package io.github.declangh.projectmanagerbackend.controller.queryresolver;

import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import io.github.declangh.projectmanagerbackend.model.entity.Sprint;
import io.github.declangh.projectmanagerbackend.service.SprintService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SprintQueryResolver {

    private final SprintService sprintService;

    public SprintQueryResolver(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @QueryMapping
    public SprintEntityDto getGroupSprint(@Argument @NonNull final String userEmail,
                                          @Argument @NonNull final Long projectId,
                                          @Argument @NonNull final Long groupId,
                                          @Argument @NonNull final Long sprintId) {
        return sprintService.getGroupSprint(userEmail, projectId, groupId, sprintId);
    }

    @QueryMapping
    public List<SprintEntityDto> getGroupSprints(@Argument @NonNull final String userEmail,
                                                 @Argument @NonNull final Long projectId,
                                                 @Argument @NonNull final Long groupId) {
        return sprintService.getGroupSprints(userEmail, projectId, groupId);
    }
}
