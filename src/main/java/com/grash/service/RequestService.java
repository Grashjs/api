package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.dto.RequestPatchDTO;
import com.grash.dto.RequestShowDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.RequestMapper;
import com.grash.model.OwnUser;
import com.grash.model.Request;
import com.grash.model.WorkOrder;
import com.grash.model.enums.RoleType;
import com.grash.repository.RequestRepository;
import com.grash.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
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
    private final RequestMapper requestMapper;
    private final EntityManager em;

    @Transactional
    public Request create(Request request) {
        Request savedRequest = requestRepository.saveAndFlush(request);
        em.refresh(savedRequest);
        return savedRequest;
    }

    @Transactional
    public Request update(Long id, RequestPatchDTO request) {
        if (requestRepository.existsById(id)) {
            Request savedRequest = requestRepository.findById(id).get();
            Request updatedRequest = requestRepository.saveAndFlush(requestMapper.updateRequest(savedRequest, request));
            em.refresh(updatedRequest);
            return updatedRequest;
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
        return requestRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, Request request) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(request.getCompanyId());
    }

    public boolean canCreate(OwnUser user, Request requestReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = requestReq.getCompanyId().equals(companyId);
        return first && canPatch(user, requestMapper.toPatchDto(requestReq));
    }

    public boolean canPatch(OwnUser user, RequestPatchDTO requestReq) {
        Long companyId = user.getCompany().getId();
        //optional fields
        boolean first = assetService.isAssetInCompany(requestReq.getAsset(), companyId, true);
        boolean second = locationService.isLocationInCompany(requestReq.getLocation(), companyId, true);
        boolean third = fileService.isFileInCompany(requestReq.getImage(), companyId, true);
        boolean fourth = userService.isUserInCompany(requestReq.getPrimaryUser(), companyId, true);
        boolean fifth = teamService.isTeamInCompany(requestReq.getTeam(), companyId, true);

        return first && second && third && fourth && fifth;
    }

    public WorkOrder createWorkOrderFromRequest(Request request, OwnUser creator) {
        WorkOrder workOrder = workOrderService.getWorkOrderFromWorkOrderBase(request);
        if (creator.getCompany().getCompanySettings().getGeneralPreferences().isAutoAssignRequests()) {
            OwnUser primaryUser = workOrder.getPrimaryUser();
            workOrder.setPrimaryUser(primaryUser == null ? creator : primaryUser);
        }
        workOrder.setParentRequest(request);
        WorkOrder savedWorkOrder = workOrderService.create(workOrder);
        workOrderService.notify(savedWorkOrder, Helper.getLocale(companyService.findById(savedWorkOrder.getCompanyId()).get()));
        request.setWorkOrder(savedWorkOrder);
        requestRepository.save(request);

        return savedWorkOrder;
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public Collection<Request> findByCreatedAtBetweenAndCompany(Date date1, Date date2, Long id) {
        return requestRepository.findByCreatedAtBetweenAndCompanyId(date1, date2, id);
    }

    public Page<RequestShowDTO> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<Request> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return requestRepository.findAll(builder.build(), page).map(requestMapper::toShowDto);
    }

    public boolean isRequestInCompany(Request request, long companyId, boolean optional) {
        if (optional) {
            Optional<Request> optionalRequest = request == null ? Optional.empty() : findById(request.getId());
            return request == null || (optionalRequest.isPresent() && optionalRequest.get().getCompanyId().equals(companyId));
        } else {
            Optional<Request> optionalRequest = findById(request.getId());
            return optionalRequest.isPresent() && optionalRequest.get().getCompanyId().equals(companyId);
        }
    }
}
