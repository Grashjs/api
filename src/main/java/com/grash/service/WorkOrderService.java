package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.dto.WorkOrderPatchDTO;
import com.grash.dto.WorkOrderShowDTO;
import com.grash.dto.imports.WorkOrderImportDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.WorkOrderMapper;
import com.grash.model.*;
import com.grash.model.abstracts.Cost;
import com.grash.model.abstracts.WorkOrderBase;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.Priority;
import com.grash.model.enums.RoleType;
import com.grash.model.enums.Status;
import com.grash.repository.WorkOrderHistoryRepository;
import com.grash.repository.WorkOrderRepository;
import com.grash.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderHistoryRepository workOrderHistoryRepository;
    private final LocationService locationService;
    private final CustomerService customerService;
    private final TeamService teamService;
    private final AssetService assetService;
    private final UserService userService;
    private final CompanyService companyService;
    private LaborService laborService;
    private AdditionalCostService additionalCostService;
    private PartQuantityService partQuantityService;
    private final NotificationService notificationService;
    private final WorkOrderMapper workOrderMapper;
    private final EntityManager em;
    private final EmailService2 emailService2;
    private final WorkOrderCategoryService workOrderCategoryService;
    private final MessageSource messageSource;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public WorkOrder create(WorkOrder workOrder) {
        WorkOrder savedWorkOrder = workOrderRepository.saveAndFlush(workOrder);
        em.refresh(savedWorkOrder);
        return savedWorkOrder;
    }

    @Autowired
    public void setDeps(@Lazy LaborService laborService,
                        @Lazy AdditionalCostService additionalCostService,
                        @Lazy PartQuantityService partQuantityService) {
        this.laborService = laborService;
        this.additionalCostService = additionalCostService;
        this.partQuantityService = partQuantityService;
    }

    @Transactional
    public WorkOrder update(Long id, WorkOrderPatchDTO workOrder, OwnUser user) {
        if (workOrderRepository.existsById(id)) {
            WorkOrder savedWorkOrder = workOrderRepository.findById(id).get();
            WorkOrder updatedWorkOrder = workOrderRepository.saveAndFlush(workOrderMapper.updateWorkOrder(savedWorkOrder, workOrder));
            em.refresh(updatedWorkOrder);
            WorkOrderHistory workOrderHistory = WorkOrderHistory.builder()
                    .name(messageSource.getMessage("updating_wo", new Object[]{workOrder.getTitle()}, Helper.getLocale(user)))
                    .workOrder(updatedWorkOrder)
                    .user(user)
                    .build();
            workOrderHistoryRepository.save(workOrderHistory);
            return updatedWorkOrder;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<WorkOrder> getAll() {
        return workOrderRepository.findAll();
    }

    public void delete(Long id) {
        workOrderRepository.deleteById(id);
    }

    public Optional<WorkOrder> findById(Long id) {
        return workOrderRepository.findById(id);
    }

    public Optional<WorkOrder> findByIdAndCompany(Long id, Long companyId) {
        return workOrderRepository.findByIdAndCompanyId(id, companyId);
    }

    public Collection<WorkOrder> findByCompany(Long id) {
        return workOrderRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, WorkOrder workOrder) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(workOrder.getCompanyId());
    }

    public boolean canCreate(OwnUser user, WorkOrder workOrderReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = workOrderReq.getCompanyId().equals(companyId);

        return first && canPatch(user, workOrderMapper.toPatchDto(workOrderReq));
    }

    public boolean canPatch(OwnUser user, WorkOrderPatchDTO workOrderReq) {
        Long companyId = user.getCompany().getId();

        boolean second = locationService.isLocationInCompany(workOrderReq.getLocation(), companyId, true);
        boolean third = teamService.isTeamInCompany(workOrderReq.getTeam(), companyId, true);
        boolean fourth = userService.isUserInCompany(workOrderReq.getPrimaryUser(), companyId, true);
        boolean fifth = assetService.isAssetInCompany(workOrderReq.getAsset(), companyId, true);
        return second && third && fourth && fifth;
    }

    public void notify(WorkOrder workOrder, Locale locale) {
        String title = messageSource.getMessage("new_wo", null, locale);
        String message = messageSource.getMessage("notification_wo_assigned", new Object[]{workOrder.getTitle()}, locale);
        Collection<OwnUser> users = workOrder.getUsers();
        notificationService.createMultiple(users.stream().map(user -> new Notification(message, user, NotificationType.WORK_ORDER, workOrder.getId())).collect(Collectors.toList()), true, title);

        Map<String, Object> mailVariables = new HashMap<String, Object>() {{
            put("workOrderLink", frontendUrl + "/app/work-orders/" + workOrder.getId());
            put("featuresLink", frontendUrl + "/#key-features");
            put("workOrderTitle", workOrder.getTitle());
        }};
        Collection<OwnUser> usersToMail = users.stream().filter(user -> user.getUserSettings().isEmailUpdatesForWorkOrders()).collect(Collectors.toList());
        if (usersToMail.size() > 0) {
            emailService2.sendMessageUsingThymeleafTemplate(usersToMail.stream().map(OwnUser::getEmail).toArray(String[]::new), messageSource.getMessage("new_wo", null, locale), mailVariables, "new-work-order.html", Helper.getLocale(users.stream().findFirst().get()));
        }
    }

    public void patchNotify(WorkOrder oldWorkOrder, WorkOrder newWorkOrder, Locale locale) {
        String title = messageSource.getMessage("new_assignment", null, locale);
        String message = messageSource.getMessage("notification_wo_assigned", new Object[]{newWorkOrder.getTitle()}, Helper.getLocale(companyService.findById(newWorkOrder.getCompanyId()).get()));
        notificationService.createMultiple(oldWorkOrder.getNewUsersToNotify(newWorkOrder.getUsers()).stream().map(user ->
                new Notification(message, user, NotificationType.WORK_ORDER, newWorkOrder.getId())).collect(Collectors.toList()), true, title);
    }

    public Collection<WorkOrder> findByAsset(Long id) {
        return workOrderRepository.findByAsset_Id(id);
    }

    public Collection<WorkOrder> findByPM(Long id) {
        return workOrderRepository.findByParentPreventiveMaintenance_Id(id);
    }

    public Collection<WorkOrder> findByLocation(Long id) {
        return workOrderRepository.findByLocation_Id(id);
    }

    public Page<WorkOrderShowDTO> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<WorkOrder> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return workOrderRepository.findAll(builder.build(), page).map(workOrderMapper::toShowDto);
    }

    public void save(WorkOrder workOrder) {
        workOrderRepository.save(workOrder);
    }

    public WorkOrder getWorkOrderFromWorkOrderBase(WorkOrderBase workOrderBase) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setCompanyId(workOrderBase.getCompanyId());
        workOrder.setTitle(workOrderBase.getTitle());
        workOrder.setDescription(workOrderBase.getDescription());
        workOrder.setPriority(workOrderBase.getPriority());
        workOrder.setImage(workOrder.getImage());
        //TODO
        //workOrder.setFiles(workOrderBase.getFiles());
        workOrder.setAsset(workOrderBase.getAsset());
        workOrder.setLocation(workOrderBase.getLocation());
        workOrder.setPrimaryUser(workOrderBase.getPrimaryUser());
        workOrder.setTeam(workOrderBase.getTeam());
        return workOrder;
    }

    public Collection<WorkOrder> findByPrimaryUser(Long id) {
        return workOrderRepository.findByPrimaryUser_Id(id);
    }

    public Collection<WorkOrder> findByCompletedBy(Long id) {
        return workOrderRepository.findByCompletedBy_Id(id);
    }

    public Collection<WorkOrder> findByPriorityAndCompany(Priority priority, Long companyId) {
        return workOrderRepository.findByPriorityAndCompanyId(priority, companyId);
    }

    public Collection<WorkOrder> findByCategory(Long id) {
        return workOrderRepository.findByCategory_Id(id);
    }

    public Collection<WorkOrder> findByCompletedOnBetweenAndCompany(Date date1, Date date2, Long companyId) {
        return workOrderRepository.findByCompletedOnBetweenAndCompanyId(date1, date2, companyId);
    }

    public Pair<Long, Long> getLaborCostAndTime(Collection<WorkOrder> workOrders) {
        Collection<Long> laborCostsArray = new ArrayList<>();
        Collection<Long> laborTimesArray = new ArrayList<>();
        workOrders.forEach(workOrder -> {
                    Collection<Labor> labors = laborService.findByWorkOrder(workOrder.getId());
                    long laborsCosts = labors.stream().mapToLong(labor -> labor.getHourlyRate() * labor.getDuration() / 3600).sum();
                    long laborTimes = labors.stream().mapToLong(Labor::getDuration).sum();
                    laborCostsArray.add(laborsCosts);
                    laborTimesArray.add(laborTimes);
                }
        );
        long laborCost = laborCostsArray.stream().mapToLong(value -> value).sum();
        long laborTimes = laborTimesArray.stream().mapToLong(value -> value).sum();

        return Pair.of(laborCost, laborTimes);
    }

    public long getAdditionalCost(Collection<WorkOrder> workOrders) {
        Collection<Long> costs = workOrders.stream().map(workOrder -> {
                    Collection<AdditionalCost> additionalCosts = additionalCostService.findByWorkOrder(workOrder.getId());
                    return additionalCosts.stream().mapToLong(Cost::getCost).sum();
                }
        ).collect(Collectors.toList());
        return costs.stream().mapToLong(value -> value).sum();
    }

    public long getPartCost(Collection<WorkOrder> workOrders) {
        Collection<Long> costs = workOrders.stream().map(workOrder -> {
                    Collection<PartQuantity> partQuantities = partQuantityService.findByWorkOrder(workOrder.getId());
                    return partQuantities.stream().mapToLong(partQuantity -> partQuantity.getPart().getCost() * partQuantity.getQuantity()).sum();
                }
        ).collect(Collectors.toList());
        return costs.stream().mapToLong(value -> value).sum();
    }

    public long getAllCost(Collection<WorkOrder> workOrders, boolean includeLaborCost) {
        return getPartCost(workOrders) + getAdditionalCost(workOrders) + (includeLaborCost ? getLaborCostAndTime(workOrders).getFirst() : 0);
    }

    public Collection<WorkOrder> findByCreatedBy(Long id) {
        return workOrderRepository.findByCreatedBy(id);
    }

    public boolean isWorkOrderInCompany(WorkOrder workOrder, long companyId, boolean optional) {
        if (optional) {
            Optional<WorkOrder> optionalWorkOrder = workOrder == null ? Optional.empty() : findById(workOrder.getId());
            return workOrder == null || (optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompanyId().equals(companyId));
        } else {
            Optional<WorkOrder> optionalWorkOrder = findById(workOrder.getId());
            return optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompanyId().equals(companyId);
        }
    }

    public Collection<WorkOrder> findByDueDateBetweenAndCompany(Date date1, Date date2, Long id) {
        return workOrderRepository.findByDueDateBetweenAndCompanyId(date1, date2, id);
    }

    public void importWorkOrder(WorkOrder workOrder, WorkOrderImportDTO dto, Company company) {
        Long companySettingsId = company.getCompanySettings().getId();
        Long companyId = company.getId();
        workOrder.setDueDate(Helper.getDateFromExcelDate(dto.getDueDate()));
        workOrder.setPriority(Priority.getPriorityFromString(dto.getPriority()));
        workOrder.setEstimatedDuration(dto.getEstimatedDuration());
        workOrder.setDescription(dto.getDescription());
        workOrder.setTitle(dto.getTitle());
        workOrder.setRequiredSignature(Helper.getBooleanFromString(dto.getRequiredSignature()));
        Optional<WorkOrderCategory> optionalWorkOrderCategory = workOrderCategoryService.findByNameAndCompanySettings(dto.getCategory(), companySettingsId);
        optionalWorkOrderCategory.ifPresent(workOrder::setCategory);
        Optional<Location> optionalLocation = locationService.findByNameAndCompany(dto.getLocationName(), companyId);
        optionalLocation.ifPresent(workOrder::setLocation);
        Optional<Team> optionalTeam = teamService.findByNameAndCompany(dto.getTeamName(), companyId);
        optionalTeam.ifPresent(workOrder::setTeam);
        Optional<OwnUser> optionalPrimaryUser = userService.findByEmailAndCompany(dto.getPrimaryUserEmail(), companyId);
        optionalPrimaryUser.ifPresent(workOrder::setPrimaryUser);
        List<OwnUser> assignedTo = new ArrayList<>();
        dto.getAssignedToEmails().forEach(email -> {
            Optional<OwnUser> optionalUser1 = userService.findByEmailAndCompany(email, companyId);
            optionalUser1.ifPresent(assignedTo::add);
        });
        workOrder.setAssignedTo(assignedTo);
        Optional<Asset> optionalAsset = assetService.findByNameAndCompany(dto.getAssetName(), companyId);
        optionalAsset.ifPresent(workOrder::setAsset);
        Optional<OwnUser> optionalCompletedBy = userService.findByEmailAndCompany(dto.getCompletedByEmail(), companyId);
        optionalCompletedBy.ifPresent(workOrder::setCompletedBy);
        workOrder.setCompletedOn(Helper.getDateFromExcelDate(dto.getCompletedOn()));
        workOrder.setArchived(Helper.getBooleanFromString(dto.getArchived()));
        workOrder.setStatus(Status.getStatusFromString(dto.getStatus()));
        workOrder.setFeedback(dto.getFeedback());
        List<Customer> customers = new ArrayList<>();
        dto.getCustomersNames().forEach(name -> {
            Optional<Customer> optionalCustomer = customerService.findByNameAndCompany(name, companyId);
            optionalCustomer.ifPresent(customers::add);
        });
        workOrder.setCustomers(customers);
        workOrderRepository.save(workOrder);
    }

    public Collection<WorkOrder> findByCreatedByAndCreatedAtBetween(Long id, Date date1, Date date2) {
        return workOrderRepository.findByCreatedByAndCreatedAtBetween(id, date1, date2);
    }

    public Collection<WorkOrder> findByCompletedByAndCreatedAtBetween(Long id, Date date1, Date date2) {
        return workOrderRepository.findByCompletedBy_IdAndCreatedAtBetween(id, date1, date2);
    }
}
