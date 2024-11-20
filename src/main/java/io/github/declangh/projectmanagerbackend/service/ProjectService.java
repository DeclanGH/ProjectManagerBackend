package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectPage;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(BacklogService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Project createProject(@NonNull final String email,
                                 @NonNull final String name,
                                 @NonNull final String description,
                                 @NonNull final Integer duration) {
        User creator = getUser(email);

        Project newProject = new Project(name, description, duration, email);
        newProject.addToOwnerSet(creator);
        newProject.addToMemberSet(creator);

        return projectRepository.save(newProject);
    }

    @Transactional
    public ProjectPage getProjectPage(@NonNull final Long projectId, @NonNull final String requesterEmail) {
        User requester = getUser(requesterEmail);
        Project project = getProject(projectId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<ProjectMember> projectMembers = new ArrayList<>();

        // creator
        User creator = getUser(project.getCreatorEmail());
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
                .projectCreator(getUser(project.getCreatorEmail()))
                .projectMembersList(projectMembers)
                .projectGroupList(sortedGroupsById)
                .build();
    }

    @Transactional
    public Boolean deleteProject(@NonNull final Long projectId, @NonNull final String email){
        User deleter = getUser(email);
        String deleterEmail = deleter.getEmail();

        Project project = getProject(projectId);
        String projectCreatorEmail = project.getCreatorEmail();

        if (!projectCreatorEmail.equals(deleterEmail)) {
            return false;
        }

        projectRepository.deleteById(projectId);
        return true;
    }

    @Deprecated
    @Transactional
    public Project addProjectMember(@NonNull final Long projectId,
                                    @NonNull final String adderEmail,
                                    @NonNull final String newMemberEmail) {
        User adder = getUser(adderEmail);
        User newMember = getUser(newMemberEmail);
        Project project = getProject(projectId);

        if (!project.getCreatorEmail().equals(adderEmail) || !project.getOwners().contains(adder)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        project.addToMemberSet(newMember);

        return projectRepository.save(project);
    }

    @Transactional
    public String generateInviteLinkPath(@NonNull final Long projectId,
                                         @NonNull final String userEmail) {
        User user = getUser(userEmail);
        Project project = getProject(projectId);

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
    public Project addUserUsingInvite(@NonNull final Long projectId,
                                      @NonNull final String userEmail,
                                      @NonNull final String token) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        User newMember = getUser(userEmail);
        Project project = getProject(projectId);
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
        Project project = getProject(projectId);
        User deleter = getUser(deleterEmail);
        User memberToDelete = getUser(memberToDeleteEmail);

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
        User user = getUser(userEmail);
        return projectRepository.findAllByMembersContaining(user);
    }

    // private helper methods - I expect them to be used frequently
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }
}
