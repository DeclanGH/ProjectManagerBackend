package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.common.helper.EntityRetriever;
import io.github.declangh.projectmanagerbackend.model.dto.BurndownChartDataDto;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectPage;
import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.repository.BacklogRepository;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(BacklogService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BacklogRepository backlogRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          BacklogRepository backlogRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.backlogRepository = backlogRepository;
    }

    @Transactional
    public Project createProject(@NonNull final String email,
                                 @NonNull final String name,
                                 @NonNull final String description,
                                 @NonNull final Integer duration) {
        User creator = EntityRetriever.getById(userRepository, email);

        Project newProject = new Project(name, description, duration, email);
        newProject.addToOwnerSet(creator);
        newProject.addToMemberSet(creator);

        return projectRepository.save(newProject);
    }

    @Transactional
    public ProjectPage getProjectPage(@NonNull final Long projectId, @NonNull final String requesterEmail) {
        User requester = EntityRetriever.getById(userRepository, requesterEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<ProjectMember> projectMembers = new ArrayList<>();

        // creator
        User creator = EntityRetriever.getById(userRepository, project.getCreatorEmail());
        ProjectMember projectCreator = ProjectMember.builder()
                .email(creator.getEmail())
                .firstName(creator.getFirstName())
                .middleName(creator.getMiddleName())
                .lastName(creator.getLastName())
                .isCreator(true)
                .isOwner(true)
                .build();
        projectMembers.add(projectCreator);

        // owners
        for (User owner : project.getOwners()) {
            if (owner.getEmail().equals(creator.getEmail())) continue;
            ProjectMember projectOwner = ProjectMember.builder()
                    .email(owner.getEmail())
                    .firstName(owner.getFirstName())
                    .middleName(owner.getMiddleName())
                    .lastName(owner.getLastName())
                    .isCreator(false)
                    .isOwner(true)
                    .build();
            projectMembers.add(projectOwner);
        }

        // members
        for (User member : project.getMembers()) {
            if (project.getOwners().contains(member) || member.getEmail().equals(creator.getEmail())) continue;
            ProjectMember projectMember = ProjectMember.builder()
                    .email(member.getEmail())
                    .firstName(member.getFirstName())
                    .middleName(member.getMiddleName())
                    .lastName(member.getLastName())
                    .isCreator(false)
                    .isOwner(false)
                    .build();
            projectMembers.add(projectMember);
        }

        List<Group> sortedGroupsById = project.getGroups().stream().sorted(Comparator.comparing(Group::getId)).toList();

        return ProjectPage.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .projectDescription(project.getDescription())
                .projectDuration(project.getDuration())
                .projectCreateDate(project.getDateCreated())
                .projectCreator(EntityRetriever.getById(userRepository, project.getCreatorEmail()))
                .projectMembersList(projectMembers)
                .projectGroupList(sortedGroupsById)
                .build();
    }

    @Transactional
    public Boolean deleteProject(@NonNull final Long projectId, @NonNull final String email){
        User deleter = EntityRetriever.getById(userRepository, email);
        String deleterEmail = deleter.getEmail();

        Project project = EntityRetriever.getById(projectRepository, projectId);
        String projectCreatorEmail = project.getCreatorEmail();

        if (!projectCreatorEmail.equals(deleterEmail)) {
            return false;
        }

        projectRepository.deleteById(projectId);
        return true;
    }

    @Deprecated // might revive when I figure out how to add someone in a manner that they decide to join or not
    @Transactional
    public Project addProjectMember(@NonNull final Long projectId,
                                    @NonNull final String adderEmail,
                                    @NonNull final String newMemberEmail) {
        User adder = EntityRetriever.getById(userRepository, adderEmail);
        User newMember = EntityRetriever.getById(userRepository, newMemberEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);

        if (!project.getCreatorEmail().equals(adderEmail) || !project.getOwners().contains(adder)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        project.addToMemberSet(newMember);

        return projectRepository.save(project);
    }

    @Transactional
    public String generateInviteLinkPath(@NonNull final Long projectId,
                                         @NonNull final String userEmail) {
        User user = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);

        if (!project.getCreatorEmail().equals(userEmail) || !project.getOwners().contains(user)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(24);

        project.setInviteToken(token);
        project.setInviteTokenExpirationDate(expirationDate);
        projectRepository.save(project);


        return "/" + projectId + "?token=" + token;
    }

    @Transactional
    public BurndownChartDataDto getProjectBurndownChartData(@NonNull final String userEmail,
                                                            @NonNull final Long projectId) {
        User user = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);

        if (!project.getMembers().contains(user)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> effortPointsRemaining = new ArrayList<>();
        List<Double> idealEffortPointsRemaining = new ArrayList<>();

        int totalEffort = project.getGroups()
                .stream()
                .flatMap(group -> group.getBacklogs().stream())
                .mapToInt(Backlog::getEffort)
                .sum();
        int durationInWeeks = project.getDuration();
        double effortPointsPerWeek = (double) totalEffort /durationInWeeks;

        LocalDate projectCreateDate = LocalDate.from(project.getDateCreated());

        // for iteration
        LocalDate date1 = projectCreateDate;
        int effortRemaining = totalEffort;

        long weeksBetweenCreateDateAndNow = ChronoUnit.WEEKS.between(projectCreateDate, LocalDate.now());

        List<Long> projectIds = project.getGroups().stream()
                .map(Group::getId)
                .toList();

        for (int i = 0; i < durationInWeeks; i++) {
            labels.add("Week " + (i+1));

            double idealEffortPointRemainingPerWeek = totalEffort - (i * effortPointsPerWeek);
            idealEffortPointsRemaining.add(Math.max(idealEffortPointRemainingPerWeek, 0.0));

            if (i <= weeksBetweenCreateDateAndNow) {
                LocalDate date2 = date1.plusWeeks(1);
                effortRemaining -= backlogRepository
                        .getSumOfCompletedEffortsInGroupBetweenDates(projectIds, date1.atStartOfDay(), date2.atStartOfDay())
                        .orElse(0);
                effortPointsRemaining.add(effortRemaining);
                date1 = date2;
            }
        }


        return BurndownChartDataDto.builder()
                .title("Project Burndown Chart")
                .labels(labels)
                .effortPointsRemaining(effortPointsRemaining)
                .idealEffortPointsRemaining(idealEffortPointsRemaining)
                .build();
    }

    @Transactional
    public Project addUserUsingInvite(@NonNull final Long projectId,
                                      @NonNull final String userEmail,
                                      @NonNull final String token) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        User newMember = EntityRetriever.getById(userRepository, userEmail);
        Project project = EntityRetriever.getById(projectRepository, projectId);
        String projectInviteToken = project.getInviteToken();
        LocalDateTime inviteExpirationDate = project.getInviteTokenExpirationDate();

        if (Objects.equals(projectInviteToken, token) && currentDateTime.isBefore(inviteExpirationDate)) {
            if (project.getMembers().contains(newMember)) {
                throw new ProjectManagerException(ProjectMangerStatusCode.ACCEPTED);
            } else {
                project.addToMemberSet(newMember);
            }
            return projectRepository.save(project);
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.BAD_REQUEST);
        }
    }

    @Transactional
    public Boolean removeMember(@NonNull final Long projectId,
                                @NonNull final String deleterEmail,
                                @NonNull final String memberToDeleteEmail) {
        Project project = EntityRetriever.getById(projectRepository,  projectId);
        User deleter = EntityRetriever.getById(userRepository, deleterEmail);
        User memberToDelete = EntityRetriever.getById(userRepository, memberToDeleteEmail);

        String creatorEmail = project.getCreatorEmail();

        if (memberToDelete.getEmail().equals(creatorEmail)) {
            return false;
        } else if (!deleter.getEmail().equals(creatorEmail) || !project.getOwners().contains(deleter)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        project.removeMemberFromProject(memberToDelete);
        projectRepository.save(project);
        return true;
    }

    @Transactional
    public List<Project> getUserProjects(@NonNull final String userEmail) {
        User user = EntityRetriever.getById(userRepository, userEmail);
        return projectRepository.findAllByMembersContaining(user);
    }
}
