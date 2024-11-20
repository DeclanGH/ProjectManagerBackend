package io.github.declangh.projectmanagerbackend.controller.queryresolver;

import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.service.GroupService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GroupQueryResolver {

    private final GroupService groupService;

    public GroupQueryResolver(GroupService groupService) {
        this.groupService = groupService;
    }

    @QueryMapping
    public List<ProjectMember> getGroupMembers(@Argument @NonNull final String userEmail,
                                               @Argument @NonNull final Long projectId,
                                               @Argument @NonNull final Long groupId) {
        return groupService.getGroupMembers(userEmail, projectId, groupId);
    }
}
