package io.github.declangh.projectmanagerbackend.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
@Setter
public class GroupEntityDto {
    @NonNull private final Long id;

    @NonNull private final String name;
}
