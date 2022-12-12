package com.grash.service;

import com.grash.dto.PartQuantityPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.PartQuantityMapper;
import com.grash.model.*;
import com.grash.model.enums.RoleType;
import com.grash.repository.PartQuantityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartQuantityService {
    private final PartQuantityRepository partQuantityRepository;
    private final CompanyService companyService;
    private final PartService partService;
    private final PurchaseOrderService purchaseOrderService;
    private final WorkOrderService workOrderService;
    private final PartQuantityMapper partQuantityMapper;

    public PartQuantity create(PartQuantity PartQuantity) {
        return partQuantityRepository.save(PartQuantity);
    }

    public PartQuantity update(Long id, PartQuantityPatchDTO partQuantity) {
        if (partQuantityRepository.existsById(id)) {
            PartQuantity savedPartQuantity = partQuantityRepository.findById(id).get();
            return partQuantityRepository.save(partQuantityMapper.updatePartQuantity(savedPartQuantity, partQuantity));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<PartQuantity> getAll() {
        return partQuantityRepository.findAll();
    }

    public void delete(Long id) {
        partQuantityRepository.deleteById(id);
    }

    public Optional<PartQuantity> findById(Long id) {
        return partQuantityRepository.findById(id);
    }

    public Collection<PartQuantity> findByCompany(Long id) {
        return partQuantityRepository.findByCompany_Id(id);
    }

    public Collection<PartQuantity> findByWorkOrder(Long id) {
        return partQuantityRepository.findByWorkOrder_Id(id);
    }

    public boolean hasAccess(OwnUser user, PartQuantity partQuantity) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(partQuantity.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, PartQuantity partQuantityReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(partQuantityReq.getCompany().getId());
        Optional<Part> optionalPart = partService.findById(partQuantityReq.getPart().getId());
        Optional<PurchaseOrder> optionalPurchaseOrder = partQuantityReq.getPurchaseOrder() == null ? Optional.empty() : purchaseOrderService.findById(partQuantityReq.getPurchaseOrder().getId());
        Optional<WorkOrder> optionalWorkOrder = partQuantityReq.getWorkOrder() == null ? Optional.empty() : workOrderService.findById(partQuantityReq.getWorkOrder().getId());

        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);
        boolean second = optionalPart.isPresent() && optionalPart.get().getCompany().getId().equals(companyId);
        boolean third = partQuantityReq.getPurchaseOrder() == null || (optionalPurchaseOrder.isPresent() && optionalPurchaseOrder.get().getCompany().getId().equals(companyId));
        boolean fourth = partQuantityReq.getWorkOrder() == null || (optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompany().getId().equals(companyId));

        return first && second && third && fourth && canPatch(user, partQuantityMapper.toDto(partQuantityReq));
    }

    public boolean canPatch(OwnUser user, PartQuantityPatchDTO partQuantityReq) {
        return true;
    }

    public Collection<PartQuantity> findByPart(Long id) {
        return partQuantityRepository.findByPart_Id(id);
    }
}
