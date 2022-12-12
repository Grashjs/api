package com.grash.service;

import com.grash.dto.LaborPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.LaborMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.LaborRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LaborService {
    private final LaborRepository laborRepository;
    private final WorkOrderService workOrderService;
    private final UserService userService;
    private final CompanyService companyService;
    private final LaborMapper laborMapper;

    private final NotificationService notificationService;

    public Labor create(Labor Labor) {
        return laborRepository.save(Labor);
    }

    public Labor update(Long id, LaborPatchDTO labor) {
        if (laborRepository.existsById(id)) {
            Labor savedLabor = laborRepository.findById(id).get();
            return laborRepository.save(laborMapper.updateLabor(savedLabor, labor));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Labor> getAll() {
        return laborRepository.findAll();
    }

    public void delete(Long id) {
        laborRepository.deleteById(id);
    }

    public Optional<Labor> findById(Long id) {
        return laborRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, Labor labor) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(labor.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Labor laborReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(laborReq.getCompany().getId());
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(laborReq.getWorkOrder().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);
        boolean second = optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompany().getId().equals(companyId);

        return first && second && canPatch(user, laborMapper.toDto(laborReq));
    }

    public boolean canPatch(OwnUser user, LaborPatchDTO laborReq) {
        Long companyId = user.getCompany().getId();
        return true;
    }

    public void notify(Labor labor) {

        String message = "Labor " + labor.getId() + " has been assigned to you";
        if (labor.getWorker() != null) {
            notificationService.create(new Notification(message, labor.getWorker(), NotificationType.LABOR, labor.getId()));
        }
    }

    public void patchNotify(Labor oldLabor, Labor newLabor) {
        String message = "Labor " + newLabor.getId() + " has been assigned to you";
        if (newLabor.getWorker() != null && !newLabor.getWorker().getId().equals(oldLabor.getWorker().getId())) {
            notificationService.create(new Notification(message, newLabor.getWorker(), NotificationType.ASSET, newLabor.getId()));
        }
    }
}
