package io.github.declangh.projectmanagerbackend.model.dto;

import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class BacklogEntityDto {
    @NonNull
    private final Long id;

    @NonNull private final String name;

    @NonNull private final String description;

    @NonNull private final Integer effort;

    @NonNull private final ProjectMember creator;

    @NonNull private final LocalDateTime dateCreated;

    private final LocalDateTime dateCompleted;

    @NonNull private final Boolean isModifiable;

    @NonNull private final BacklogState state;

    private final ProjectMember assigner;

    private final ProjectMember assignee;

    private final GroupEntityDto group;

    private final SprintEntityDto sprint;
}
