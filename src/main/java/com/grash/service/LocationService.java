package com.grash.service;

import com.grash.dto.LocationPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.LocationMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final VendorService vendorService;
    private final CustomerService customerService;
    private final CompanyService companyService;
    private final LocationMapper locationMapper;
    private final NotificationService notificationService;
    private final EntityManager em;

    public Location create(Location Location) {
        return locationRepository.save(Location);
    }

    @Transactional
    public Location update(Long id, LocationPatchDTO location) {
        if (locationRepository.existsById(id)) {
            Location savedLocation = locationRepository.findById(id).get();
            Location patchedLocation = locationRepository.saveAndFlush(locationMapper.updateLocation(savedLocation, location));
            em.refresh(patchedLocation);
            return patchedLocation;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Location> getAll() {
        return locationRepository.findAll();
    }

    public void delete(Long id) {
        locationRepository.deleteById(id);
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public Collection<Location> findByCompany(Long id) {
        return locationRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, Location location) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(location.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Location locationReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(locationReq.getCompany().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);

        return first && canPatch(user, locationMapper.toDto(locationReq));
    }

    public boolean canPatch(OwnUser user, LocationPatchDTO locationReq) {
        Long companyId = user.getCompany().getId();

        Optional<Location> optionalLocation = locationReq.getParentLocation() == null ? Optional.empty() : findById(locationReq.getParentLocation().getId());
        //optional fields
        boolean second = locationReq.getParentLocation() == null || (optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId));

        return second;
    }

    public void notify(Location location) {

        String message = "Location " + location.getName() + " has been assigned to you";
        if (location.getWorkers() != null) {
            location.getWorkers().forEach(assignedUser ->
                    notificationService.create(new Notification(message, assignedUser, NotificationType.LOCATION, location.getId())));
        }
        if (location.getTeams() != null) {
            location.getTeams().forEach(team -> team.getUsers().forEach(user ->
                    notificationService.create(new Notification(message, user, NotificationType.LOCATION, location.getId()))));
        }
    }

    public void patchNotify(Location oldLocation, Location newLocation) {
        String message = "Location " + newLocation.getName() + " has been assigned to you";
        if (newLocation.getWorkers() != null) {
            List<OwnUser> newUsers = newLocation.getWorkers().stream().filter(
                    user -> oldLocation.getWorkers().stream().noneMatch(user1 -> user1.getId().equals(user.getId()))).collect(Collectors.toList());
            newUsers.forEach(newUser ->
                    notificationService.create(new Notification(message, newUser, NotificationType.LOCATION, newLocation.getId())));
        }
        if (newLocation.getTeams() != null) {
            List<Team> newTeams = newLocation.getTeams().stream().filter(
                    team -> oldLocation.getTeams().stream().noneMatch(team1 -> team1.getId().equals(team.getId()))).collect(Collectors.toList());
            newTeams.forEach(team -> team.getUsers().forEach(user ->
                    notificationService.create(new Notification(message, user, NotificationType.LOCATION, newLocation.getId()))));
        }
    }

    public Collection<Location> findLocationChildren(Long id) {
        return locationRepository.findByParentLocation_Id(id);
    }

    public void save(Location location) {
        locationRepository.save(location);
    }
}
