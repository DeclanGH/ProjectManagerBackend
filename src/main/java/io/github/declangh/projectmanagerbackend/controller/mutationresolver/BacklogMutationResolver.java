package io.github.declangh.projectmanagerbackend.controller.mutationresolver;

import io.github.declangh.projectmanagerbackend.model.dto.BacklogEntityDto;
import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import io.github.declangh.projectmanagerbackend.service.BacklogService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class BacklogMutationResolver {

    private final BacklogService backlogService;

    public BacklogMutationResolver(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @MutationMapping
    public BacklogEntityDto createBacklog(@Argument @NonNull final String userEmail,
                                          @Argument @NonNull final Long projectId,
                                          @Argument @NonNull final Long groupId,
                                          @Argument @NonNull final String backlogName,
                                          @Argument @NonNull final String backlogDescription,
                                          @Argument @NonNull final Integer backlogEffort) {
        return backlogService.createBacklog(userEmail, projectId, groupId, backlogName, backlogDescription, backlogEffort);
    }

    @MutationMapping
    public BacklogEntityDto updateBacklog(@Argument @NonNull final String userEmail,
                                          @Argument @NonNull final Long projectId,
                                          @Argument @NonNull final Long groupId,
                                          @Argument @NonNull final Long backlogId,
                                          @Argument final String assigneeEmail,
                                          @Argument final Long sprintId,
                                          @Argument final BacklogState backlogState){
        return backlogService.updateBacklog(userEmail, projectId, groupId, backlogId, assigneeEmail, sprintId, backlogState);
    }
}
