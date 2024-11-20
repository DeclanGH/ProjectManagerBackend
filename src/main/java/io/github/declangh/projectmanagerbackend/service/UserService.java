package io.github.declangh.projectmanagerbackend.service;

import io.github.declangh.projectmanagerbackend.common.constant.statuscodes.ProjectMangerStatusCode;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.model.entity.User;
import io.github.declangh.projectmanagerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createOrUpdateUser(@NonNull String email,
                                   @NonNull String firstName,
                                   @NonNull String middleName,
                                   @NonNull String lastName) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
        } else {
            user = new User(email, firstName, middleName, lastName);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User getUser(@NonNull String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND);
        }
    }

    @Transactional
    public Boolean deleteUser(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ProjectManagerException(ProjectMangerStatusCode.NOT_FOUND));

        try {
            userRepository.deleteById(email);
            return true;
        } catch (Exception e) {
            throw new ProjectManagerException(ProjectMangerStatusCode.INTERNAL_SERVER_ERROR);
        }
    }
}
