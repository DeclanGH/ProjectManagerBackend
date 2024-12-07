package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.common.helper.EntityRetriever;
import io.github.declangh.projectmanagerbackend.model.dto.BacklogEntityDto;
import io.github.declangh.projectmanagerbackend.model.dto.BurndownChartDataDto;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import io.github.declangh.projectmanagerbackend.repository.BacklogRepository;
import io.github.declangh.projectmanagerbackend.repository.GroupRepository;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final BacklogRepository backlogRepository;

    public GroupService(GroupRepository groupRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository,
                        BacklogRepository backlogRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.backlogRepository = backlogRepository;
    }

    @Transactional
    public Group createGroup(@NonNull final String email,
                             @NonNull final String name,
                             @NonNull final Long projectId) {
        User groupCreator = EntityRetriever.getById(userRepository, email);
        Project project = EntityRetriever.getById(projectRepository, projectId);

        if (!project.getMembers().contains(groupCreator)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        Group group = new Group(name, project, email);
        group.addToMemberSet(groupCreator);

        return groupRepository.save(group);
    }

    @Transactional
    public ProjectMember addMemberToGroup(@NonNull final String userEmail,
                                          @NonNull final Long projectId,
                                          @NonNull final Long groupId,
                                          @NonNull final String newGroupMember) {
        User adder = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);
        User newMember = EntityRetriever.getById(userRepository, newGroupMember);

        if (!group.getCreatorEmail().equals(adder.getEmail()) && !project.getCreatorEmail().equals(adder.getEmail())
                && !project.getOwners().contains(adder)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        group.addToMemberSet(newMember);
        groupRepository.save(group);

        return DtoBuilder.buildProjectMember(project, newMember, group);
    }



    @Transactional
    public BurndownChartDataDto getGroupBurndownChartData(@NonNull final String userEmail,
                                                          @NonNull final Long projectId,
                                                          @NonNull final Long groupId) {
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> effortPointsRemaining = new ArrayList<>();
        List<Double> idealEffortPointsRemaining = new ArrayList<>();

        int totalEffort = group.getBacklogs().stream().mapToInt(Backlog::getEffort).sum();
        int durationInWeeks = group.getDuration();
        double effortPointsPerWeek = (double) totalEffort /durationInWeeks;

        LocalDate groupCreateDate = LocalDate.from(group.getDateCreated());

        // necessary for iteration
        LocalDate date1 = groupCreateDate;
        int effortRemaining = totalEffort;

        long weeksBetweenCreateDateAndNow = ChronoUnit.WEEKS.between(groupCreateDate, LocalDate.now());

        for (int i = 0; i < durationInWeeks; i++) {
            labels.add("Week " + (i+1));

            double idealEffortPointRemainingPerWeek = totalEffort - (i * effortPointsPerWeek);
            idealEffortPointsRemaining.add(Math.max(idealEffortPointRemainingPerWeek, 0.0));

            // Either end the loop at the week of the current date or the week of the expected end date, whichever comes first
            if (i <= weeksBetweenCreateDateAndNow) {
                LocalDate date2 = date1.plusWeeks(1);
                effortRemaining -= backlogRepository
                        .getSumOfCompletedEffortsInGroupBetweenDates(List.of(groupId), date1.atStartOfDay(), date2.atStartOfDay())
                        .orElse(0);
                effortPointsRemaining.add(effortRemaining);
                date1 = date2;
            }
        }

        return BurndownChartDataDto.builder()
                .title("Group Burndown Chart")
                .labels(labels)
                .effortPointsRemaining(effortPointsRemaining)
                .idealEffortPointsRemaining(idealEffortPointsRemaining)
                .build();
    }

    @Transactional
    public List<ProjectMember> getGroupMembers(@NonNull final String userEmail,
                                               @NonNull final Long projectId,
                                               @NonNull final Long groupId) {
        User requester = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<ProjectMember> groupMembers = new ArrayList<>();

        for (User member : group.getMembers()) {
            groupMembers.add(DtoBuilder.buildProjectMember(project, member, group));
        }

        return groupMembers;
    }

    @Transactional
    public List<BacklogEntityDto> getNotCompletedAssignedBacklogs(@NonNull final String userEmail,
                                                                  @NonNull final Long projectId,
                                                                  @NonNull final Long groupId) {
        User user = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (!group.getMembers().contains(user)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<BacklogEntityDto> userAssignedBacklogs = new ArrayList<>();


        List<Backlog> userAssignedBacklogsFromDb = backlogRepository.getBacklogsByAssigneeEmailAndGroupIdAndStateIsNot(
                user.getEmail(), group.getId(), BacklogState.COMPLETED);

        for (Backlog backlog : userAssignedBacklogsFromDb) {
            userAssignedBacklogs.add(DtoBuilder.buildBacklogEntityDto(backlog, project, group));
        }

        return userAssignedBacklogs;
    }

    @Transactional
    public boolean deleteGroup(@NonNull final String email,
                               @NonNull final Long projectId,
                               @NonNull final Long groupId) {
        User user = EntityRetriever.getById(userRepository, email);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        Group group = EntityRetriever.getById(groupRepository, groupId);

        if (group.getCreatorEmail().equals(email) || project.getCreatorEmail().equals(email) ||
                project.getOwners().contains(user)) {
            groupRepository.deleteById(groupId);
            return true;
        }
        return false;
    }
}
