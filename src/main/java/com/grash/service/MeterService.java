package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.dto.MeterPatchDTO;
import com.grash.dto.MeterShowDTO;
import com.grash.dto.imports.MeterImportDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.MeterMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterService {
    private final MeterRepository meterRepository;
    private final MeterCategoryService meterCategoryService;
    private final FileService fileService;
    private final AssetService assetService;
    private final CompanyService companyService;
    private final MessageSource messageSource;
    private final LocationService locationService;
    private final UserService userService;
    private final EntityManager em;
    private final MeterMapper meterMapper;
    private final NotificationService notificationService;
    private final ReadingService readingService;

    @Transactional
    public Meter create(Meter meter) {
        Meter savedMeter = meterRepository.saveAndFlush(meter);
        em.refresh(savedMeter);
        return savedMeter;
    }

    @Transactional
    public Meter update(Long id, MeterPatchDTO meter) {
        if (meterRepository.existsById(id)) {
            Meter savedMeter = meterRepository.findById(id).get();
            Meter patchedMeter = meterRepository.saveAndFlush(meterMapper.updateMeter(savedMeter, meter));
            em.refresh(patchedMeter);
            return patchedMeter;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Meter> getAll() {
        return meterRepository.findAll();
    }

    public void delete(Long id) {
        meterRepository.deleteById(id);
    }

    public Optional<Meter> findById(Long id) {
        return meterRepository.findById(id);
    }

    public Collection<Meter> findByCompany(Long id) {
        return meterRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, Meter meter) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(meter.getCompanyId());
    }

    public boolean canCreate(OwnUser user, Meter meterReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = meterReq.getCompanyId().equals(companyId);
        boolean second = assetService.isAssetInCompany(meterReq.getAsset(), companyId, false);
        return first && second && canPatch(user, meterMapper.toPatchDto(meterReq));
    }

    public boolean canPatch(OwnUser user, MeterPatchDTO meterReq) {
        Long companyId = user.getCompany().getId();
        //optional fields
        boolean second = meterCategoryService.isMeterCategoryInCompany(meterReq.getMeterCategory(), companyId, true);
        boolean third = fileService.isFileInCompany(meterReq.getImage(), companyId, true);
        boolean fourth = locationService.isLocationInCompany(meterReq.getLocation(), companyId, true);
        boolean fifth = meterReq.getUsers() == null || meterReq.getUsers().stream().allMatch(item ->
                userService.isUserInCompany(item, companyId, false));
        return second && third && fourth && fifth;
    }

    public void notify(Meter meter, Locale locale) {
        String title = messageSource.getMessage("new_assignment", null, locale);
        String message = messageSource.getMessage("notification_meter_assigned", new Object[]{meter.getName()}, locale);
        if (meter.getUsers() != null) {
            notificationService.createMultiple(meter.getUsers().stream().map(assignedUser ->
                    new Notification(message, assignedUser, NotificationType.METER, meter.getId())).collect(Collectors.toList()), true, title);
        }
    }

    public void patchNotify(Meter oldMeter, Meter newMeter, Locale locale) {
        String title = messageSource.getMessage("new_assignment", null, locale);
        String message = messageSource.getMessage("notification_meter_assigned", new Object[]{newMeter.getName()}, locale);
        if (newMeter.getUsers() != null) {
            List<OwnUser> newUsers = newMeter.getUsers().stream().filter(
                    user -> oldMeter.getUsers().stream().noneMatch(user1 -> user1.getId().equals(user.getId()))).collect(Collectors.toList());
            notificationService.createMultiple(newUsers.stream().map(newUser ->
                    new Notification(message, newUser, NotificationType.ASSET, newMeter.getId())).collect(Collectors.toList()), true, title);
        }
    }

    public Collection<Meter> findByAsset(Long id) {
        return meterRepository.findByAsset_Id(id);
    }

    public boolean isMeterInCompany(Meter meter, long companyId, boolean optional) {
        if (optional) {
            Optional<Meter> optionalMeter = meter == null ? Optional.empty() : findById(meter.getId());
            return meter == null || (optionalMeter.isPresent() && optionalMeter.get().getCompanyId().equals(companyId));
        } else {
            Optional<Meter> optionalMeter = findById(meter.getId());
            return optionalMeter.isPresent() && optionalMeter.get().getCompanyId().equals(companyId);
        }
    }

    public Page<MeterShowDTO> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<Meter> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return meterRepository.findAll(builder.build(), page).map(meter -> meterMapper.toShowDto(meter, readingService));
    }

    public void importMeter(Meter meter, MeterImportDTO dto, Company company) {
        Long companyId = company.getId();
        Long companySettingsId = company.getCompanySettings().getId();
        meter.setName(dto.getName());
        meter.setUnit(dto.getUnit());
        meter.setUpdateFrequency(dto.getUpdateFrequency());
        Optional<Location> optionalLocation = locationService.findByNameAndCompany(dto.getLocationName(), companyId);
        optionalLocation.ifPresent(meter::setLocation);
        Optional<MeterCategory> optionalMeterCategory = meterCategoryService.findByNameAndCompanySettings(dto.getMeterCategory(), companySettingsId);
        optionalMeterCategory.ifPresent(meter::setMeterCategory);
        List<OwnUser> users = new ArrayList<>();
        dto.getUsersEmails().forEach(email -> {
            Optional<OwnUser> optionalUser1 = userService.findByEmailAndCompany(email, companyId);
            optionalUser1.ifPresent(users::add);
        });
        meter.setUsers(users);
        meterRepository.save(meter);
    }

    public Optional<Meter> findByIdAndCompany(Long id, Long companyId) {
        return meterRepository.findByIdAndCompanyId(id, companyId);
    }
}
