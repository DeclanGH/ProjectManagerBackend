package io.github.declangh.projectmanagerbackend.model.dto;

import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class ProjectPage {
    @NonNull private final Long projectId;

    @NonNull private final String projectTitle;

    @NonNull private final String projectDescription;

    @NonNull private final Integer projectDuration;

    @NonNull private LocalDateTime projectCreateDate;

    @NonNull private User projectCreator;

    @NonNull private final List<ProjectMember> projectMembersList;

    @NonNull private final List<Group> projectGroupList;
}
