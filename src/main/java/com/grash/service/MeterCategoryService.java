package com.grash.service;

import com.grash.dto.CategoryPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.MeterCategoryMapper;
import com.grash.model.CompanySettings;
import com.grash.model.MeterCategory;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.MeterCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeterCategoryService {
    private final MeterCategoryRepository meterCategoryRepository;

    private final CompanySettingsService companySettingsService;
    private final MeterCategoryMapper meterCategoryMapper;

    public MeterCategory create(MeterCategory MeterCategory) {
        return meterCategoryRepository.save(MeterCategory);
    }

    public MeterCategory update(Long id, CategoryPatchDTO meterCategory) {
        if (meterCategoryRepository.existsById(id)) {
            MeterCategory savedMeterCategory = meterCategoryRepository.findById(id).get();
            return meterCategoryRepository.save(meterCategoryMapper.updateMeterCategory(savedMeterCategory, meterCategory));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<MeterCategory> getAll() {
        return meterCategoryRepository.findAll();
    }

    public void delete(Long id) {
        meterCategoryRepository.deleteById(id);
    }

    public Optional<MeterCategory> findById(Long id) {
        return meterCategoryRepository.findById(id);
    }

    public Collection<MeterCategory> findByCompany(Long id) {
        return meterCategoryRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, MeterCategory meterCategory) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(meterCategory.getCompanySettings().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, MeterCategory meterCategoryReq) {
        Long companyId = user.getCompany().getId();

        Optional<CompanySettings> optionalCompanySettings = companySettingsService.findById(meterCategoryReq.getCompanySettings().getId());

        boolean first = optionalCompanySettings.isPresent() && optionalCompanySettings.get().getId().equals(companyId);

        return first && canPatch(user, meterCategoryMapper.toDto(meterCategoryReq));
    }

    public boolean canPatch(OwnUser user, CategoryPatchDTO meterCategoryReq) {
        return true;
    }


}
