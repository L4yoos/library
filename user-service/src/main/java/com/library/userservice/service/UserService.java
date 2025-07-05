package com.library.userservice.service;

import com.library.userservice.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(UUID id);
    User createUser(User user);
    Optional<User> updateUser(UUID id, User userDetails);
    void deleteUser(UUID id);
    void deactivateUser(UUID id);
    void activateUser(UUID id);
}