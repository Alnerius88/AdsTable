package ru.skypro.homework;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.skypro.homework.controller.AuthController;
import ru.skypro.homework.controller.UserController;
import ru.skypro.homework.dto.LoginDto;
import ru.skypro.homework.dto.RegisterDto;
import ru.skypro.homework.dto.SetPasswordDto;
import ru.skypro.homework.repository.RoleRepository;
import ru.skypro.homework.repository.UsersRepository;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUserController {

    @LocalServerPort
    private int port;

    @Autowired
    private UserController userController;

    @Autowired
    private AuthController authController;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    String registerPath;
    String loginPath;
    String setPasswordPath;

    @BeforeEach
    void init() {
        registerPath = "http://localhost:" + port + "/register";
        loginPath = "http://localhost:" + port + "/login";
        setPasswordPath = "http://localhost:" + port + "/set_password";
    }


    @Test
    @Order(1)
    void contextLoad() {
        assertThat(userController).isNotNull();
    }

    @Test
    @Order(2)
    void fillDbFakeData() {
        String testName = "super_user@gmail.com";
        RegisterDto registerDtoUser = RegisterDto.builder().
                username(testName).
                firstName("Евгений").
                lastName("Белых").
                password("123123123").
                role("USER").build();

        ResponseEntity<?> response =
                restTemplate.
                        postForEntity(registerPath, registerDtoUser, ResponseEntity.class);

        if (usersRepository.existsByUsername(testName)) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } else assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }

    @Test
    void login(){
        LoginDto loginDto=new LoginDto("user@gmail.com","123123123");
        ResponseEntity<?> responseEntity = restTemplate.postForObject(loginPath, loginDto, ResponseEntity.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    }


    @Test
    @Order(3)
    @Transactional
    @Rollback(value = false)
    void setPassword() {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "super_user@gmail.com",
                        "123123123"));
        SecurityContextHolder.getContext().setAuthentication(authentication);


        SetPasswordDto setPasswordDto = new SetPasswordDto();
        setPasswordDto.setNewPassword("1");
        setPasswordDto.setCurrentPassword("123123123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("super_user@gmail.com", "123123123");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<SetPasswordDto> httpEntity = new HttpEntity<>(setPasswordDto, headers);

        ClientHttpRequestFactory requestFactory = new
                HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());

        RestTemplate restTemplate2 = new RestTemplate();
        restTemplate2.setRequestFactory(requestFactory);
        restTemplate2.setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR;
            }
        });

        SetPasswordDto responseSetDto =
                restTemplate.postForObject(setPasswordPath, httpEntity, SetPasswordDto.class);

        log.info(responseSetDto.toString());

        assertThat(responseSetDto.getNewPassword()).isEqualTo("1");
    }

}




