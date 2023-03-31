package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.dto.VendorPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.VendorMapper;
import com.grash.model.OwnUser;
import com.grash.model.Vendor;
import com.grash.model.enums.RoleType;
import com.grash.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VendorService {
    private final VendorRepository vendorRepository;
    private final CompanyService companyService;
    private final VendorMapper vendorMapper;
    private AssetService assetService;
    private LocationService locationService;
    private PartService partService;

    @Autowired
    public void setDeps(@Lazy PartService partService, @Lazy LocationService locationService, @Lazy AssetService assetService
    ) {
        this.partService = partService;
        this.locationService = locationService;
        this.assetService = assetService;
    }

    public Vendor create(Vendor Vendor) {
        return vendorRepository.save(Vendor);
    }

    public Vendor update(Long id, VendorPatchDTO vendor) {
        if (vendorRepository.existsById(id)) {
            Vendor savedVendor = vendorRepository.findById(id).get();
            return vendorRepository.save(vendorMapper.updateVendor(savedVendor, vendor));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Vendor> getAll() {
        return vendorRepository.findAll();
    }

    public void delete(Long id) {
        vendorRepository.deleteById(id);
    }

    public Optional<Vendor> findById(Long id) {
        return vendorRepository.findById(id);
    }

    public Collection<Vendor> findByCompany(Long id) {
        return vendorRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, Vendor vendor) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(vendor.getCompanyId());
    }

    public boolean canCreate(OwnUser user, Vendor vendorReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = vendorReq.getCompanyId().equals(companyId);
        boolean second = vendorReq.getAssets() == null || vendorReq.getAssets().stream().allMatch(item ->
                assetService.isAssetInCompany(item, companyId, false));
        boolean third = vendorReq.getParts() == null || vendorReq.getParts().stream().allMatch(item ->
                partService.isPartInCompany(item, companyId, false));
        boolean fifth = vendorReq.getLocations() == null || vendorReq.getLocations().stream().allMatch(item ->
                locationService.isLocationInCompany(item, companyId, false));

        return first && second && third && fifth && canPatch(user, vendorMapper.toPatchDto(vendorReq));
    }

    public boolean canPatch(OwnUser user, VendorPatchDTO vendorReq) {
        return true;
    }

    public boolean isVendorInCompany(Vendor vendor, long companyId, boolean optional) {
        if (optional) {
            Optional<Vendor> optionalVendor = vendor == null ? Optional.empty() : findById(vendor.getId());
            return vendor == null || (optionalVendor.isPresent() && optionalVendor.get().getCompanyId().equals(companyId));
        } else {
            Optional<Vendor> optionalVendor = findById(vendor.getId());
            return optionalVendor.isPresent() && optionalVendor.get().getCompanyId().equals(companyId);
        }
    }

    public Page<Vendor> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<Vendor> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return vendorRepository.findAll(builder.build(), page);
    }

    public Optional<Vendor> findByNameAndCompany(String name, Long companyId) {
        return vendorRepository.findByNameAndCompanyId(name, companyId);
    }
}
