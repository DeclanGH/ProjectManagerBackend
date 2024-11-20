package io.github.declangh.projectmanagerbackend.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ProjectMemberStats {
    @NonNull private final String email;

    @NonNull private final String firstName;

    @NonNull private final String middleName;

    @NonNull private final String lastName;

    @NonNull private final Integer completedBacklogs;
}
