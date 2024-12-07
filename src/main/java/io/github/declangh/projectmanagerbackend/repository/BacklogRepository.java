package io.github.declangh.projectmanagerbackend.repository;

import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Long> {
    List<Backlog> findByGroupIdOrderByDateCreatedDesc(Long groupId);

    @Query("SELECT SUM(b.effort) FROM Backlog b " +
            "WHERE b.group.id IN :groupIds " +
            "AND b.dateCompleted IS NOT NULL " +
            "AND b.dateCompleted BETWEEN :date1 AND :date2")
    Optional<Integer> getSumOfCompletedEffortsInGroupBetweenDates(
            @NonNull @Param("groupIds") List<Long> groupIds,
            @NonNull @Param("date1") LocalDateTime date1,
            @NonNull @Param("date2") LocalDateTime date2
    );

    List<Backlog> getBacklogsByAssigneeEmailAndGroupIdAndStateIsNot(String assigneeEmail, Long groupId, BacklogState state);
}
