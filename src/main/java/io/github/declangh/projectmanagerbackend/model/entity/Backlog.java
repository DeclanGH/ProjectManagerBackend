package io.github.declangh.projectmanagerbackend.model.entity;

import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.table.TableName;
import io.github.declangh.projectmanagerbackend.model.enumeration.BacklogState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = TableName.BACKLOG)
public class Backlog {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer effort;

    @ManyToOne
    private User creator;

    private LocalDateTime dateCreated;

    @Setter
    private LocalDateTime dateCompleted;

    private Boolean isModifiable;

    private BacklogState state;

    @ManyToOne
    @Setter
    private User assigner;

    @ManyToOne
    @Setter
    private User assignee;

    @ManyToOne
    private Group group;

    @ManyToOne
    private Sprint sprint;

    protected Backlog(){
    }

    public Backlog(@NonNull final String name,
                   @NonNull final String description,
                   @NonNull final Integer effort,
                   @NonNull final User creator,
                   @NonNull final Group group) {
        this.name = name;
        this.description = description;
        this.effort = effort;
        this.creator = creator;
        this.group = group;
        this.dateCreated = LocalDateTime.now();
        this.state = BacklogState.NOT_STARTED;
        this.isModifiable = true;
        this.assigner = null;
        this.assignee = null;
        this.dateCompleted = null;
    }

    /**
    Used to update the field that checks whether a backlog is open/closed to modification.
    If attached to a sprint, the modifiable state of the backlog is controlled by the sprint (either being open
    or close), but if it isn't attached to a sprint and is marked as completed, then the state is irreversible,
    as the backlog becomes closed to modification.
     */
    @PrePersist
    @PreUpdate
    protected void updateModifiableState() {
        if (this.sprint == null) {
            this.isModifiable = this.state != BacklogState.COMPLETED;
        } else {
            this.isModifiable = this.sprint.isOpen() || this.state != BacklogState.COMPLETED;
        }
    }

    public void setState(@NonNull final User setter, @NonNull final BacklogState backlogState) {
        this.state = backlogState;

        if (backlogState == BacklogState.COMPLETED) {
            this.dateCompleted = LocalDateTime.now();
            if (this.assignee == null) {
                this.assignee = setter;
                this.assigner = setter;
            }
        } else {
            this.dateCompleted = null;
        }

        this.updateModifiableState();
    }

    public void setSprint(@NonNull final Sprint sprint) {
        if (this.sprint != null) {
            this.sprint.removeBacklog(this);
        }
        this.sprint = sprint;
        this.sprint.getBacklogs().add(this);
    }

    protected void removeSprint() {
        this.sprint = null;
    }
}
