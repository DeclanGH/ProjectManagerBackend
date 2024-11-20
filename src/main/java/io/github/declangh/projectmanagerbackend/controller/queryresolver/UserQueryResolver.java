package io.github.declangh.projectmanagerbackend.controller.queryresolver;

import io.github.declangh.projectmanagerbackend.model.dto.ProjectMember;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.service.UserService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserQueryResolver {

    private final UserService userService;

    public UserQueryResolver(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public User getUserByEmail(@Argument @NonNull String email) {
        return userService.getUserByEmail(email);
    }

    @QueryMapping
    public ProjectMember getUserDetails(@Argument @NonNull String userEmail,
                                        @Argument @NonNull Long projectId,
                                        @Argument          Long groupId) {
        return userService.getUserDetails(userEmail, projectId, groupId);
    }
}
