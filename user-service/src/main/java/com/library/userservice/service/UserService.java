package com.library.userservice.service;

import com.library.common.dto.UserAuthDTO;
import com.library.userservice.model.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(UUID id);
    User createUser(User user);
    User updateUser(UUID id, User userDetails);
    void deleteUser(UUID id);
    void deactivateUser(UUID id);
    void activateUser(UUID id);
    UserAuthDTO getUserAuthDataByEmail(String email);
}