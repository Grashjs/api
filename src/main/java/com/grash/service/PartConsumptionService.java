package com.grash.service;

import com.grash.model.OwnUser;
import com.grash.model.PartConsumption;
import com.grash.model.enums.RoleType;
import com.grash.repository.PartConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartConsumptionService {
    private final PartConsumptionRepository partConsumptionRepository;

    public PartConsumption create(PartConsumption PartConsumption) {
        return partConsumptionRepository.save(PartConsumption);
    }

    public Collection<PartConsumption> getAll() {
        return partConsumptionRepository.findAll();
    }

    public void delete(Long id) {
        partConsumptionRepository.deleteById(id);
    }

    public Optional<PartConsumption> findById(Long id) {
        return partConsumptionRepository.findById(id);
    }

    public Collection<PartConsumption> findByCompany(Long id) {
        return partConsumptionRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, PartConsumption partConsumption) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(partConsumption.getCompanyId());
    }

    public Collection<PartConsumption> findByCreatedAtBetweenAndCompany(Date date1, Date date2, Long id) {
        return partConsumptionRepository.findByCreatedAtBetweenAndCompanyId(date1, date2, id);
    }

    public Collection<PartConsumption> findByWorkOrderAndPart(Long workOrderId, Long partId) {
        return partConsumptionRepository.findByWorkOrder_IdAndPart_Id(workOrderId, partId);
    }

    public void save(PartConsumption partConsumption) {
        partConsumptionRepository.save(partConsumption);
    }
}
