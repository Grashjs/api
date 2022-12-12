package com.grash.service;

import com.grash.dto.CompanyPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.CompanyMapper;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public Company create(Company Company) {
        return companyRepository.save(Company);
    }

    public Company update(Company Company) {
        return companyRepository.save(Company);
    }

    public Collection<Company> getAll() {
        return companyRepository.findAll();
    }

    public void delete(Long id) {
        companyRepository.deleteById(id);
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, Company company) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(company.getId());
    }

    public boolean canPatch(OwnUser user, CompanyPatchDTO company) {
        return true;
    }

    public Company update(Long id, CompanyPatchDTO company) {
        if (companyRepository.existsById(id)) {
            Company savedCompany = companyRepository.findById(id).get();
            return companyRepository.save(companyMapper.updateCompany(savedCompany, company));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

}
