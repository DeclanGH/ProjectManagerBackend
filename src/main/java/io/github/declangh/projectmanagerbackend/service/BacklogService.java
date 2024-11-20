package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.common.helper.EntityRetriever;
import io.github.declangh.projectmanagerbackend.model.dto.BacklogEntityDto;
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
        User creator = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!project.getCreatorEmail().equals(userEmail) || !project.getOwners().contains(creator) ||
                !group.getMembers().contains(creator)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        Backlog backlog = new Backlog(backlogName, backlogDescription, backlogEffort, creator, group);
        backlogRepository.save(backlog);

        return DtoBuilder.buildBacklogEntityDto(backlog, project, group);
    }

    @Transactional
    public BacklogEntityDto updateBacklog(@NonNull final String userEmail,
                                          @NonNull final Long projectId,
                                          @NonNull final Long groupId,
                                          @NonNull final Long backlogId,
                                          final String assigneeEmail,
                                          final Long sprintId,
                                          final BacklogState backlogState){
        User assigner = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);
        Backlog backlog = EntityRetriever.getById(backlogRepository, backlogId);

        if (group.getMembers().contains(assigner) && backlog.getIsModifiable()) {
            User assignee;
            Sprint sprint;

            if (assigneeEmail != null) {
                assignee = EntityRetriever.getById(userRepository, assigneeEmail);
                backlog.setAssigner(assigner);
                backlog.setAssignee(assignee);
            }

            if (sprintId != null) {
                sprint = EntityRetriever.getById(sprintRepository, sprintId);
                backlog.setSprint(sprint);
            }

            if (backlogState != null) {
                backlog.setState(assigner, backlogState);
            }

            backlogRepository.save(backlog);

            return DtoBuilder.buildBacklogEntityDto(backlog, project, group);
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }
    }

    @Transactional
    public BacklogEntityDto getGroupBacklog(@NonNull final String userEmail,
                                            @NonNull final Long projectId,
                                            @NonNull final Long groupId,
                                            @NonNull final Long backlogId) {
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);
        Backlog backlog = EntityRetriever.getById(backlogRepository, backlogId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        return DtoBuilder.buildBacklogEntityDto(backlog, project, group);
    }

    @Transactional
    public List<BacklogEntityDto> getGroupBacklogs(@NonNull final String userEmail,
                                                   @NonNull final Long projectId,
                                                   @NonNull final Long groupId) {
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<Backlog> backlogs = backlogRepository.findByGroupId(groupId);
        List<BacklogEntityDto> groupBacklogs = new ArrayList<>();

        for (Backlog backlog : backlogs) {
            groupBacklogs.add(DtoBuilder.buildBacklogEntityDto(backlog, project, group));
        }

        return groupBacklogs;
    }
}
