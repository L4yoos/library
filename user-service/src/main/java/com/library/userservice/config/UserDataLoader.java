package com.library.userservice.config;

import com.library.userservice.model.User;
import com.library.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Loading sample user data into the database....");

            User user1 = new User(
                    "Jan", "Kowalski", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "jan.kowalski@example.com", "123456789", "Warszawska 1, Warszawa"
            );

            User user2 = new User(
                    "Anna", "Nowak", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke",  "anna.nowak@example.com", "987654321", "Krakowska 2, Kraków"
            );

            User user3 = new User(
                    "Piotr", "Wiśniewski", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "piotr.wisniewski@example.com", "555111222", "Gdańska 3, Gdańsk"
            );

            List<User> users = Arrays.asList(user1, user2, user3);
            userRepository.saveAll(users);
            System.out.println("Loading of sample user data has been completed.");
        } else {
            System.out.println("The user database already contains records. I omit loading the start-up data.");
        }
    }
}