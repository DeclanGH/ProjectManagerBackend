package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.repository.GroupRepository;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public GroupService(GroupRepository groupRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Group createGroup(@NonNull final String email,
                             @NonNull final String name,
                             @NonNull final Long projectId) {
        User groupCreator = getUser(email);
        Project project = getProject(projectId);

        if (!project.getMembers().contains(groupCreator)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        Group group = new Group(name, project, email);
        long daysBetweenProjectAndGroup = Duration.between(project.getDateCreated(), group.getDateCreated()).toDays();
        Integer weeksBetweenProjectAndGroup = (int) (daysBetweenProjectAndGroup / 7);
        Integer remainingWeeksTillProjectEnd = project.getDuration() - weeksBetweenProjectAndGroup;
        group.setDuration(remainingWeeksTillProjectEnd);
        group.addToMemberSet(groupCreator);

        return groupRepository.save(group);
    }

    @Transactional
    public Group addMemberToGroup(@NonNull final Long groupId, @NonNull final Long userId) {
        return null;
    }

    @Transactional
    public List<ProjectMember> getGroupMembers(@NonNull final String userEmail,
                                               @NonNull final Long projectId,
                                               @NonNull final Long groupId) {
        User requester = getUser(userEmail);
        Project project = getProject(projectId);
        Group group = getGroup(groupId);

        if (!project.getMembers().contains(requester)) {
            throw new ProjectManagerException(ProjectMangerStatusCode.FORBIDDEN);
        }

        List<ProjectMember> groupMembers = new ArrayList<>();

        for (User member : group.getMembers()) {
            groupMembers.add(DtoBuilder.buildProjectMember(project, member));
        }

        return groupMembers;
    }

    @Transactional
    public boolean deleteGroup(@NonNull final String email,
                               @NonNull final Long projectId,
                               @NonNull final Long groupId) {
        User user = getUser(email);
        Project project = getProject(projectId);
        Group group = getGroup(groupId);
        getGroup(groupId);

        if (group.getCreatorEmail().equals(email) || project.getCreatorEmail().equals(email) ||
                project.getOwners().contains(user)) {
            groupRepository.deleteById(groupId);
            return true;
        }
        return false;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }

    private Group getGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));
    }
}
