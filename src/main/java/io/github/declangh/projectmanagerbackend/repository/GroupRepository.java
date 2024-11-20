package io.github.declangh.projectmanagerbackend.repository;

import io.github.declangh.projectmanagerbackend.model.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
