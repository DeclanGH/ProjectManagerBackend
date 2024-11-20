package io.github.declangh.projectmanagerbackend.model.entity;

import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.field.BacklogFieldName;
import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.table.TableName;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = TableName.SPRINT)
public class Sprint {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isOpen;

    private boolean isDue;

    @ManyToOne
    private Group group;

    @OneToMany(mappedBy = BacklogFieldName.SPRINT)
    private Set<Backlog> backlogs;

    protected Sprint(){
    }

    public Sprint(@NonNull final String name,
                  @NonNull final LocalDate startDate,
                  @NonNull final LocalDate endDate,
                  @NonNull final Group group) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.group = group;
        this.isOpen = true;
        this.isDue = this.endDate.isBefore(LocalDate.now());
        this.backlogs = new HashSet<>();
    }

    @PrePersist
    @PreUpdate
    private void updateIsDue() {
        this.isDue = this.isOpen && this.endDate != null && this.endDate.isBefore(LocalDate.now());
    }

    public void removeBacklog(@NonNull final Backlog backlog) {
        backlog.removeSprint();
        this.backlogs.remove(backlog);
    }

    public void dissociateBacklogs() {
        for (Backlog backlog : this.backlogs) {
            backlog.removeSprint();
        }
        this.backlogs.clear();
    }
}
