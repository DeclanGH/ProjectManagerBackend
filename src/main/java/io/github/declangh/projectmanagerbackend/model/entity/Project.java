package io.github.declangh.projectmanagerbackend.model.entity;

import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.field.GroupFieldName;
import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.table.TableName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = TableName.PROJECT)
public class Project {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Setter
    private String title;

    @Setter
    private String description;

    private String creatorEmail;

    private LocalDateTime dateCreated;

    @Setter
    private Integer duration;

    @Setter
    private String inviteToken;

    @Setter
    private LocalDateTime inviteTokenExpirationDate;

    @JoinTable(name = TableName.PROJECT_OWNERS)
    @ManyToMany
    private Set<User> owners;

    @JoinTable(name = TableName.PROJECT_MEMBERS)
    @ManyToMany
    private Set<User> members;

    @OneToMany(
            mappedBy = GroupFieldName.PROJECT,
            cascade = CascadeType.ALL)
    private Set<Group> groups;

    protected Project(){
    }

    public Project(@NonNull final String title,
                   @NonNull final String description,
                   @NonNull final Integer duration,
                   @NonNull final String creatorEmail) {
        this.title = title;
        this.description = description;
        this.creatorEmail = creatorEmail;
        this.dateCreated = LocalDateTime.now();
        this.duration = duration;
        this.owners = new HashSet<>();
        this.members = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public void addToOwnerSet(@NonNull final User user) {
        this.owners.add(user);
        user.getOwnedProjects().add(this);
    }

    public void addToMemberSet(@NonNull final User user) {
        this.members.add(user);
        user.getProjects().add(this);
    }

    public void removeMemberFromProject(@NonNull final User user) {
        this.removeFromOwnerSet(user);
        this.removeFromMemberSet(user);

        for (Group group : this.getGroups()) {
            group.removeFromMemberSet(user);
        }
    }

    public void removeFromOwnerSet(@NonNull final User user) {
        this.owners.remove(user);
        user.getOwnedProjects().remove(this);
    }

    public void removeFromMemberSet(@NonNull final User user) {
        this.members.remove(user);
        user.getProjects().remove(this);
    }

    public boolean removeFromGroupSet(@NonNull final Group group) {
        return this.getGroups().remove(group);
    }
}
