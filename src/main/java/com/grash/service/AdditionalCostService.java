package com.grash.service;

import com.grash.dto.AdditionalCostPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.AdditionalCostMapper;
import com.grash.model.AdditionalCost;
import com.grash.model.OwnUser;
import com.grash.model.WorkOrder;
import com.grash.model.enums.RoleType;
import com.grash.repository.AdditionalCostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdditionalCostService {

    private final AdditionalCostRepository additionalCostRepository;
    private final CompanyService companyService;
    private final UserService userService;
    private final WorkOrderService workOrderService;


    private final AdditionalCostMapper additionalCostMapper;

    public AdditionalCost create(AdditionalCost additionalCost) {
        return additionalCostRepository.save(additionalCost);
    }

    public AdditionalCost update(Long id, AdditionalCostPatchDTO additionalCost) {
        if (additionalCostRepository.existsById(id)) {
            AdditionalCost savedAdditionalCost = additionalCostRepository.findById(id).get();
            return additionalCostRepository.save(additionalCostMapper.updateAdditionalCost(savedAdditionalCost, additionalCost));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<AdditionalCost> getAll() {
        return additionalCostRepository.findAll();
    }

    public void delete(Long id) {
        additionalCostRepository.deleteById(id);
    }

    public Optional<AdditionalCost> findById(Long id) {
        return additionalCostRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, AdditionalCost additionalCost) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(additionalCost.getWorkOrder().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, AdditionalCost additionalCostReq) {
        Long companyId = user.getCompany().getId();

        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(additionalCostReq.getWorkOrder().getId());

        //@NotNull fields
        boolean second = optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompany().getId().equals(companyId);

        return second && canPatch(user, additionalCostMapper.toDto(additionalCostReq));
    }

    public boolean canPatch(OwnUser user, AdditionalCostPatchDTO additionalCostReq) {
        Long companyId = user.getCompany().getId();
        Optional<OwnUser> optionalUser = additionalCostReq.getAssignedTo() == null ? Optional.empty() : userService.findById(additionalCostReq.getAssignedTo().getId());

        boolean first = additionalCostReq.getAssignedTo() == null || (optionalUser.isPresent() && optionalUser.get().getCompany().getId().equals(companyId));

        return first;
    }

    public Collection<AdditionalCost> findByWorkOrder(Long id) {
        return additionalCostRepository.findByWorkOrder_Id(id);
    }
}
