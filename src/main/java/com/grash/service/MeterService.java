package com.grash.service;

import com.grash.dto.MeterPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.MeterMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterService {
    private final MeterRepository meterRepository;
    private final MeterCategoryService meterCategoryService;
    private final FileService fileService;
    private final AssetService assetService;
    private final CompanyService companyService;
    private final LocationService locationService;
    private final EntityManager em;
    private final MeterMapper meterMapper;
    private final NotificationService notificationService;

    public Meter create(Meter Meter) {
        return meterRepository.save(Meter);
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
        return meterRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, Meter meter) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(meter.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Meter meterReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(meterReq.getCompany().getId());
        Optional<Asset> optionalAsset = assetService.findById(meterReq.getAsset().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);
        boolean second = optionalAsset.isPresent() && optionalAsset.get().getCompany().getId().equals(companyId);

        return first && second && canPatch(user, meterMapper.toDto(meterReq));
    }

    public boolean canPatch(OwnUser user, MeterPatchDTO meterReq) {
        Long companyId = user.getCompany().getId();

        Optional<MeterCategory> optionalMeterCategory = meterReq.getMeterCategory() == null ? Optional.empty() : meterCategoryService.findById(meterReq.getMeterCategory().getId());
        Optional<File> optionalImage = meterReq.getImage() == null ? Optional.empty() : fileService.findById(meterReq.getImage().getId());
        Optional<Location> optionalLocation = meterReq.getLocation() == null ? Optional.empty() : locationService.findById(meterReq.getLocation().getId());

        //optional fields
        boolean second = meterReq.getMeterCategory() == null || (optionalMeterCategory.isPresent() && optionalMeterCategory.get().getCompanySettings().getCompany().getId().equals(companyId));
        boolean third = meterReq.getImage() == null || (optionalImage.isPresent() && optionalImage.get().getCompany().getId().equals(companyId));
        boolean fourth = meterReq.getLocation() == null || (optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId));

        return second && third && fourth;
    }

    public void notify(Meter meter) {

        String message = "Meter " + meter.getName() + " has been assigned to you";
        if (meter.getUsers() != null) {
            meter.getUsers().forEach(assignedUser ->
                    notificationService.create(new Notification(message, assignedUser, NotificationType.METER, meter.getId())));
        }
    }

    public void patchNotify(Meter oldMeter, Meter newMeter) {
        String message = "Meter " + newMeter.getName() + " has been assigned to you";
        if (newMeter.getUsers() != null) {
            List<OwnUser> newUsers = newMeter.getUsers().stream().filter(
                    user -> oldMeter.getUsers().stream().noneMatch(user1 -> user1.getId().equals(user.getId()))).collect(Collectors.toList());
            newUsers.forEach(newUser ->
                    notificationService.create(new Notification(message, newUser, NotificationType.ASSET, newMeter.getId())));
        }
    }

    public Collection<Meter> findByAsset(Long id) {
        return meterRepository.findByAsset_Id(id);
    }
}
