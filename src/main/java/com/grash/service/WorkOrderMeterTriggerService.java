package com.grash.service;

import com.grash.dto.WorkOrderMeterTriggerPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.WorkOrderMeterTriggerMapper;
import com.grash.model.Meter;
import com.grash.model.OwnUser;
import com.grash.model.WorkOrderMeterTrigger;
import com.grash.model.enums.RoleType;
import com.grash.repository.WorkOrderMeterTriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkOrderMeterTriggerService {
    private final WorkOrderMeterTriggerRepository workOrderMeterTriggerRepository;
    private final WorkOrderService workOrderService;
    private final WorkOrderMeterTriggerMapper workOrderMeterTriggerMapper;
    private final MeterService meterService;

    public WorkOrderMeterTrigger create(WorkOrderMeterTrigger WorkOrderMeterTrigger) {
        return workOrderMeterTriggerRepository.save(WorkOrderMeterTrigger);
    }

    public WorkOrderMeterTrigger update(Long id, WorkOrderMeterTriggerPatchDTO workOrderMeterTrigger) {
        if (workOrderMeterTriggerRepository.existsById(id)) {
            WorkOrderMeterTrigger savedWorkOrderMeterTrigger = workOrderMeterTriggerRepository.findById(id).get();
            return workOrderMeterTriggerRepository.save(workOrderMeterTriggerMapper.updateWorkOrderMeterTrigger(savedWorkOrderMeterTrigger, workOrderMeterTrigger));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<WorkOrderMeterTrigger> getAll() {
        return workOrderMeterTriggerRepository.findAll();
    }

    public void delete(Long id) {
        workOrderMeterTriggerRepository.deleteById(id);
    }

    public Optional<WorkOrderMeterTrigger> findById(Long id) {
        return workOrderMeterTriggerRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, WorkOrderMeterTrigger workOrderMeterTrigger) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(workOrderMeterTrigger.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, WorkOrderMeterTrigger workOrderMeterTriggerReq) {
        Long companyId = user.getCompany().getId();

        Optional<Meter> optionalMeter = meterService.findById(workOrderMeterTriggerReq.getMeter().getId());

        //@NotNull fields
        boolean second = optionalMeter.isPresent() && optionalMeter.get().getCompany().getId().equals(companyId);

        return second && canPatch(user, workOrderMeterTriggerMapper.toDto(workOrderMeterTriggerReq));
    }

    public boolean canPatch(OwnUser user, WorkOrderMeterTriggerPatchDTO workOrderMeterTriggerReq) {
        return true;
    }

    public Collection<WorkOrderMeterTrigger> findByMeter(Long id) {
        return workOrderMeterTriggerRepository.findByMeter_Id(id);
    }
}
