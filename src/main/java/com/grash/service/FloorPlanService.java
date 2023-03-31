package com.grash.service;

import com.grash.dto.FloorPlanPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.FloorPlanMapper;
import com.grash.model.FloorPlan;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.FloorPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FloorPlanService {
    private final FloorPlanRepository floorPlanRepository;
    private final FileService fileService;
    private final LocationService locationService;
    private final FloorPlanMapper floorPlanMapper;
    private final EntityManager em;

    @Transactional
    public FloorPlan create(FloorPlan floorPlan) {
        FloorPlan savedFloorPlan = floorPlanRepository.saveAndFlush(floorPlan);
        em.refresh(savedFloorPlan);
        return savedFloorPlan;
    }

    @Transactional
    public FloorPlan update(Long id, FloorPlanPatchDTO floorPlan) {
        if (floorPlanRepository.existsById(id)) {
            FloorPlan savedFloorPlan = floorPlanRepository.findById(id).get();
            FloorPlan updatedFloorPlan = floorPlanRepository.saveAndFlush(floorPlanMapper.updateFloorPlan(savedFloorPlan, floorPlan));
            em.refresh(updatedFloorPlan);
            return updatedFloorPlan;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<FloorPlan> getAll() {
        return floorPlanRepository.findAll();
    }

    public void delete(Long id) {
        floorPlanRepository.deleteById(id);
    }

    public Optional<FloorPlan> findById(Long id) {
        return floorPlanRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, FloorPlan floorPlan) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(floorPlan.getLocation().getCompanyId());
    }

    public boolean canCreate(OwnUser user, FloorPlan floorPlanReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = locationService.isLocationInCompany(floorPlanReq.getLocation(), companyId, false);
        return first && canPatch(user, floorPlanMapper.toPatchDto(floorPlanReq));
    }

    public boolean canPatch(OwnUser user, FloorPlanPatchDTO floorPlanReq) {
        Long companyId = user.getCompany().getId();
        //optional fields
        return fileService.isFileInCompany(floorPlanReq.getImage(), companyId, true);
    }

    public Collection<FloorPlan> findByLocation(Long id) {
        return floorPlanRepository.findByLocation_Id(id);
    }
}
