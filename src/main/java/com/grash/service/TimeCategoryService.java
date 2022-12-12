package com.grash.service;

import com.grash.dto.CategoryPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.TimeCategoryMapper;
import com.grash.model.CompanySettings;
import com.grash.model.OwnUser;
import com.grash.model.TimeCategory;
import com.grash.model.enums.RoleType;
import com.grash.repository.TimeCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeCategoryService {
    private final TimeCategoryRepository timeCategoryRepository;
    private final CompanySettingsService companySettingsService;
    private final TimeCategoryMapper timeCategoryMapper;

    public TimeCategory create(TimeCategory timeCategory) {
        Optional<TimeCategory> categoryWithSameName = timeCategoryRepository.findByName(timeCategory.getName());
        if(categoryWithSameName.isPresent()) {
            throw new CustomException("TimeCategory with same name already exists", HttpStatus.NOT_ACCEPTABLE);
        }
        return timeCategoryRepository.save(timeCategory);
    }

    public TimeCategory update(Long id, CategoryPatchDTO timeCategory) {
        if (timeCategoryRepository.existsById(id)) {
            TimeCategory savedTimeCategory = timeCategoryRepository.findById(id).get();
            return timeCategoryRepository.save(timeCategoryMapper.updateTimeCategory(savedTimeCategory, timeCategory));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<TimeCategory> getAll() {
        return timeCategoryRepository.findAll();
    }

    public void delete(Long id) {
        timeCategoryRepository.deleteById(id);
    }

    public Optional<TimeCategory> findById(Long id) {
        return timeCategoryRepository.findById(id);
    }

    public Collection<TimeCategory> findByCompanySettings(Long id) {
        return timeCategoryRepository.findByCompanySettings_Id(id);

    }

    public boolean hasAccess(OwnUser user, TimeCategory timeCategory) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(timeCategory.getCompanySettings().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, TimeCategory timeCategoryReq) {
        Long companyId = user.getCompany().getId();

        Optional<CompanySettings> optionalCompanySettings = companySettingsService.findById(timeCategoryReq.getCompanySettings().getId());

        //@NotNull fields
        boolean first = optionalCompanySettings.isPresent() && optionalCompanySettings.get().getCompany().getId().equals(companyId);

        return first && canPatch(user, timeCategoryMapper.toDto(timeCategoryReq));
    }

    public boolean canPatch(OwnUser user, CategoryPatchDTO timeCategoryReq) {
        return true;
    }
}
