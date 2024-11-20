package io.github.declangh.projectmanagerbackend.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class BurndownChartDataDto {
    @NonNull private final String title;

    @NonNull private final List<String> labels;

    @NonNull private final List<Integer> effortPointsRemaining;

    @NonNull private final List<Double> idealEffortPointsRemaining;
}
