package com.grash.service;

import com.grash.dto.AssetPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.AssetMapper;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.RoleType;
import com.grash.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final LocationService locationService;
    private final ImageService imageService;
    private final AssetCategoryService assetCategoryService;
    private final DeprecationService deprecationService;
    private final UserService userService;
    private final CompanyService companyService;
    private final NotificationService notificationService;
    private final AssetMapper assetMapper;

    public Asset create(Asset Asset) {
        return assetRepository.save(Asset);
    }

    public Asset update(Long id, AssetPatchDTO asset) {
        if (assetRepository.existsById(id)) {
            Asset savedAsset = assetRepository.findById(id).get();
            return assetRepository.save(assetMapper.updateAsset(savedAsset, asset));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Asset save(Asset asset) {
        return assetRepository.save(asset);
    }

    public Collection<Asset> getAll() {
        return assetRepository.findAll();
    }

    public void delete(Long id) {
        assetRepository.deleteById(id);
    }

    public Optional<Asset> findById(Long id) {
        return assetRepository.findById(id);
    }

    public Collection<Asset> findByCompany(Long id) {
        return assetRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(User user, Asset asset) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(asset.getCompany().getId());
    }

    public boolean canCreate(User user, Asset assetReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(assetReq.getCompany().getId());
        Optional<Location> optionalLocation = locationService.findById(assetReq.getLocation().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);
        boolean second = optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId);

        boolean third = assetReq.getAssignedTo() == null || assetReq.getAssignedTo().stream().allMatch(user1 -> {
            Optional<User> optionalUser = userService.findById(user1.getId());
            return optionalUser.map(value -> value.getCompany().getId().equals(companyId)).orElse(false);
        });

        return first && second && third && canPatch(user, assetMapper.toDto(assetReq));
    }

    public boolean canPatch(User user, AssetPatchDTO assetReq) {
        Long companyId = user.getCompany().getId();

        Optional<Location> optionalLocation = assetReq.getLocation() == null ? Optional.empty() : locationService.findById(assetReq.getLocation().getId());
        Optional<Image> optionalImage = assetReq.getImage() == null ? Optional.empty() : imageService.findById(assetReq.getImage().getId());
        Optional<AssetCategory> optionalAssetCategory = assetReq.getCategory() == null ? Optional.empty() : assetCategoryService.findById(assetReq.getCategory().getId());
        Optional<Asset> optionalParentAsset = assetReq.getParentAsset() == null ? Optional.empty() : findById(assetReq.getParentAsset().getId());
        Optional<User> optionalUser = assetReq.getPrimaryUser() == null ? Optional.empty() : userService.findById(assetReq.getPrimaryUser().getId());
        Optional<Deprecation> optionalDeprecation = assetReq.getDeprecation() == null ? Optional.empty() : deprecationService.findById(assetReq.getDeprecation().getId());

        //optional fields
        boolean second = assetReq.getLocation() == null || (optionalLocation.isPresent() && optionalLocation.get().getCompany().getId().equals(companyId));
        boolean third = assetReq.getImage() == null || (optionalImage.isPresent() && optionalImage.get().getCompany().getId().equals(companyId));
        boolean fourth = assetReq.getCategory() == null || (optionalAssetCategory.isPresent() && optionalAssetCategory.get().getCompanySettings().getCompany().getId().equals(companyId));
        boolean fifth = assetReq.getParentAsset() == null || (optionalParentAsset.isPresent() && optionalParentAsset.get().getCompany().getId().equals(companyId));
        boolean sixth = assetReq.getPrimaryUser() == null || (optionalUser.isPresent() && optionalUser.get().getCompany().getId().equals(companyId));
        boolean seventh = assetReq.getDeprecation() == null || (optionalDeprecation.isPresent() && optionalDeprecation.get().getCompany().getId().equals(companyId));

        boolean eighth = assetReq.getAssignedTo() == null || assetReq.getAssignedTo().stream().allMatch(user1 -> {
            Optional<User> optionalUser1 = userService.findById(user1.getId());
            return optionalUser1.map(value -> value.getCompany().getId().equals(companyId)).orElse(false);
        });

        return second && third && fourth && fifth && sixth && seventh && eighth;
    }

    public void notify(Asset asset) {
        String message = "Asset " + asset.getName() + " has been assigned to you";
        if (asset.getPrimaryUser() != null) {
            notificationService.create(new Notification(message, asset.getPrimaryUser(), NotificationType.ASSET, asset.getId()));
        }
        if (asset.getAssignedTo() != null) {
            asset.getAssignedTo().forEach(assignedUser ->
                    notificationService.create(new Notification(message, assignedUser, NotificationType.ASSET, asset.getId())));
        }
    }

    public void patchNotify(Asset oldAsset, Asset newAsset) {
        String message = "Asset " + newAsset.getName() + " has been assigned to you";
        if (newAsset.getPrimaryUser() != null && !newAsset.getPrimaryUser().getId().equals(oldAsset.getPrimaryUser().getId())) {
            notificationService.create(new Notification(message, newAsset.getPrimaryUser(), NotificationType.ASSET, newAsset.getId()));
        }
        if (newAsset.getAssignedTo() != null) {
            List<User> newUsers = newAsset.getAssignedTo().stream().filter(
                    user -> oldAsset.getAssignedTo().stream().noneMatch(user1 -> user1.getId().equals(user.getId()))).collect(Collectors.toList());
            newUsers.forEach(newUser ->
                    notificationService.create(new Notification(message, newUser, NotificationType.ASSET, newAsset.getId())));
        }
    }
}
