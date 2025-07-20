package com.library.userservice.config;

import com.library.userservice.model.User;
import com.library.userservice.model.enums.Role;
import com.library.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserDataLoader.class);

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            logger.info("Loading sample user data into the database....");

            User user1 = new User(
                    "Jan", "Kowalski", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "jan.kowalski@example.com", "123456789", "Warszawska 1, Warszawa"
            );

            Set<Role> roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
            roles.add(Role.ROLE_EDITOR);
            User user2 = new User(
                    "Anna", "Nowak", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke",  "anna.nowak@example.com", "987654321", "Krakowska 2, Kraków"
            );
            user2.setRoles(roles);

            roles.add(Role.ROLE_ADMIN);
            User user3 = new User(
                    "Piotr", "Wiśniewski", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "piotr.wisniewski@example.com", "555111222", "Gdańska 3, Gdańsk"
            );
            user3.setRoles(roles);

            List<User> users = Arrays.asList(user1, user2, user3);
            userRepository.saveAll(users);
            logger.info("Loading of sample user data has been completed. {} users loaded.", users.size());
        } else {
            logger.info("The user database already contains records. Omitting loading the start-up data.");
        }
    }
}