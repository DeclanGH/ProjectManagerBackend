package io.github.declangh.projectmanagerbackend.common.builder;

import io.github.declangh.projectmanagerbackend.model.dto.BacklogEntityDto;
import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.dto.SprintEntityDto;
import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import io.github.declangh.projectmanagerbackend.model.entity.Group;
import io.github.declangh.projectmanagerbackend.model.entity.Project;
import io.github.declangh.projectmanagerbackend.model.entity.Sprint;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DtoBuilder {

    /**
     * Helper method to build a {@link ProjectMember}
     *
     * @param project {@link Project}
     * @param member {@link User}
     * @return {@link ProjectMember} is both parameters are provided, and null if {@code member} is null
     */
    public static ProjectMember buildProjectMember(@NonNull final Project project, final User member, final Group group) {
        if (member == null) {
            return null;
        }

        Boolean isGroupCreator = (group == null) ? null : group.getCreatorEmail().equals(member.getEmail());
        Boolean isGroupMember = (group == null) ? null : group.getMembers().contains(member);

        return ProjectMember.builder()
                .email(member.getEmail())
                .firstName(member.getFirstName())
                .middleName(member.getMiddleName())
                .lastName(member.getLastName())
                .isOwner(project.getOwners().contains(member))
                .isCreator(project.getCreatorEmail().equals(member.getEmail()))
                // I know the two lines below is a bit hacky, but I intend on creating a GroupMemberDto in future TODO:...
                .isGroupCreator(isGroupCreator)
                .isGroupMember(isGroupMember)
                .build();
    }

    /**
     * Helper method to build a {@link BacklogEntityDto}
     *
     * @param backlog {@link Backlog}
     * @param project {@link Project}
     * @return {@link BacklogEntityDto}
     */
    public static BacklogEntityDto buildBacklogEntityDto(@NonNull final Backlog backlog,
                                                         @NonNull final Project project,
                                                         final Group group) {
        return BacklogEntityDto.builder()
                .id(backlog.getId())
                .name(backlog.getName())
                .description(backlog.getDescription())
                .effort(backlog.getEffort())
                .creator(buildProjectMember(project, backlog.getCreator(), group))
                .dateCreated(backlog.getDateCreated())
                .dateCompleted(backlog.getDateCompleted())
                .isModifiable(backlog.getIsModifiable())
                .state(backlog.getState())
                .assigner(buildProjectMember(project, backlog.getAssigner(), group))
                .assignee(buildProjectMember(project, backlog.getAssignee(), group))
                .group(null) // maybe in the future when backlog can be viewed at project level, this can be relevant
                .sprint(buildSprintEntityDtoWithoutBacklogList(backlog.getSprint()))
                .build();
    }

    public static SprintEntityDto buildSprintEntityDto(@NonNull final Sprint sprint,
                                                       @NonNull final Project project,
                                                       @NonNull final Group group) {
        Set<Backlog> backlogs = sprint.getBacklogs();
        List<BacklogEntityDto> backlogEntityDtosWithoutSprint = new ArrayList<>();
        for (Backlog backlog : backlogs) {
            backlogEntityDtosWithoutSprint.add(buildBacklogEntityDtoWithoutSprint(backlog, project, group));
        }

        return SprintEntityDto.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .isOpen(sprint.isOpen())
                .isDue(sprint.isDue())
                .group(null)
                .backlogs(backlogEntityDtosWithoutSprint)
                .build();
    }

    private static SprintEntityDto buildSprintEntityDtoWithoutBacklogList(final Sprint sprint) {
        if (sprint == null) {
            return null;
        }
        return SprintEntityDto.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .isOpen(sprint.isOpen())
                .isDue(sprint.isDue())
                .group(null)
                .backlogs(null)
                .build();
    }

    private static BacklogEntityDto buildBacklogEntityDtoWithoutSprint(@NonNull final Backlog backlog,
                                                                       @NonNull final Project project,
                                                                       @NonNull final Group group) {
        return BacklogEntityDto.builder()
                .id(backlog.getId())
                .name(backlog.getName())
                .description(backlog.getDescription())
                .effort(backlog.getEffort())
                .creator(buildProjectMember(project, backlog.getCreator(), group))
                .dateCreated(backlog.getDateCreated())
                .dateCompleted(backlog.getDateCompleted())
                .isModifiable(backlog.getIsModifiable())
                .state(backlog.getState())
                .assigner(buildProjectMember(project, backlog.getAssigner(), group))
                .assignee(buildProjectMember(project, backlog.getAssignee(), group))
                .group(null)
                .sprint(null)
                .build();
    }
}
