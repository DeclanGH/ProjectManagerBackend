package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.common.helper.EntityRetriever;
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
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!group.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        LocalDate startDateAsLocalDate = LocalDate.parse(startDate);
        LocalDate endDateAsLocalDate = LocalDate.parse(endDate);

        Sprint sprint = new Sprint(sprintName, startDateAsLocalDate, endDateAsLocalDate, group);
        sprintRepository.save(sprint);

        return DtoBuilder.buildSprintEntityDto(sprint, project, group);
    }

    @Transactional
    public SprintEntityDto getGroupSprint(@NonNull final String userEmail,
                                          @NonNull final Long projectId,
                                          @NonNull final Long groupId,
                                          @NonNull final Long sprintId) {
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);
        Sprint sprint = EntityRetriever.getById(sprintRepository, sprintId);

        sprintRepository.save(sprint);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        return DtoBuilder.buildSprintEntityDto(sprint, project, group);
    }

    @Transactional
    public List<SprintEntityDto> getGroupSprints(@NonNull final String userEmail,
                                                 @NonNull final Long projectId,
                                                 @NonNull final Long groupId) {
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<Sprint> groupSprints = sprintRepository.findByGroupIdOrderByStartDateDesc(groupId);
        List<SprintEntityDto> sprintEntityDtoList = new ArrayList<>();

        for (Sprint sprint : groupSprints) {
            sprint.updateIsDue();
            sprintEntityDtoList.add(DtoBuilder.buildSprintEntityDto(sprint, project, group));
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
        User backlogStateUpdater = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);
        Sprint sprint = EntityRetriever.getById(sprintRepository, sprintId);
        Backlog backlog = EntityRetriever.getById(backlogRepository, backlogId);

        if (group.getMembers().contains(backlogStateUpdater) && backlog.getIsModifiable()) {
            backlog.setState(backlogStateUpdater, backlogState);
            backlogRepository.save(backlog);
            sprintRepository.save(sprint);
            return DtoBuilder.buildSprintEntityDto(sprint, project, group);
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }
    }

    @Transactional
    public SprintEntityDto closeSprint(@NonNull final String userEmail,
                                       @NonNull final Long projectId,
                                       @NonNull final Long groupId,
                                       @NonNull final Long sprintId) {
        User user = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);
        Sprint sprint = EntityRetriever.getById(sprintRepository, sprintId);

        if (group.getMembers().contains(user)) {
            sprint.setOpen(false);
            sprintRepository.save(sprint);
            return DtoBuilder.buildSprintEntityDto(sprint, project, group);
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }
    }
}
