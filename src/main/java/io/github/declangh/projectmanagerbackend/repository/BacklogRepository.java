package io.github.declangh.projectmanagerbackend.repository;

import io.github.declangh.projectmanagerbackend.model.entity.Backlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Long> {
    List<Backlog> findByGroupId(Long groupId);
}
