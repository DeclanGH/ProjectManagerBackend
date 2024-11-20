package io.github.declangh.projectmanagerbackend.controller.queryresolver;

import io.github.declangh.projectmanagerbackend.model.dto.BacklogEntityDto;
import io.github.declangh.projectmanagerbackend.service.BacklogService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BacklogQueryResolver {

    private final BacklogService backlogService;

    public BacklogQueryResolver(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @QueryMapping
    public BacklogEntityDto getGroupBacklog(@Argument @NonNull final String userEmail,
                                            @Argument @NonNull final Long projectId,
                                            @Argument @NonNull final Long groupId,
                                            @Argument @NonNull final Long backlogId) {
        return backlogService.getGroupBacklog(userEmail, projectId, groupId, backlogId);
    }

    @QueryMapping
    public List<BacklogEntityDto> getGroupBacklogs(@Argument @NonNull final String userEmail,
                                                   @Argument @NonNull final Long projectId,
                                                   @Argument @NonNull final Long groupId) {
        return backlogService.getGroupBacklogs(userEmail, projectId, groupId);
    }
}
