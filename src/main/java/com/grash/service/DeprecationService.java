package com.grash.service;

import com.grash.dto.DeprecationPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.DeprecationMapper;
import com.grash.model.Deprecation;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.DeprecationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeprecationService {
    private final DeprecationRepository deprecationRepository;
    private final CompanyService companyService;
    private final DeprecationMapper deprecationMapper;

    public Deprecation create(Deprecation Deprecation) {
        return deprecationRepository.save(Deprecation);
    }

    public Deprecation update(Long id, DeprecationPatchDTO deprecation) {
        if (deprecationRepository.existsById(id)) {
            Deprecation savedDeprecation = deprecationRepository.findById(id).get();
            return deprecationRepository.save(deprecationMapper.updateDeprecation(savedDeprecation, deprecation));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Deprecation> getAll() {
        return deprecationRepository.findAll();
    }

    public void delete(Long id) {
        deprecationRepository.deleteById(id);
    }

    public Optional<Deprecation> findById(Long id) {
        return deprecationRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, Deprecation deprecation) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(deprecation.getCompanyId());
    }

    public boolean canCreate(OwnUser user, Deprecation deprecationReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = deprecationReq.getCompanyId().equals(companyId);
        return first && canPatch(user, deprecationMapper.toPatchDto(deprecationReq));
    }

    public boolean canPatch(OwnUser user, DeprecationPatchDTO deprecationReq) {
        return true;
    }

    public boolean isDeprecationInCompany(Deprecation deprecation, long companyId, boolean optional) {
        if (optional) {
            Optional<Deprecation> optionalDeprecation = deprecation == null ? Optional.empty() : findById(deprecation.getId());
            return deprecation == null || (optionalDeprecation.isPresent() && optionalDeprecation.get().getCompanyId().equals(companyId));
        } else {
            Optional<Deprecation> optionalDeprecation = findById(deprecation.getId());
            return optionalDeprecation.isPresent() && optionalDeprecation.get().getCompanyId().equals(companyId);
        }
    }
}
