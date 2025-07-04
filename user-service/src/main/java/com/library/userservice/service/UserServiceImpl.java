package com.library.userservice.service;

import com.library.userservice.model.User;
import com.library.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        if (user.getEmail() != null && userRepository.findByEmailValue(user.getEmail().getValue()).isPresent()) {
            throw new IllegalArgumentException("Użytkownik o podanym adresie email już istnieje.");
        }

        if (user.getPhoneNumber() != null && userRepository.findByPhoneNumberValue(user.getPhoneNumber().getValue()).isPresent()) {
            throw new IllegalArgumentException("Użytkownik o podanym numerze telefonu już istnieje.");
        }
        user.setRegistrationDate(LocalDate.now());
        user.setActive(true);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(existingUser -> {

            if (userDetails.getEmail() != null &&
                    !userDetails.getEmail().getValue().equals(existingUser.getEmail().getValue())) {
                Optional<User> userWithNewEmail = userRepository.findByEmailValue(userDetails.getEmail().getValue());
                if (userWithNewEmail.isPresent() && !userWithNewEmail.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Nowy adres email jest już zajęty.");
                }
            }

            if (userDetails.getPhoneNumber() != null &&
                    !userDetails.getPhoneNumber().getValue().equals(existingUser.getPhoneNumber().getValue())) {
                Optional<User> userWithNewPhone = userRepository.findByPhoneNumberValue(userDetails.getPhoneNumber().getValue());
                if (userWithNewPhone.isPresent() && !userWithNewPhone.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Nowy numer telefonu jest już zajęty.");
                }
            }

            existingUser.setFirstName(userDetails.getFirstName());
            existingUser.setLastName(userDetails.getLastName());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
            existingUser.setAddress(userDetails.getAddress());
            existingUser.setActive(userDetails.isActive());

            return userRepository.save(existingUser);
        });
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Użytkownik o ID " + id + " nie istnieje.");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> deactivateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(false);
            return userRepository.save(user);
        });
    }

    @Override
    public Optional<User> activateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(true);
            return userRepository.save(user);
        });
    }
}