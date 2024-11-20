package io.github.declangh.projectmanagerbackend.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
public class SprintEntityDto {
    @NonNull private final Long id;

    @NonNull private final String name;

    @NonNull private final LocalDate startDate;

    @NonNull private final LocalDate endDate;

    @NonNull private final Boolean isOpen;

    @NonNull private final Boolean isDue;

    private final GroupEntityDto group;

    private final List<BacklogEntityDto> backlogs;
}
