package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.model.File;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private AssetService assetService;
    private PartService partService;
    private RequestService requestService;
    private WorkOrderService workOrderService;
    private LocationService locationService;

    @Autowired
    public void setDeps(@Lazy AssetService assetService, @Lazy PartService partService, @Lazy RequestService requestService, @Lazy LocationService locationService, @Lazy WorkOrderService workOrderService
    ) {
        this.assetService = assetService;
        this.partService = partService;
        this.requestService = requestService;
        this.locationService = locationService;
        this.workOrderService = workOrderService;
    }

    public File create(File File) {
        return fileRepository.save(File);
    }

    public File update(File File) {
        return fileRepository.save(File);
    }

    public Collection<File> getAll() {
        return fileRepository.findAll();
    }

    public void delete(Long id) {
        fileRepository.deleteById(id);
    }

    public Optional<File> findById(Long id) {
        return fileRepository.findById(id);
    }

    public Collection<File> findByCompany(Long id) {
        return fileRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, File file) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(file.getCompanyId());
    }

    public boolean canCreate(OwnUser user, File fileReq) {
        Long companyId = user.getCompany().getId();
        boolean first = fileReq.getAssets() == null || fileReq.getAssets().stream().allMatch(item ->
                assetService.isAssetInCompany(item, companyId, false));
        boolean second = fileReq.getParts() == null || fileReq.getParts().stream().allMatch(item ->
                partService.isPartInCompany(item, companyId, false));
        boolean third = fileReq.getRequests() == null || fileReq.getRequests().stream().allMatch(item ->
                requestService.isRequestInCompany(item, companyId, false));
        boolean fourth = fileReq.getWorkOrders() == null || fileReq.getWorkOrders().stream().allMatch(item ->
                workOrderService.isWorkOrderInCompany(item, companyId, false));
        boolean fifth = fileReq.getLocations() == null || fileReq.getLocations().stream().allMatch(item ->
                locationService.isLocationInCompany(item, companyId, false));
        return first && second && third && fourth && fifth;
    }

    public boolean isFileInCompany(File file, long companyId, boolean optional) {
        if (optional) {
            Optional<File> optionalFile = file == null ? Optional.empty() : findById(file.getId());
            return file == null || (optionalFile.isPresent() && optionalFile.get().getCompanyId().equals(companyId));
        } else {
            Optional<File> optionalFile = findById(file.getId());
            return optionalFile.isPresent() && optionalFile.get().getCompanyId().equals(companyId);
        }
    }

    public Page<File> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<File> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return fileRepository.findAll(builder.build(), page);
    }
}
