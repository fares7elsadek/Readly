package com.fares_elsadek.Readly.seeder;

import com.fares_elsadek.Readly.repository.RoleRepository;
import com.fares_elsadek.Readly.repository.UserRepository;
import com.fares_elsadek.Readly.entity.Role;
import com.fares_elsadek.Readly.enums.RoleType;
import com.fares_elsadek.Readly.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class Seeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        if(roleRepository.count() == 0){
            List<Role> roles = new ArrayList<>();
            roles.add(Role.builder()
                    .name(RoleType.ADMIN)
                    .build());

            roles.add(Role.builder()
                    .name(RoleType.USER)
                    .build());

            roleRepository.saveAll(roles);
        }

        if(userRepository.count() == 0){
            userRepository.save(
                    User.builder()
                            .email("fares@readly.com")
                            .enabled(true)
                            .firstname("fares")
                            .lastname("elsadek")
                            .password(passwordEncoder.encode("fares@readly.com"))
                            .roles(List.of(roleRepository.findByName(RoleType.ADMIN).orElseThrow(() ->
                                    new RuntimeException("Something wrong has happened during startup")))).build()
            );
        }
    }
}
