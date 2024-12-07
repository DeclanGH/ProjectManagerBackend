package io.github.declangh.projectmanagerbackend.model.entity;

import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.field.BacklogFieldName;
import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.table.TableName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = TableName.GROUP)
public class Group {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private String creatorEmail;

    private LocalDateTime dateCreated;

    private Integer duration;

    @ManyToOne
    private Project project;

    @JoinTable(name = TableName.GROUP_MEMBERS)
    @ManyToMany
    private Set<User> members;

    @OneToMany(
            mappedBy = BacklogFieldName.GROUP,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Backlog> backlogs;

    @OneToMany(
            mappedBy = BacklogFieldName.GROUP,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Sprint> sprints;

    protected Group() {
    }

    public Group(@NonNull String name, @NonNull Project project, @NonNull String creatorEmail) {
        this.name = name;
        this.creatorEmail = creatorEmail;
        this.project = project;
        this.dateCreated = LocalDateTime.now();
        this.members = new HashSet<>();
        this.backlogs = new HashSet<>();
        this.sprints = new HashSet<>();

        long daysBetweenProjectAndGroup = Duration.between(project.getDateCreated(), this.dateCreated).toDays();
        Integer weeksBetweenProjectAndGroup = (int) (daysBetweenProjectAndGroup / 7);
        this.duration = project.getDuration() - weeksBetweenProjectAndGroup; //remainingWeeksTillProjectEnd
    }

    public void addToMemberSet(User user) {
        this.members.add(user);
    }

    public void removeFromMemberSet(User user) {
        this.members.remove(user);
    }
}
