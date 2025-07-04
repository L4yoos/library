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
            System.out.println("Ładowanie przykładowych danych użytkowników do bazy danych...");

            List<User> users = Arrays.asList(
                    new User("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", "Warszawska 1, Warszawa"),
                    new User("Anna", "Nowak", "anna.nowak@example.com", "987654321", "Krakowska 2, Kraków"),
                    new User("Piotr", "Wiśniewski", "piotr.wisniewski@example.com", "555111222", "Gdańska 3, Gdańsk")
            );
            userRepository.saveAll(users);
            System.out.println("Zakończono ładowanie przykładowych danych użytkowników.");
        } else {
            System.out.println("Baza danych użytkowników już zawiera rekordy. Pomijam ładowanie danych startowych.");
        }
    }
}