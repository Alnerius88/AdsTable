package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.LoginDto;
import ru.skypro.homework.dto.RegisterDto;
import ru.skypro.homework.entities.Role;
import ru.skypro.homework.entities.UserEntity;
import ru.skypro.homework.mappers.UserMapper;
import ru.skypro.homework.repository.RoleRepository;
import ru.skypro.homework.repository.UsersRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
public class AuthController {


    private final AuthenticationManager authManager;
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Operation(
            summary = "Вход пользователя",
            description = "Аутентификация пользователя и выдача токена доступа.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный вход в систему",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))
                    )
            },
            tags = "Объявление"
    )
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.status(HttpStatus.OK).build();
    }


    @Operation(
            summary = "Регистрация пользователя",
            description = "Регтстрация пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный вход в систему",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))
                    )
            },
            tags = "Объявление"
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
        if (usersRepository.existsByUsername(registerDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Optional<Role> role= Optional.empty();

        if (registerDto.getRole()==null || !checkRole(registerDto.getRole())) {
            role = roleRepository.findByName("USER");
        } else {
            role = roleRepository.findByName(registerDto.getRole());
        }

        UserEntity user = userMapper.registerDtoToUserEntity(registerDto);

        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Collections.singletonList(role.get()));
        usersRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private boolean checkRole(String role) {
        for (var element : ru.skypro.homework.dto.Role.values()) {
            if (element.name().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
