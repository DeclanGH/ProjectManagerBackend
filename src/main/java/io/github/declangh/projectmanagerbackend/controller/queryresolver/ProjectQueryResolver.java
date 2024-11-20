package io.github.declangh.projectmanagerbackend.controller.queryresolver;

import io.github.declangh.projectmanagerbackend.model.dto.BurndownChartDataDto;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectPage;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.service.ProjectService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProjectQueryResolver {

    private final ProjectService projectService;

    public ProjectQueryResolver(ProjectService projectService) {
        this.projectService = projectService;
    }

    @QueryMapping
    public List<Project> getUserProjects(@Argument @NonNull final String userEmail) {
        return projectService.getUserProjects(userEmail);
    }

    @QueryMapping
    public ProjectPage getProjectPage(@Argument @NonNull final Long projectId,
                                      @Argument @NonNull final String userEmail) {
        return projectService.getProjectPage(projectId, userEmail);
    }

    @QueryMapping
    public String getProjectInviteLinkPath(@Argument @NonNull final Long projectId,
                                           @Argument @NonNull final String userEmail) {
        return projectService.generateInviteLinkPath(projectId, userEmail);
    }

    @QueryMapping
    public BurndownChartDataDto getProjectBurndownChartData(@Argument @NonNull final String userEmail,
                                                            @Argument @NonNull final Long projectId) {
        return projectService.getProjectBurndownChartData(userEmail, projectId);
    }
}
