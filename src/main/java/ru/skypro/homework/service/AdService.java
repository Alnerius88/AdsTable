package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;
import ru.skypro.homework.entities.AdEntity;
import ru.skypro.homework.entities.ImageEntity;
import ru.skypro.homework.entities.UserEntity;
import ru.skypro.homework.exceptions.AdNotDeletedException;
import ru.skypro.homework.exceptions.NoAdException;
import ru.skypro.homework.mappers.AdsMapper;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UsersRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {
    private final AdsRepository adsRepository;
    private final AdsMapper adsMapper;
    private final UsersRepository usersRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    /**
     * @param createOrUpdateAdDto - данные о товаре. title, price, decsription
     * @param file                - загружаемая картинка товара
     * @param userDetails         - данные о том, куда класть объявления берутся из spring security
     * @return - Возвращаем DTO AdDto
     * @throws IOException
     */
    public AdDto adAd(CreateOrUpdateAdDto createOrUpdateAdDto,
                      MultipartFile file,
                      UserDetails userDetails) throws IOException {

        Optional<UserEntity> user = usersRepository.findByUsername(userDetails.getUsername());
        ImageEntity image = imageService.createImageEntityAndSaveBD(file);
        AdEntity ad = new AdEntity();
        adsMapper.updateAdDtoToAdEntity(createOrUpdateAdDto, ad);
        ad.setImageEntity(image);
        ad.setAuthor(user.get());
        adsRepository.save(ad);

        return adsMapper.adEntityToAdDto(ad);
    }

    /**
     * Получение информации об объявлении
     *
     * @param id - идентификатор объявления
     * @return
     */
    public ExtendedAdDto findInfoAboutAd(Integer id) {
        Optional<AdEntity> optionalAd = adsRepository.findById(id);
        if (optionalAd.isPresent()) {
            return adsMapper.adEntityToExAdDto(optionalAd.get());
        } else {
            log.debug("Ad with id = {} not found", id);
            throw new NoAdException("Ad with id =" + id + "not found");
        }
    }

    /**
     * Удаляем выбранное объявление
     *
     * @param id
     */
    public void deleteAdEntity(Integer id) {
        Optional<AdEntity> ad = adsRepository.findById(id);
        if (ad.isPresent()) {

            adsRepository.deleteById(id);
            Optional<AdEntity> checkAd = adsRepository.findById(id);

            if (checkAd.isEmpty()) {
                log.info("Ad with id={}, successfully deleted", id);
            } else {
                log.debug("Ad with id={}, cannot be deleted", id);
                throw new AdNotDeletedException("Не удается удалить объявление");
            }
        } else throw new NoAdException("Ad with id =" + id + "not found");


    }

    /**
     * Обновляем информацию об объявлении
     * @param id - идентификатор объявления
     * @param updateAdDto
     * @return
     */
    public AdDto updateAd(Integer id, CreateOrUpdateAdDto updateAdDto) {
        Optional<AdEntity> optionalAd = adsRepository.findById(id);
        if (optionalAd.isPresent()) {
            adsMapper.updateAdDtoToAdEntity(updateAdDto, optionalAd.get());
            adsRepository.save(optionalAd.get());
            return adsMapper.adEntityToAdDto(optionalAd.get());
        } else{
            log.debug("Ad with id={}, cannot be deleted", id);
            throw new AdNotDeletedException("Не удается удалить объявление");
        }
    }

    /**
     * Находим объявления авторизованного пользователя
     * @param userDetails
     * @return
     */
    @Transactional
    public AdsDto findMyAds(UserDetails userDetails) {
        UserEntity user = usersRepository.findByUsername(userDetails.getUsername()).get();
        List<AdDto> listAdsDto = adsMapper.ListAdToListDto(user.getAdEntityList());
        AdsDto myAdsDto = new AdsDto();
        myAdsDto.setResults((ArrayList<AdDto>) listAdsDto);
        myAdsDto.setCount(listAdsDto.size());
        return myAdsDto;
    }

    /**
     * Возвращаем все объявления, что есть в базе
     * @return
     */
    public AdsDto findAllAds() {
        ArrayList<AdEntity> listAds = adsRepository.findAll();
        List<AdDto> listAdsDto = adsMapper.ListAdToListDto(listAds);
        AdsDto adsDto = new AdsDto();
        adsDto.setResults((ArrayList<AdDto>) listAdsDto);
        adsDto.setCount(listAdsDto.size());
        return adsDto;
    }
}