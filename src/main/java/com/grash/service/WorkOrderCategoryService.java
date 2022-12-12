package com.grash.service;

import com.grash.dto.CategoryPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.WorkOrderCategoryMapper;
import com.grash.model.CompanySettings;
import com.grash.model.OwnUser;
import com.grash.model.WorkOrderCategory;
import com.grash.model.enums.RoleType;
import com.grash.repository.WorkOrderCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkOrderCategoryService {
    private final WorkOrderCategoryRepository workOrderCategoryRepository;

    private final CompanySettingsService companySettingsService;
    private final WorkOrderCategoryMapper workOrderCategoryMapper;

    public WorkOrderCategory create(WorkOrderCategory workOrderCategory) {
        Optional<WorkOrderCategory> categoryWithSameName = workOrderCategoryRepository.findByName(workOrderCategory.getName());
        if(categoryWithSameName.isPresent()) {
            throw new CustomException("WorkOrderCategory with same name already exists", HttpStatus.NOT_ACCEPTABLE);
        }
        return workOrderCategoryRepository.save(workOrderCategory);
    }

    public WorkOrderCategory update(Long id, CategoryPatchDTO workOrderCategory) {
        if (workOrderCategoryRepository.existsById(id)) {
            WorkOrderCategory saveWorkOrderCategory = workOrderCategoryRepository.findById(id).get();
            return workOrderCategoryRepository.save(workOrderCategoryMapper.updateWorkOrderCategory(saveWorkOrderCategory, workOrderCategory));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<WorkOrderCategory> getAll() {
        return workOrderCategoryRepository.findAll();
    }

    public void delete(Long id) {
        workOrderCategoryRepository.deleteById(id);
    }

    public Optional<WorkOrderCategory> findById(Long id) {
        return workOrderCategoryRepository.findById(id);
    }

    public Collection<WorkOrderCategory> findByCompanySettings(Long id) {
        return workOrderCategoryRepository.findByCompanySettings_Id(id);
    }

    public boolean hasAccess(OwnUser user, WorkOrderCategory workOrderCategory) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(workOrderCategory.getCompanySettings().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, WorkOrderCategory workOrderCategoryReq) {
        Long companyId = user.getCompany().getId();

        Optional<CompanySettings> optionalCompanySettings = companySettingsService.findById(workOrderCategoryReq.getCompanySettings().getId());

        boolean first = optionalCompanySettings.isPresent() && optionalCompanySettings.get().getCompany().getId().equals(companyId);

        return first && canPatch(user, workOrderCategoryMapper.toDto(workOrderCategoryReq));
    }

    public boolean canPatch(OwnUser user, CategoryPatchDTO workOrderCategory) {
        return true;
    }

}
