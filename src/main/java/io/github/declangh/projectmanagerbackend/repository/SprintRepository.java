package io.github.declangh.projectmanagerbackend.repository;

import io.github.declangh.projectmanagerbackend.model.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByGroupId(Long groupId);
    List<Sprint> findByGroupIdOrderByStartDateDesc(Long groupId);
    //List<Sprint> findSprintByGroup_IdAndIsOpenIsTrue(Long groupId);
}
