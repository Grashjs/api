package com.grash.service;

import com.grash.dto.CategoryPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.PurchaseOrderCategoryMapper;
import com.grash.model.CompanySettings;
import com.grash.model.OwnUser;
import com.grash.model.PurchaseOrderCategory;
import com.grash.model.enums.RoleType;
import com.grash.repository.PurchaseOrderCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderCategoryService {
    private final PurchaseOrderCategoryRepository purchaseOrderCategoryRepository;

    private final CompanySettingsService companySettingsService;
    private final PurchaseOrderCategoryMapper purchaseOrderCategoryMapper;

    public PurchaseOrderCategory create(PurchaseOrderCategory purchaseOrderCategory) {
        Optional<PurchaseOrderCategory> categoryWithSameName = purchaseOrderCategoryRepository.findByName(purchaseOrderCategory.getName());
        if(categoryWithSameName.isPresent()) {
            throw new CustomException("PurchaseOrderCategory with same name already exists", HttpStatus.NOT_ACCEPTABLE);
        }
        return purchaseOrderCategoryRepository.save(purchaseOrderCategory);
    }

    public PurchaseOrderCategory update(Long id, CategoryPatchDTO purchaseOrderCategory) {
        if (purchaseOrderCategoryRepository.existsById(id)) {
            PurchaseOrderCategory savedPurchaseOrderCategory = purchaseOrderCategoryRepository.findById(id).get();
            return purchaseOrderCategoryRepository.save(purchaseOrderCategoryMapper.updatePurchaseOrderCategory(savedPurchaseOrderCategory, purchaseOrderCategory));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<PurchaseOrderCategory> getAll() {
        return purchaseOrderCategoryRepository.findAll();
    }

    public void delete(Long id) {
        purchaseOrderCategoryRepository.deleteById(id);
    }

    public Optional<PurchaseOrderCategory> findById(Long id) {
        return purchaseOrderCategoryRepository.findById(id);
    }

    public Collection<PurchaseOrderCategory> findByCompanySettings(Long id) {
        return purchaseOrderCategoryRepository.findByCompanySettings_Id(id);
    }

    public boolean hasAccess(OwnUser user, PurchaseOrderCategory purchaseOrderCategory) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(purchaseOrderCategory.getCompanySettings().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, PurchaseOrderCategory purchaseOrderCategoryReq) {
        Long companyId = user.getCompany().getId();

        Optional<CompanySettings> optionalCompanySettings = companySettingsService.findById(purchaseOrderCategoryReq.getCompanySettings().getId());

        boolean first = optionalCompanySettings.isPresent() && optionalCompanySettings.get().getCompany().getId().equals(companyId);

        return first && canPatch(user, purchaseOrderCategoryMapper.toDto(purchaseOrderCategoryReq));
    }

    public boolean canPatch(OwnUser user, CategoryPatchDTO purchaseOrderCategoryReq) {
        return true;
    }

}
