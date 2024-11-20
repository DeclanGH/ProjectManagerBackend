package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.model.dto.BacklogEntityDto;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BacklogService {
    private static final Logger logger = LoggerFactory.getLogger(BacklogService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final BacklogRepository backlogRepository;
    private final SprintRepository sprintRepository;

    public BacklogService(GroupRepository groupRepository,
                          UserRepository userRepository,
                          ProjectRepository projectRepository,
                          BacklogRepository backlogRepository,
                          SprintRepository sprintRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.backlogRepository = backlogRepository;
        this.sprintRepository = sprintRepository;
    }

    @Transactional
    public BacklogEntityDto createBacklog(@NonNull final String userEmail,
                                    @NonNull final Long projectId,
                                    @NonNull final Long groupId,
                                    @NonNull final String backlogName,
                                    @NonNull final String backlogDescription,
                                    @NonNull final Integer backlogEffort) {
        User creator = getUser(userEmail);
        Project project = getProject(projectId);
        Group group = getGroup(groupId);

        if (!project.getCreatorEmail().equals(userEmail) || !project.getOwners().contains(creator) ||
                !group.getMembers().contains(creator)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        Backlog backlog = new Backlog(backlogName, backlogDescription, backlogEffort, creator, group);
        backlogRepository.save(backlog);

        return DtoBuilder.buildBacklogEntityDto(backlog, project);
    }

    @Transactional
    public BacklogEntityDto updateBacklog(@NonNull final String userEmail,
                                    @NonNull final Long projectId,
                                    @NonNull final Long groupId,
                                    @NonNull final Long backlogId,
                                    final String assigneeEmail,
                                    final Long sprintId,
                                    final BacklogState backlogState){
        User assigner = getUser(userEmail);
        Project project = getProject(projectId);
        Group group = getGroup(groupId);
        Backlog backlog = getBacklog(backlogId);

        if (group.getMembers().contains(assigner) && backlog.getIsModifiable()) {
            User assignee;
            Sprint sprint;

            if (assigneeEmail != null) {
                assignee = getUser(assigneeEmail);
                backlog.setAssigner(assigner);
                backlog.setAssignee(assignee);
            }
            if (sprintId != null) {
                sprint = getSprint(sprintId);
                backlog.setSprint(sprint);
            }

            if (backlogState != null) {
                backlog.setState(backlogState);
                if (backlog.getState().equals(BacklogState.COMPLETED)) {
                    backlog.setDateCompleted(LocalDateTime.now());
                    if (backlog.getAssigner() == null) backlog.setAssigner(assigner);
                    if (backlog.getAssignee() == null) backlog.setAssignee(assigner); // yes
                }
            }

            backlog.updateModifiableState();
            backlogRepository.save(backlog);

            return DtoBuilder.buildBacklogEntityDto(backlog, project);
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }
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

    @Transactional
    public BacklogEntityDto getGroupBacklog(@NonNull final String userEmail,
                                            @NonNull final Long projectId,
                                            @NonNull final Long groupId,
                                            @NonNull final Long backlogId) {
        User requester = getUser(userEmail);
        Project project = getProject(projectId);
        getGroup(groupId); // check if group exist
        Backlog backlog = getBacklog(backlogId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        return DtoBuilder.buildBacklogEntityDto(backlog, project);
    }

    @Transactional
    public List<BacklogEntityDto> getGroupBacklogs(@NonNull final String userEmail,
                                                   @NonNull final Long projectId,
                                                   @NonNull final Long groupId) {
        User requester = getUser(userEmail);
        Project project = getProject(projectId);
        getGroup(groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<Backlog> backlogs = backlogRepository.findByGroupId(groupId);
        List<BacklogEntityDto> groupBacklogs = new ArrayList<>();

        for (Backlog backlog : backlogs) {
            groupBacklogs.add(DtoBuilder.buildBacklogEntityDto(backlog, project));
        }

        return groupBacklogs;
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

    private Backlog getBacklog(final Long backlogId) {
        return backlogRepository.findById(backlogId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Sprint getSprint(final Long sprint) {
        return sprintRepository.findById(sprint)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }
}
