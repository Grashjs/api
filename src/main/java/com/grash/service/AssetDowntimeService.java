package com.grash.service;

import com.grash.dto.AssetDowntimePatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.AssetDowntimeMapper;
import com.grash.model.AssetDowntime;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.AssetDowntimeRepository;
import com.grash.utils.DowntimeComparator;
import com.grash.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AssetDowntimeService {
    private final AssetDowntimeRepository assetDowntimeRepository;
    private final CompanyService companyService;
    private final AssetDowntimeMapper assetDowntimeMapper;

    public AssetDowntime create(AssetDowntime assetDowntime) {
        checkOverlapping(assetDowntime);
        return assetDowntimeRepository.save(assetDowntime);
    }

    public AssetDowntime save(AssetDowntime assetDowntime) {
        return assetDowntimeRepository.save(assetDowntime);
    }

    public AssetDowntime update(Long id, AssetDowntimePatchDTO assetDowntime) {
        if (assetDowntimeRepository.existsById(id)) {
            AssetDowntime savedAssetDowntime = assetDowntimeRepository.findById(id).get();
            AssetDowntime updatedAssetDowntime = assetDowntimeMapper.updateAssetDowntime(savedAssetDowntime, assetDowntime);
            checkOverlapping(updatedAssetDowntime);
            return assetDowntimeRepository.save(updatedAssetDowntime);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<AssetDowntime> getAll() {
        return assetDowntimeRepository.findAll();
    }

    public void delete(Long id) {
        assetDowntimeRepository.deleteById(id);
    }

    public Optional<AssetDowntime> findById(Long id) {
        return assetDowntimeRepository.findById(id);
    }

    public Collection<AssetDowntime> findByAsset(Long id) {
        return assetDowntimeRepository.findByAsset_Id(id);
    }

    public Collection<AssetDowntime> findByCompany(Long id) {
        return assetDowntimeRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, AssetDowntime assetDowntime) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(assetDowntime.getAsset().getCompanyId());
    }

    public boolean canCreate(OwnUser user, AssetDowntime assetDowntimeReq) {
        return true;
    }

    public boolean canPatch(OwnUser user, AssetDowntimePatchDTO assetDowntimeReq) {
        return true;
    }

    public Collection<AssetDowntime> findByStartsOnBetweenAndCompany(Date date1, Date date2, Long id) {
        return assetDowntimeRepository.findByStartsOnBetweenAndCompanyId(date1, date2, id);

    }

    public long getDowntimesMeantime(Collection<AssetDowntime> downtimes) {
        long result = 0;
        if (downtimes.size() > 2) {
            DowntimeComparator downtimeComparator = new DowntimeComparator();
            AssetDowntime firstDowntime = Collections.min(downtimes, downtimeComparator);
            AssetDowntime lastDowntime = Collections.max(downtimes, downtimeComparator);
            result = (Helper.getDateDiff(firstDowntime.getStartsOn(), lastDowntime.getStartsOn(), TimeUnit.HOURS)) / (downtimes.size() - 1);
        }
        return result;
    }

    private void checkOverlapping(AssetDowntime assetDowntime) {
        Collection<AssetDowntime> assetDowntimes = findByAsset(assetDowntime.getAsset().getId());
        Date startedOn = assetDowntime.getStartsOn();
        Date endedOn = assetDowntime.getEndsOn();
        assetDowntimes.forEach(assetDowntime1 -> {
            Date startedOn1 = assetDowntime1.getStartsOn();
            Date endedOn1 = assetDowntime1.getEndsOn();
            //(StartA <= EndB) and (EndA >= StartB)
            if (startedOn.before(endedOn1) && endedOn.after(startedOn1)) {
                throw new CustomException("The downtimes can't interweave", HttpStatus.NOT_ACCEPTABLE);
            }
        });
    }
}
