package com.grash.controller;

import com.grash.dto.analytics.assets.*;
import com.grash.exception.CustomException;
import com.grash.model.Asset;
import com.grash.model.AssetDowntime;
import com.grash.model.OwnUser;
import com.grash.model.WorkOrder;
import com.grash.model.enums.Status;
import com.grash.service.AssetDowntimeService;
import com.grash.service.AssetService;
import com.grash.service.UserService;
import com.grash.service.WorkOrderService;
import com.grash.utils.AuditComparator;
import com.grash.utils.DowntimeComparator;
import com.grash.utils.Helper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analytics/assets")
@Api(tags = "AssetAnalytics")
@RequiredArgsConstructor
public class AssetAnalyticsController {

    private final WorkOrderService workOrderService;
    private final UserService userService;
    private final AssetService assetService;
    private final AssetDowntimeService assetDowntimeService;

    @GetMapping("/time-cost")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public Collection<TimeCostByAsset> getTimeCostByAsset(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.canSeeAnalytics()) {
            Collection<Asset> assets = assetService.findByCompany(user.getCompany().getId());
            Collection<TimeCostByAsset> result = new ArrayList<>();
            assets.forEach(asset -> {
                Collection<WorkOrder> completeWO = workOrderService.findByAsset(asset.getId())
                        .stream().filter(workOrder -> workOrder.getStatus().equals(Status.COMPLETE)).collect(Collectors.toList());
                double time = workOrderService.getLaborCostAndTime(completeWO).getSecond();
                double cost = workOrderService.getAllCost(completeWO);
                result.add(TimeCostByAsset.builder()
                        .time(time)
                        .cost(cost)
                        .name(asset.getName())
                        .id(asset.getId())
                        .build());
            });
            return result;
        } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public AssetStats getOverviewStats(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.canSeeAnalytics()) {
            Collection<AssetDowntime> downtimes = assetDowntimeService.findByCompany(user.getCompany().getId());
            long downtimesDuration = downtimes.stream().mapToLong(AssetDowntime::getDuration).sum();
            Collection<Asset> assets = assetService.findByCompany(user.getCompany().getId());
            long ages = assets.stream().mapToLong(Asset::getAge).sum();
            long availability = (ages - downtimesDuration) * 100 / ages;
            return AssetStats.builder()
                    .downtime(downtimesDuration)
                    .availability(availability)
                    .downtimeEvents(downtimes.size())
                    .build();
        } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/downtimes")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public Collection<DowntimesByAsset> getDowntimesByAsset(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.canSeeAnalytics()) {
            Collection<Asset> assets = assetService.findByCompany(user.getCompany().getId());
            return assets.stream().map(asset -> {
                Collection<AssetDowntime> downtimes = assetDowntimeService.findByAsset(asset.getId());
                long downtimesDuration = downtimes.stream().mapToLong(AssetDowntime::getDuration).sum();
                long percent = downtimesDuration * 100 / asset.getAge();
                return DowntimesByAsset.builder()
                        .count(downtimes.size())
                        .percent(percent)
                        .id(asset.getId())
                        .name(asset.getName())
                        .build();
            }).collect(Collectors.toList());
        } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/meantimes")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public Meantimes getMeantimes(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.canSeeAnalytics()) {
            Collection<AssetDowntime> downtimes = assetDowntimeService.findByCompany(user.getCompany().getId());
            long betweenDowntimes = 0L;
            long betweenMaintenances = 0L;
            if (downtimes.size() > 2) {
                DowntimeComparator downtimeComparator = new DowntimeComparator();
                AssetDowntime firstDowntime = Collections.max(downtimes, downtimeComparator);
                AssetDowntime lastDowntime = Collections.min(downtimes, downtimeComparator);
                betweenDowntimes = (Helper.getDateDiff(firstDowntime.getStartsOn(), lastDowntime.getStartsOn(), TimeUnit.HOURS)) / (downtimes.size() - 1);
            }
            Collection<WorkOrder> workOrders = workOrderService.findByCompany(user.getCompany().getId());
            if (workOrders.size() > 2) {
                AuditComparator auditComparator = new AuditComparator();
                WorkOrder firstWorkOrder = Collections.max(workOrders, auditComparator);
                WorkOrder lastWorkOrder = Collections.min(workOrders, auditComparator);
                betweenDowntimes = (Helper.getDateDiff(Date.from(firstWorkOrder.getCreatedAt()), Date.from(lastWorkOrder.getCreatedAt()), TimeUnit.HOURS)) / (workOrders.size() - 1);
            }
            return Meantimes.builder()
                    .betweenDowntimes(betweenDowntimes)
                    .betweenMaintenances(betweenMaintenances)
                    .build();
        } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/repair-times")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public Collection<RepairTimeByAsset> getRepairTimeByAsset(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.canSeeAnalytics()) {
            Collection<Asset> assets = assetService.findByCompany(user.getCompany().getId());
            return assets.stream().map(asset -> {
                Collection<WorkOrder> completeWO = workOrderService.findByAsset(asset.getId()).stream().filter(workOrder -> workOrder.getStatus().equals(Status.COMPLETE)).collect(Collectors.toList());
                return RepairTimeByAsset.builder()
                        .id(asset.getId())
                        .name(asset.getName())
                        .duration(Helper.getAverageAge(completeWO))
                        .build();
            }).collect(Collectors.toList());
        } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
    }
}
