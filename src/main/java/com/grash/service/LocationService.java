package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.dto.LocationPatchDTO;
import com.grash.dto.LocationShowDTO;
import com.grash.dto.imports.LocationImportDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.LocationMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final CompanyService companyService;
    private final CustomerService customerService;
    private final MessageSource messageSource;
    private final VendorService vendorService;
    private final LocationMapper locationMapper;
    private final NotificationService notificationService;
    private final TeamService teamService;
    private final EntityManager em;
    private final FileService fileService;

    @Transactional
    public Location create(Location location) {
        Location savedLocation = locationRepository.saveAndFlush(location);
        em.refresh(savedLocation);
        return savedLocation;
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
        return locationRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, Location location) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(location.getCompanyId());
    }

    public boolean canCreate(OwnUser user, Location locationReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = locationReq.getCompanyId().equals(companyId);


        return first && canPatch(user, locationMapper.toPatchDto(locationReq));
    }

    public boolean canPatch(OwnUser user, LocationPatchDTO locationReq) {
        Long companyId = user.getCompany().getId();
        boolean first = isLocationInCompany(locationReq.getParentLocation(), companyId, true);
        boolean second = locationReq.getWorkers() == null || locationReq.getWorkers().stream().allMatch(item ->
                userService.isUserInCompany(item, companyId, false));
        boolean third = locationReq.getTeams() == null || locationReq.getTeams().stream().allMatch(item ->
                teamService.isTeamInCompany(item, companyId, false));
        boolean fourth = locationReq.getVendors() == null || locationReq.getVendors().stream().allMatch(item ->
                vendorService.isVendorInCompany(item, companyId, false));
        boolean fifth = locationReq.getCustomers() == null || locationReq.getCustomers().stream().allMatch(item ->
                customerService.isCustomerInCompany(item, companyId, false));
        boolean sixth = locationReq.getFiles() == null || locationReq.getFiles().stream().allMatch(item ->
                fileService.isFileInCompany(item, companyId, false));

        return first && second && third && fourth && fifth && sixth;
    }

    public void notify(Location location, Locale locale) {
        String title = messageSource.getMessage("new_assignment", null, locale);
        String message = messageSource.getMessage("notification_location_assigned", new Object[]{location.getName()}, locale);
        notificationService.createMultiple(location.getUsers().stream().map(user -> new Notification(message, user, NotificationType.LOCATION, location.getId())).collect(Collectors.toList()), true, title);
    }

    public void patchNotify(Location oldLocation, Location newLocation, Locale locale) {
        String title = messageSource.getMessage("new_assignment", null, locale);
        String message = messageSource.getMessage("notification_location_assigned", new Object[]{newLocation.getName()}, locale);
        notificationService.createMultiple(oldLocation.getNewUsersToNotify(newLocation.getUsers()).stream().map(user ->
                new Notification(message, user, NotificationType.LOCATION, newLocation.getId())).collect(Collectors.toList()), true, title);
    }

    public Collection<Location> findLocationChildren(Long id) {
        return locationRepository.findByParentLocation_Id(id);
    }

    public void save(Location location) {
        locationRepository.save(location);
    }

    public boolean isLocationInCompany(Location location, long companyId, boolean optional) {
        if (optional) {
            Optional<Location> optionalLocation = location == null ? Optional.empty() : findById(location.getId());
            return location == null || (optionalLocation.isPresent() && optionalLocation.get().getCompanyId().equals(companyId));
        } else {
            Optional<Location> optionalLocation = findById(location.getId());
            return optionalLocation.isPresent() && optionalLocation.get().getCompanyId().equals(companyId);
        }
    }

    public Optional<Location> findByNameAndCompany(String locationName, Long companyId) {
        return locationRepository.findByNameAndCompanyId(locationName, companyId);
    }

    public void importLocation(Location location, LocationImportDTO dto, Company company) {
        Long companyId = company.getId();
        location.setName(dto.getName());
        location.setAddress(dto.getAddress());
        location.setLongitude(dto.getLongitude());
        location.setLatitude(dto.getLatitude());
        Optional<Location> optionalLocation = findByNameAndCompany(dto.getParentLocationName(), companyId);
        optionalLocation.ifPresent(location::setParentLocation);
        List<OwnUser> workers = new ArrayList<>();
        dto.getWorkersEmails().forEach(email -> {
            Optional<OwnUser> optionalUser1 = userService.findByEmailAndCompany(email, companyId);
            optionalUser1.ifPresent(workers::add);
        });
        location.setWorkers(workers);
        List<Team> teams = new ArrayList<>();
        dto.getTeamsNames().forEach(teamName -> {
            Optional<Team> optionalTeam = teamService.findByNameAndCompany(teamName, companyId);
            optionalTeam.ifPresent(teams::add);
        });
        location.setTeams(teams);
        List<Customer> customers = new ArrayList<>();
        dto.getCustomersNames().forEach(name -> {
            Optional<Customer> optionalCustomer = customerService.findByNameAndCompany(name, companyId);
            optionalCustomer.ifPresent(customers::add);
        });
        location.setCustomers(customers);
        List<Vendor> vendors = new ArrayList<>();
        dto.getVendorsNames().forEach(name -> {
            Optional<Vendor> optionalVendor = vendorService.findByNameAndCompany(name, companyId);
            optionalVendor.ifPresent(vendors::add);
        });
        location.setVendors(vendors);
        locationRepository.save(location);
    }

    public Optional<Location> findByIdAndCompany(Long id, Long companyId) {
        return locationRepository.findByIdAndCompanyId(id, companyId);
    }

    public Page<LocationShowDTO> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<Location> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return locationRepository.findAll(builder.build(), page).map(locationMapper::toShowDto);
    }
}
