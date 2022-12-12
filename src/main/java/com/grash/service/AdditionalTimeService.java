package com.grash.service;

import com.grash.dto.AdditionalTimePatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.AdditionalTimeMapper;
import com.grash.model.*;
import com.grash.model.enums.RoleType;
import com.grash.model.enums.TimeStatus;
import com.grash.repository.AdditionalTimeRepository;
import com.grash.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AdditionalTimeService {
    private final AdditionalTimeRepository additionalTimeRepository;

    private final CompanyService companyService;
    private final TimeCategoryService timeCategoryService;
    private final UserService userService;
    private final WorkOrderService workOrderService;
    private final AdditionalTimeMapper additionalTimeMapper;

    public AdditionalTime create(AdditionalTime AdditionalTime) {
        return additionalTimeRepository.save(AdditionalTime);
    }

    public AdditionalTime update(Long id, AdditionalTimePatchDTO additionalTime) {
        if (additionalTimeRepository.existsById(id)) {
            AdditionalTime savedAdditionalTime = additionalTimeRepository.findById(id).get();
            return additionalTimeRepository.save(additionalTimeMapper.updateAdditionalTime(savedAdditionalTime, additionalTime));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public AdditionalTime save(AdditionalTime additionalTime) {
        return additionalTimeRepository.save(additionalTime);
    }

    public Collection<AdditionalTime> getAll() {
        return additionalTimeRepository.findAll();
    }

    public void delete(Long id) {
        additionalTimeRepository.deleteById(id);
    }

    public Optional<AdditionalTime> findById(Long id) {
        return additionalTimeRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, AdditionalTime additionalTime) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(additionalTime.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, AdditionalTime additionalTimeReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(additionalTimeReq.getCompany().getId());
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(additionalTimeReq.getWorkOrder().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);
        boolean second = optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompany().getId().equals(companyId);

        return first && second && canPatch(user, additionalTimeMapper.toDto(additionalTimeReq));
    }

    public boolean canPatch(OwnUser user, AdditionalTimePatchDTO additionalTimeReq) {
        Long companyId = user.getCompany().getId();

        Optional<TimeCategory> optionalTimeCategory = additionalTimeReq.getTimeCategory() == null ? Optional.empty() : timeCategoryService.findById(additionalTimeReq.getTimeCategory().getId());
        Optional<OwnUser> optionalUser = additionalTimeReq.getAssignedTo() == null ? Optional.empty() : userService.findById(additionalTimeReq.getAssignedTo().getId());

        //optional fields
        boolean third = additionalTimeReq.getTimeCategory() == null || (optionalTimeCategory.isPresent() && optionalTimeCategory.get().getCompanySettings().getCompany().getId().equals(companyId));
        boolean sixth = additionalTimeReq.getAssignedTo() == null || (optionalUser.isPresent() && optionalUser.get().getCompany().getId().equals(companyId));

        return third && sixth;
    }

    public Collection<AdditionalTime> findByWorkOrder(Long id) {
        return additionalTimeRepository.findByWorkOrder_Id(id);
    }

    public AdditionalTime stop(AdditionalTime additionalTime) {
        additionalTime.setStatus(TimeStatus.STOPPED);
        additionalTime.setDuration(additionalTime.getDuration() + Helper.getDateDiff(additionalTime.getStartedAt(), new Date(), TimeUnit.SECONDS));
        return save(additionalTime);
    }
}
