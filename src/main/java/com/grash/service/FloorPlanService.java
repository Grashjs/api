package com.grash.service;

import com.grash.dto.FloorPlanPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.FloorPlanMapper;
import com.grash.model.File;
import com.grash.model.FloorPlan;
import com.grash.model.Location;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.FloorPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FloorPlanService {
    private final FloorPlanRepository floorPlanRepository;
    private final FileService fileService;
    private final LocationService locationService;
    private final FloorPlanMapper floorPlanMapper;

    public FloorPlan create(FloorPlan FloorPlan) {
        return floorPlanRepository.save(FloorPlan);
    }

    public FloorPlan update(Long id, FloorPlanPatchDTO floorPlan) {
        if (floorPlanRepository.existsById(id)) {
            FloorPlan savedFloorPlan = floorPlanRepository.findById(id).get();
            return floorPlanRepository.save(floorPlanMapper.updateFloorPlan(savedFloorPlan, floorPlan));
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
        } else return user.getCompany().getId().equals(floorPlan.getLocation().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, FloorPlan floorPlanReq) {
        Long companyId = user.getCompany().getId();

        Optional<Location> optionalLocation = locationService.findById(floorPlanReq.getLocation().getId());

        //@NotNull fields
        boolean first = optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId);

        return first && canPatch(user, floorPlanMapper.toDto(floorPlanReq));
    }

    public boolean canPatch(OwnUser user, FloorPlanPatchDTO floorPlanReq) {
        Long companyId = user.getCompany().getId();
        Optional<File> optionalImage = floorPlanReq.getImage() == null ? Optional.empty() : fileService.findById(floorPlanReq.getImage().getId());

        //optional fields
        boolean third = floorPlanReq.getImage() == null || (optionalImage.isPresent() && optionalImage.get().getCompany().getId().equals(companyId));

        return third;
    }

    public Collection<FloorPlan> findByLocation(Long id) {
        return floorPlanRepository.findByLocation_Id(id);
    }
}
