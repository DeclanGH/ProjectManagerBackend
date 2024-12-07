package io.github.declangh.projectmanagerbackend.controller.mutationresolver;

import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.service.ProjectService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProjectMutationResolver {

    private final ProjectService projectService;

    public ProjectMutationResolver(ProjectService projectService) {
        this.projectService = projectService;
    }

    @MutationMapping
    public Project createProject(@Argument @NonNull final String email,
                                 @Argument @NonNull final String title,
                                 @Argument @NonNull final String description,
                                 @Argument @NonNull final Integer duration) {
        return projectService.createProject(email, title, description, duration);
    }

    @MutationMapping
    public Boolean deleteProject(@Argument @NonNull final Long projectId,
                                 @Argument @NonNull final String email) {
        return projectService.deleteProject(projectId, email);
    }

    @MutationMapping
    public Project addMemberToProjectUsingInvite(@Argument @NonNull final Long projectId,
                                                 @Argument @NonNull final String userEmail,
                                                 @Argument @NonNull final String token) {
        return projectService.addUserUsingInvite(projectId, userEmail, token);
    }

    @MutationMapping
    public ProjectMember promoteMemberToOwner(@Argument @NonNull final String userEmail,
                                              @Argument @NonNull final String memberEmail,
                                              @Argument @NonNull final Long projectId) {
        return projectService.promoteMemberToOwner(userEmail, memberEmail, projectId);
    }

    @MutationMapping
    public ProjectMember demoteOwnerToMember(@Argument @NonNull final String userEmail,
                                             @Argument @NonNull final String memberEmail,
                                             @Argument @NonNull final Long projectId) {
        return projectService.demoteOwnerToMember(userEmail, memberEmail, projectId);
    }

    @MutationMapping
    public Boolean removeMemberFromProject(@Argument @NonNull final Long projectId,
                                           @Argument @NonNull final String deleterEmail,
                                           @Argument @NonNull final String memberToDeleteEmail) {
        return projectService.removeMember(projectId, deleterEmail, memberToDeleteEmail);
    }
}
