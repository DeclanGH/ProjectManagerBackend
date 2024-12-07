package io.github.declangh.projectmanagerbackend.controller.mutationresolver;

import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.service.GroupService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GroupMutationResolver {

    private final GroupService groupService;

    public GroupMutationResolver(GroupService groupService) {
        this.groupService = groupService;
    }

    @MutationMapping
    public Group createGroup(@Argument @NonNull final String email,
                             @Argument @NonNull final String name,
                             @Argument @NonNull final Long projectId) {
        return groupService.createGroup(email, name, projectId);
    }

    @MutationMapping
    public Boolean deleteGroup(@Argument @NonNull final String email,
                               @Argument @NonNull final Long groupId,
                               @Argument @NonNull final Long projectId) {
        return groupService.deleteGroup(email, projectId, groupId);
    }

    @MutationMapping
    public ProjectMember addMemberToGroup(@Argument @NonNull final String userEmail,
                                          @Argument @NonNull final Long projectId,
                                          @Argument @NonNull final Long groupId,
                                          @Argument @NonNull final String newGroupMember) {
        return groupService.addMemberToGroup(userEmail, projectId, groupId, newGroupMember);
    }

}
