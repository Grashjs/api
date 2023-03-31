package com.grash.service;

import com.grash.dto.MultiPartsPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.MultiPartsMapper;
import com.grash.model.MultiParts;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.MultiPartsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MultiPartsService {
    private final MultiPartsRepository multiPartsRepository;
    private final CompanyService companyService;
    private final MultiPartsMapper multiPartsMapper;
    private final EntityManager em;
    private final PartService partService;

    @Transactional
    public MultiParts create(MultiParts multiParts) {
        MultiParts savedMultiParts = multiPartsRepository.saveAndFlush(multiParts);
        em.refresh(savedMultiParts);
        return savedMultiParts;
    }

    @Transactional
    public MultiParts update(Long id, MultiPartsPatchDTO multiPartsPatchDTO) {
        if (multiPartsRepository.existsById(id)) {
            MultiParts savedMultiParts = multiPartsRepository.findById(id).get();
            MultiParts updatedMultiParts = multiPartsRepository.saveAndFlush(multiPartsMapper.updateMultiParts(savedMultiParts, multiPartsPatchDTO));
            em.refresh(updatedMultiParts);
            return updatedMultiParts;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<MultiParts> getAll() {
        return multiPartsRepository.findAll();
    }

    public void delete(Long id) {
        multiPartsRepository.deleteById(id);
    }

    public Optional<MultiParts> findById(Long id) {
        return multiPartsRepository.findById(id);
    }

    public Collection<MultiParts> findByCompany(Long id) {
        return multiPartsRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, MultiParts multiParts) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(multiParts.getCompanyId());
    }

    public boolean canCreate(OwnUser user, MultiParts multiPartsReq) {
        Long companyId = user.getCompany().getId();
        boolean first = multiPartsReq.getCompanyId().equals(companyId);
        return first && canPatch(user, multiPartsMapper.toPatchDto(multiPartsReq));
    }

    public boolean canPatch(OwnUser user, MultiPartsPatchDTO multiPartsReq) {
        Long companyId = user.getCompany().getId();
        boolean first = multiPartsReq.getParts() == null || multiPartsReq.getParts().stream().allMatch(item ->
                partService.isPartInCompany(item, companyId, false));
        return first;
    }
}
