package com.library.userservice.service;

import com.library.common.dto.UserAuthDTO;
import com.library.common.exception.UserNotFoundException;
import com.library.userservice.exception.EmailAlreadyExistsException;
import com.library.userservice.exception.PhoneNumberAlreadyExistsException;
import com.library.userservice.model.User;
import com.library.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User createUser(User user) {
        if (user.getEmail() != null && userRepository.findByEmailValue(user.getEmail().getValue()).isPresent()) {
            throw new EmailAlreadyExistsException(user.getEmail().getValue());
        }

        if (user.getPhoneNumber() != null && userRepository.findByPhoneNumberValue(user.getPhoneNumber().getValue()).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(user.getPhoneNumber().getValue());
        }
        user.setActive(true);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(UUID id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userDetails.getEmail() != null &&
                !userDetails.getEmail().getValue().equals(existingUser.getEmail().getValue())) {
            Optional<User> userWithNewEmail = userRepository.findByEmailValue(userDetails.getEmail().getValue());
            if (userWithNewEmail.isPresent() && !userWithNewEmail.get().getId().equals(id)) {
                throw new EmailAlreadyExistsException(userDetails.getEmail().getValue());
            }
        }

        if (userDetails.getPhoneNumber() != null &&
                !userDetails.getPhoneNumber().getValue().equals(existingUser.getPhoneNumber().getValue())) {
            Optional<User> userWithNewPhone = userRepository.findByPhoneNumberValue(userDetails.getPhoneNumber().getValue());
            if (userWithNewPhone.isPresent() && !userWithNewPhone.get().getId().equals(id)) {
                throw new PhoneNumberAlreadyExistsException(userDetails.getPhoneNumber().getValue());
            }
        }

        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setAddress(userDetails.getAddress());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public UserAuthDTO getUserAuthDataByEmail(String email) {
        return userRepository.findByEmailValue(email)
                .map(user -> new UserAuthDTO(
                        user.getId(),
                        user.getFirstName().getValue(),
                        user.getLastName().getValue(),
                        user.getEmail().getValue(),
                        user.getPassword().getValue()
                ))
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}