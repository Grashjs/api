package com.grash.service;

import com.grash.dto.CategoryPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.CostCategoryMapper;
import com.grash.model.CostCategory;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.CostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CostCategoryService {
    private final CostCategoryRepository costCategoryRepository;

    private final CostCategoryMapper costCategoryMapper;

    public CostCategory create(CostCategory CostCategory) {
        return costCategoryRepository.save(CostCategory);
    }

    public CostCategory update(Long id, CategoryPatchDTO costCategory) {
        if (costCategoryRepository.existsById(id)) {
            CostCategory savedCostCategory = costCategoryRepository.findById(id).get();
            return costCategoryRepository.save(costCategoryMapper.updateCostCategory(savedCostCategory, costCategory));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);

    }

    public Collection<CostCategory> getAll() {
        return costCategoryRepository.findAll();
    }

    public void delete(Long id) {
        costCategoryRepository.deleteById(id);
    }

    public Optional<CostCategory> findById(Long id) {
        return costCategoryRepository.findById(id);
    }

    public Collection<CostCategory> findByCompanySettings(Long id) {
        return costCategoryRepository.findByCompanySettings_Id(id);
    }

    public boolean hasAccess(OwnUser user, CostCategory costCategory) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(costCategory.getCompanySettings().getCompany().getId());
    }
}
