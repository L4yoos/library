package com.library.userservice.service;

import com.library.common.dto.UserAuthDTO;
import com.library.common.exception.UserNotFoundException;
import com.library.userservice.exception.EmailAlreadyExistsException;
import com.library.userservice.exception.PhoneNumberAlreadyExistsException;
import com.library.userservice.model.User;
import com.library.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        logger.info("Fetching all users.");
        List<User> users = userRepository.findAll();
        logger.debug("Found {} users.", users.size());
        return users;
    }

    @Override
    public User getUserById(UUID id) {
        logger.info("Attempting to fetch user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID: {} not found.", id);
                    return new UserNotFoundException(id);
                });
    }

    @Override
    public User createUser(User user) {
        logger.info("Attempting to create a new user with email: {}", user.getEmail().getValue());
        if (user.getEmail() != null && userRepository.findByEmailValue(user.getEmail().getValue()).isPresent()) {
            logger.warn("Email already exists: {}", user.getEmail().getValue());
            throw new EmailAlreadyExistsException(user.getEmail().getValue());
        }

        if (user.getPhoneNumber() != null && userRepository.findByPhoneNumberValue(user.getPhoneNumber().getValue()).isPresent()) {
            logger.warn("Phone number already exists: {}", user.getPhoneNumber().getValue());
            throw new PhoneNumberAlreadyExistsException(user.getPhoneNumber().getValue());
        }
        user.setActive(true);
        User createdUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", createdUser.getId());
        return createdUser;
    }

    @Override
    public User updateUser(UUID id, User userDetails) {
        logger.info("Attempting to update user with ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID: {} not found for update.", id);
                    return new UserNotFoundException(id);
                });

        if (userDetails.getEmail() != null &&
                !userDetails.getEmail().getValue().equals(existingUser.getEmail().getValue())) {
            Optional<User> userWithNewEmail = userRepository.findByEmailValue(userDetails.getEmail().getValue());
            if (userWithNewEmail.isPresent() && !userWithNewEmail.get().getId().equals(id)) {
                logger.warn("Cannot update user ID: {}. New email {} already exists for user ID: {}", id, userDetails.getEmail().getValue(), userWithNewEmail.get().getId());
                throw new EmailAlreadyExistsException(userDetails.getEmail().getValue());
            }
        }

        if (userDetails.getPhoneNumber() != null &&
                !userDetails.getPhoneNumber().getValue().equals(existingUser.getPhoneNumber().getValue())) {
            Optional<User> userWithNewPhone = userRepository.findByPhoneNumberValue(userDetails.getPhoneNumber().getValue());
            if (userWithNewPhone.isPresent() && !userWithNewPhone.get().getId().equals(id)) {
                logger.warn("Cannot update user ID: {}. New phone number {} already exists for user ID: {}", id, userDetails.getPhoneNumber().getValue(), userWithNewPhone.get().getId());
                throw new PhoneNumberAlreadyExistsException(userDetails.getPhoneNumber().getValue());
            }
        }

        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setAddress(userDetails.getAddress());

        User updatedUser = userRepository.save(existingUser);
        logger.info("User with ID: {} updated successfully.", id);
        return updatedUser;
    }

    @Override
    public void deleteUser(UUID id) {
        logger.info("Attempting to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User with ID: {} not found for deletion.", id);
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        logger.info("User with ID: {} deleted successfully.", id);
    }

    @Override
    public void deactivateUser(UUID id) {
        logger.info("Attempting to deactivate user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID: {} not found for deactivation.", id);
                    return new UserNotFoundException(id);
                });
        user.setActive(false);
        userRepository.save(user);
        logger.info("User with ID: {} deactivated successfully.", id);
    }

    @Override
    public void activateUser(UUID id) {
        logger.info("Attempting to activate user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID: {} not found for activation.", id);
                    return new UserNotFoundException(id);
                });
        user.setActive(true);
        userRepository.save(user);
        logger.info("User with ID: {} activated successfully.", id);
    }

    @Override
    public UserAuthDTO getUserAuthDataByEmail(String email) {
        logger.info("Attempting to fetch user authentication data by email: {}", email);
        return userRepository.findByEmailValue(email)
                .map(user -> {
                    logger.debug("User authentication data found for email: {}", email);
                    return new UserAuthDTO(
                            user.getId(),
                            user.getFirstName().getValue(),
                            user.getLastName().getValue(),
                            user.getEmail().getValue(),
                            user.getPassword().getValue(),
                            user.getRoles().stream()
                                    .map(Enum::name)
                                    .collect(Collectors.toSet())
                    );
                })
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {} for authentication data.", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }
}