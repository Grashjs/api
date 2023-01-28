package com.grash.service;

import com.grash.dto.PartQuantityPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.PartQuantityMapper;
import com.grash.model.OwnUser;
import com.grash.model.PartQuantity;
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
        
        boolean first = companyService.isCompanyValid(partQuantityReq.getCompany(), companyId);
        boolean second = partService.isPartInCompany(partQuantityReq.getPart(), companyId, false);
        boolean third = purchaseOrderService.isPurchaseOrderInCompany(partQuantityReq.getPurchaseOrder(), companyId, true);
        boolean fourth = workOrderService.isWorkOrderInCompany(partQuantityReq.getWorkOrder(), companyId, true);
        return first && second && third && fourth && canPatch(user, partQuantityMapper.toPatchDto(partQuantityReq));
    }

    public boolean canPatch(OwnUser user, PartQuantityPatchDTO partQuantityReq) {
        return true;
    }

    public Collection<PartQuantity> findByPart(Long id) {
        return partQuantityRepository.findByPart_Id(id);
    }

    public Collection<PartQuantity> findByPurchaseOrder(Long id) {
        return partQuantityRepository.findByPurchaseOrder_Id(id);
    }


    public void save(PartQuantity partQuantity) {
        partQuantityRepository.save(partQuantity);
    }
}
