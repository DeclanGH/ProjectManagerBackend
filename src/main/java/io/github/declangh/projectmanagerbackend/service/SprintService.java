package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.Sprint;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import io.github.declangh.projectmanagerbackend.repository.BacklogRepository;
import io.github.declangh.projectmanagerbackend.repository.GroupRepository;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.SprintRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SprintService {
    private static final Logger logger = LoggerFactory.getLogger(BacklogService.class);

    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final BacklogRepository backlogRepository;

    public SprintService(SprintRepository sprintRepository,
                         UserRepository userRepository,
                         ProjectRepository projectRepository,
                         GroupRepository groupRepository,
                         BacklogRepository backlogRepository) {
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.groupRepository = groupRepository;
        this.backlogRepository = backlogRepository;
    }

    @Transactional
    public SprintEntityDto createSprint(@NonNull String userEmail,
                                        @NonNull Long projectId,
                                        @NonNull Long groupId,
                                        @NonNull String sprintName,
                                        @NonNull String startDate,
                                        @NonNull String endDate) {
        User requester = getUser(userEmail);
        Project project = getProject(projectId);
        Group group = getGroup(groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        LocalDate startDateAsLocalDate = LocalDate.parse(startDate);
        LocalDate endDateAsLocalDate = LocalDate.parse(endDate);

        Sprint sprint = new Sprint(sprintName, startDateAsLocalDate, endDateAsLocalDate, group);
        sprintRepository.save(sprint);

        return DtoBuilder.buildSprintEntityDto(sprint, project);
    }

    @Transactional
    public SprintEntityDto getGroupSprint(@NonNull final String userEmail,
                                          @NonNull final Long projectId,
                                          @NonNull final Long groupId,
                                          @NonNull final Long sprintId) {
        User requester = getUser(userEmail);
        Project project = getProject(projectId);
        getGroup(groupId);
        Sprint sprint = getSprint(sprintId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        return DtoBuilder.buildSprintEntityDto(sprint, project);
    }

    @Transactional
    public List<SprintEntityDto> getGroupSprints(@NonNull final String userEmail,
                                                 @NonNull final Long projectId,
                                                 @NonNull final Long groupId) {
        User requester = getUser(userEmail);
        Project project = getProject(projectId);
        getGroup(groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<Sprint> groupSprints = sprintRepository.findByGroupIdOrderByStartDateDesc(groupId);
        List<SprintEntityDto> sprintEntityDtoList = new ArrayList<>();

        for (Sprint sprint : groupSprints) {
            sprintEntityDtoList.add(DtoBuilder.buildSprintEntityDto(sprint, project));
        }

        return sprintEntityDtoList;
    }

    @Transactional
    public SprintEntityDto updateBacklogState(@NonNull final String userEmail,
                                              @NonNull final Long projectId,
                                              @NonNull final Long groupId,
                                              @NonNull final Long sprintId,
                                              @NonNull final Long backlogId,
                                              @NonNull final BacklogState backlogState){
        User backlogStateUpdater = getUser(userEmail);
        Project project = getProject(projectId);
        Group group = getGroup(groupId);
        Sprint sprint = getSprint(sprintId);
        Backlog backlog = getBacklog(backlogId);

        if (group.getMembers().contains(backlogStateUpdater) && backlog.getIsModifiable()) {
            backlog.setState(backlogState);
            backlog.updateModifiableState();
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }
        backlogRepository.save(backlog);
        return DtoBuilder.buildSprintEntityDto(sprint, project);
    }

    private User getUser(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Project getProject(final Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Group getGroup(final Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Sprint getSprint(final Long sprintId) {
        return sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Backlog getBacklog(final Long backlogId) {
        return backlogRepository.findById(backlogId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }
}
