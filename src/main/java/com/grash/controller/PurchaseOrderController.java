package com.grash.controller;

import com.grash.dto.PurchaseOrderPatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.OwnUser;
import com.grash.model.PurchaseOrder;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.PlanFeatures;
import com.grash.model.enums.RoleType;
import com.grash.service.PurchaseOrderService;
import com.grash.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/purchase-orders")
@Api(tags = "purchaseOrder")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "PurchaseOrderCategory not found")})
    public Collection<PurchaseOrder> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            if (user.getRole().getViewPermissions().contains(PermissionEntity.PURCHASE_ORDERS)) {
                return purchaseOrderService.findByCompany(user.getCompany().getId()).stream().filter(purchaseOrder -> {
                    boolean canViewOthers = user.getRole().getViewOtherPermissions().contains(PermissionEntity.PURCHASE_ORDERS);
                    return canViewOthers || purchaseOrder.getCreatedBy().equals(user.getId());
                }).collect(Collectors.toList());
            } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        } else return purchaseOrderService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "PurchaseOrder not found")})
    public PurchaseOrder getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<PurchaseOrder> optionalPurchaseOrder = purchaseOrderService.findById(id);
        if (optionalPurchaseOrder.isPresent()) {
            PurchaseOrder savedPurchaseOrder = optionalPurchaseOrder.get();
            if (purchaseOrderService.hasAccess(user, savedPurchaseOrder) && user.getRole().getViewPermissions().contains(PermissionEntity.PURCHASE_ORDERS) &&
                    (user.getRole().getViewOtherPermissions().contains(PermissionEntity.PURCHASE_ORDERS) || savedPurchaseOrder.getCreatedBy().equals(user.getId()))) {
                return savedPurchaseOrder;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public PurchaseOrder create(@ApiParam("PurchaseOrder") @Valid @RequestBody PurchaseOrder purchaseOrderReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (purchaseOrderService.canCreate(user, purchaseOrderReq) && user.getRole().getCreatePermissions().contains(PermissionEntity.PURCHASE_ORDERS)
                && user.getCompany().getSubscription().getSubscriptionPlan().getFeatures().contains(PlanFeatures.PURCHASE_ORDER)) {
            return purchaseOrderService.create(purchaseOrderReq);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "PurchaseOrder not found")})
    public PurchaseOrder patch(@ApiParam("PurchaseOrder") @Valid @RequestBody PurchaseOrderPatchDTO purchaseOrder, @ApiParam("id") @PathVariable("id") Long id,
                               HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<PurchaseOrder> optionalPurchaseOrder = purchaseOrderService.findById(id);

        if (optionalPurchaseOrder.isPresent()) {
            PurchaseOrder savedPurchaseOrder = optionalPurchaseOrder.get();
            if (purchaseOrderService.hasAccess(user, savedPurchaseOrder) && purchaseOrderService.canPatch(user, purchaseOrder)
                    && user.getRole().getEditOtherPermissions().contains(PermissionEntity.PURCHASE_ORDERS) || savedPurchaseOrder.getCreatedBy().equals(user.getId())) {
                return purchaseOrderService.update(id, purchaseOrder);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("PurchaseOrder not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "PurchaseOrder not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<PurchaseOrder> optionalPurchaseOrder = purchaseOrderService.findById(id);
        if (optionalPurchaseOrder.isPresent()) {
            PurchaseOrder savedPurchaseOrder = optionalPurchaseOrder.get();
            if (purchaseOrderService.hasAccess(user, savedPurchaseOrder)
                    && (savedPurchaseOrder.getCreatedBy().equals(user.getId()) ||
                    user.getRole().getDeleteOtherPermissions().contains(PermissionEntity.PURCHASE_ORDERS))) {
                purchaseOrderService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("PurchaseOrder not found", HttpStatus.NOT_FOUND);
    }

}
