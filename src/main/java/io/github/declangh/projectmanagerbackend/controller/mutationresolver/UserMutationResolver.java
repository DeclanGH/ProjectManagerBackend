package io.github.declangh.projectmanagerbackend.controller.mutationresolver;

import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.service.UserService;
import lombok.NonNull;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserMutationResolver {

    private final UserService userService;

    public UserMutationResolver(UserService userService) {
        this.userService = userService;
    }

    @MutationMapping
    public User createOrUpdateUser(@Argument @NonNull final String email,
                           @Argument @NonNull final String firstName,
                           @Argument @NonNull final String middleName,
                           @Argument @NonNull final String lastName) {
        return userService.createOrUpdateUser(email, firstName, middleName, lastName);
    }

    @Deprecated // same idea, i want to deactivate accounts, not delete them
    @MutationMapping
    public Boolean deleteUser(@Argument @NonNull final String email) {
        return userService.deleteUser(email);
    }
}
