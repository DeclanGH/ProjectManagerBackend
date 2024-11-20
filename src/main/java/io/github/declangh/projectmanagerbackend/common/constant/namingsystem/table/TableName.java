package io.github.declangh.projectmanagerbackend.common.constant.namingsystem.table;

public class TableName {
    // Main tables/entities (naming conflict with "User" and "Group", using plural forms to be safe)
    public static final String USER = "Users";
    public static final String PROJECT = "Projects";
    public static final String GROUP = "Groups";
    public static final String SPRINT = "Sprints";
    public static final String BACKLOG = "Backlogs";

    // Join tables
    public static final String PROJECT_OWNERS = "ProjectOwners";
    public static final String PROJECT_MEMBERS = "ProjectMembers";
    public static final String GROUP_MEMBERS = "GroupMembers";
}
