package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entities.ImageEntity;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.service.UserService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@CrossOrigin(value = "http://localhost:3000")
@RequiredArgsConstructor
@RequestMapping("users/")
public class UserController {
    private final UserService userService;
    private final ImageRepository imageRepository;
    @Value("${path.avito.image.folder.test}")
    private String pathToTestImage;

    @Operation(
            summary = "Установка пароля",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Установлено",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SetPasswordDto.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователь"
    )
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "set_password")
    public SetPasswordDto setPassword(@RequestBody SetPasswordDto setPasswordDto, @AuthenticationPrincipal UserDetails userDetails) {

        return userService.setPassword(setPasswordDto, userDetails);
    }


    @Operation(
            summary = "Информация о пользователе",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Информация предоставлена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserDto.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователь"
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "me")
    public UserDto infoAboutAuthUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getInfoAboutUser(userDetails);
    }


    @Operation(
            summary = "Изменение информации о пользователе",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Информация обновлена",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UpdateUserDto.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователь"
    )
    @ResponseStatus(HttpStatus.OK)
    @Secured("ROLE_USER")
    @PatchMapping(path = "me")
    public UpdateUserDto updateUserDto(@RequestBody UpdateUserDto updateUserDto, @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateInfoUser(updateUserDto, userDetails);
    }


    @Operation(
            summary = "Обновление аватара пользователя",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Аватар обновлена"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователь"
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateAvatarUser(@RequestParam(name = "image") MultipartFile avatarUser,
                                 @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        log.info("User {} update avatar", "Id user");
        userService.updateAvatar(avatarUser, userDetails);
    }

    @Operation(
            summary = "Обновление аватара пользователя",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Аватар обновлен",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MultipartFile.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true)))
            },
            tags = "Пользователь"
    )
    @GetMapping(path = "id-image/{idImage}")
    public ResponseEntity<byte[]> getAvatarFromHardDrive(@PathVariable Integer idImage) throws IOException {
        ImageEntity image = imageRepository.findById(idImage).get();
        Path path=Path.of(image.getPathHardStore());
        ByteArrayResource resource;

        if (Files.exists(path)) {
            resource=new ByteArrayResource(Files.readAllBytes(path));
        } else {
            path= Paths.get(pathToTestImage);
            resource=new ByteArrayResource(Files.readAllBytes(path));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getMediaType()));
        headers.setContentLength(resource.contentLength());
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource.getByteArray());
    }
}
