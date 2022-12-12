package com.grash.service;

import com.grash.dto.RequestPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.RequestMapper;
import com.grash.model.*;
import com.grash.model.enums.RoleType;
import com.grash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final CompanyService companyService;
    private final FileService fileService;
    private final LocationService locationService;
    private final UserService userService;
    private final TeamService teamService;
    private final AssetService assetService;
    private final WorkOrderService workOrderService;
    private final NotificationService notificationService;
    private final RequestMapper requestMapper;

    public Request create(Request request) {
        return requestRepository.save(request);
    }

    public Request update(Long id, RequestPatchDTO request) {
        if (requestRepository.existsById(id)) {
            Request savedRequest = requestRepository.findById(id).get();
            return requestRepository.save(requestMapper.updateRequest(savedRequest, request));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Request> getAll() {
        return requestRepository.findAll();
    }

    public void delete(Long id) {
        requestRepository.deleteById(id);
    }

    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    public Collection<Request> findByCompany(Long id) {
        return requestRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, Request request) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(request.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Request requestReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(requestReq.getCompany().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);

        return first && canPatch(user, requestMapper.toDto(requestReq));
    }

    public boolean canPatch(OwnUser user, RequestPatchDTO requestReq) {
        Long companyId = user.getCompany().getId();

        Optional<Location> optionalLocation = requestReq.getLocation() == null ? Optional.empty() : locationService.findById(requestReq.getLocation().getId());
        Optional<File> optionalImage = requestReq.getImage() == null ? Optional.empty() : fileService.findById(requestReq.getImage().getId());
        Optional<Asset> optionalAsset = requestReq.getAsset() == null ? Optional.empty() : assetService.findById(requestReq.getAsset().getId());
        Optional<OwnUser> optionalPrimaryUser = requestReq.getPrimaryUser() == null ? Optional.empty() : userService.findById(requestReq.getPrimaryUser().getId());
        Optional<Team> optionalTeam = requestReq.getTeam() == null ? Optional.empty() : teamService.findById(requestReq.getTeam().getId());

        //optional fields
        boolean first = requestReq.getAsset() == null || (optionalAsset.isPresent() && optionalAsset.get().getCompany().getId().equals(companyId));
        boolean second = requestReq.getLocation() == null || (optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId));
        boolean third = requestReq.getImage() == null || (optionalImage.isPresent() && optionalImage.get().getCompany().getId().equals(companyId));
        boolean fourth = requestReq.getPrimaryUser() == null || (optionalPrimaryUser.isPresent() && optionalPrimaryUser.get().getCompany().getId().equals(companyId));
        boolean fifth = requestReq.getTeam() == null || (optionalTeam.isPresent() && optionalTeam.get().getCompany().getId().equals(companyId));

        return first && second && third && fourth && fifth;
    }

    public WorkOrder createWorkOrderFromRequest(Request request) {
        WorkOrder workOrder = workOrderService.getWorkOrderFromWorkOrderBase(request);
        workOrder.setParentRequest(request);
        WorkOrder savedWorkOrder = workOrderService.create(workOrder);
        workOrderService.notify(savedWorkOrder);
        request.setWorkOrder(savedWorkOrder);
        requestRepository.save(request);
        
        return savedWorkOrder;
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }
}
