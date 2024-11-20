package io.github.declangh.projectmanagerbackend.common.helper;

import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.Sprint;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.repository.BacklogRepository;
import io.github.declangh.projectmanagerbackend.repository.GroupRepository;
import io.github.declangh.projectmanagerbackend.repository.ProjectRepository;
import io.github.declangh.projectmanagerbackend.repository.SprintRepository;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import lombok.NonNull;

public class EntityRetriever {
    public static User getById(@NonNull final UserRepository repository, @NonNull final String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND_USER));
    }

    public static Project getById(@NonNull final ProjectRepository repository, @NonNull final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND_PROJECT));
    }

    public static Group getById(@NonNull final GroupRepository repository, @NonNull final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND_GROUP));
    }

    public static Sprint getById(@NonNull final SprintRepository repository, @NonNull final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND_SPRINT));
    }

    public static Backlog getById(@NonNull final BacklogRepository repository, @NonNull final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND_BACKLOG));
    }
}
