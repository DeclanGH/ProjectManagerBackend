package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.builder.DtoBuilder;
import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.common.helper.EntityRetriever;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.repository.GroupRepository;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;

    public UserService(UserRepository userRepository,
                       ProjectRepository projectRepository,
                       GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public User createOrUpdateUser(@NonNull final String email,
                                   @NonNull final String firstName,
                                   @NonNull final String middleName,
                                   @NonNull final String lastName) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
        } else {
            user = new User(email, firstName, middleName, lastName);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User getUserByEmail(@NonNull final String email) {
        return EntityRetriever.getById(userRepository, email);
    }

    @Transactional
    public ProjectMember getUserDetails(@NonNull final String userEmail,
                                        @NonNull final Long projectId,
                                                 final Long groupId) {
        Group group = null;

        if (groupId != null) {
            group = EntityRetriever.getById(groupRepository, groupId);
        }

        return DtoBuilder.buildProjectMember(
                EntityRetriever.getById(projectRepository, projectId),
                EntityRetriever.getById(userRepository, userEmail),
                group);
    }

    @Deprecated // TODO: Make user account inactive rather.
    @Transactional
    public Boolean deleteUser(@NonNull String email) {
        // it will probably always say true
        try {
            userRepository.deleteById(email);
            return true;
        } catch (Exception e) {
            throw new ProjectManagerException(ProjectMangerStatusCode.INTERNAL_SERVER_ERROR);
        }
    }
}
