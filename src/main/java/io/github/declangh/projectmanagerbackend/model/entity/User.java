package io.github.declangh.projectmanagerbackend.model.entity;

import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.field.ProjectFieldName;
import io.github.declangh.projectmanagerbackend.common.constant.namingsystem.table.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = TableName.USER)
public class User {

    @Column(nullable = false, unique = true)
    @Id
    private String email;

    @Column(nullable = false)
    @Setter
    private String firstName;

    @Setter
    private String middleName;

    @Setter
    private String lastName;

    @ManyToMany(mappedBy = ProjectFieldName.MEMBERS)
    private Set<Project> projects;

    @ManyToMany(mappedBy = ProjectFieldName.OWNERS)
    private Set<Project> ownedProjects;

    protected User () {
    }

    public User(@NonNull final String email,
                @NonNull final String firstName,
                final String middleName,
                @NonNull final String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.projects = new HashSet<>();
        this.ownedProjects = new HashSet<>();
    }

    public boolean addToProjectSet(@NonNull final Project project) {
        return this.projects.add(project) && project.getMembers().add(this);
    }

    public boolean addToOwnedProjectSet(@NonNull final Project project) {
        return this.ownedProjects.add(project) && project.getOwners().add(this);
    }

    public boolean removeFromProjectSet(@NonNull final Project project) {
        return this.projects.remove(project) && project.getMembers().remove(this);
    }

    public boolean removeFromOwnedProjectSet(@NonNull final Project project) {
        return this.ownedProjects.remove(project) && project.getOwners().remove(this);
    }
}
