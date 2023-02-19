package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grash.model.abstracts.WorkOrderBase;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.Status;
import com.grash.utils.Helper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder extends WorkOrderBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String customId;

    @ManyToOne
    private OwnUser completedBy;

    private Date completedOn;

    private Status status = Status.OPEN;

    @OneToOne
    private File signature;

    private boolean archived;

    @ManyToOne
    @JsonIgnore
    private Request parentRequest;

    private String feedback;


    @ManyToOne
    private PreventiveMaintenance parentPreventiveMaintenance;

    @JsonIgnore
    public Collection<OwnUser> getUsers() {
        Collection<OwnUser> users = new ArrayList<>();
        if (this.getPrimaryUser() != null) {
            users.add(this.getPrimaryUser());
        }
        if (this.getTeam() != null) {
            users.addAll(this.getTeam().getUsers());
        }
        if (this.getAssignedTo() != null) {
            users.addAll(this.getAssignedTo());
        }
        return users.stream().collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(OwnUser::getId))),
                ArrayList::new));
    }

    public boolean isAssignedTo(OwnUser user) {
        Collection<OwnUser> users = getUsers();
        return users.stream().anyMatch(user1 -> user1.getId().equals(user.getId()));
    }

    @JsonIgnore
    public boolean isCompliant() {
        return this.getDueDate() == null || this.getCompletedOn().before(this.getDueDate());
    }

    @JsonIgnore
    public boolean isReactive() {
        return this.getParentPreventiveMaintenance() == null;
    }

    @JsonIgnore
    public Date getRealCreatedAt() {
        return this.getParentRequest() == null ? this.getCreatedAt() : this.getParentRequest().getCreatedAt();
    }

    @JsonIgnore
    public List<OwnUser> getNewUsersToNotify(Collection<OwnUser> newUsers) {
        Collection<OwnUser> oldUsers = getUsers();
        return newUsers.stream().filter(newUser -> oldUsers.stream().noneMatch(user -> user.getId().equals(newUser.getId()))).
                collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(OwnUser::getId))),
                        ArrayList::new));
    }

    public boolean canBeEditedBy(OwnUser user) {
        return user.getRole().getEditOtherPermissions().contains(PermissionEntity.WORK_ORDERS)
                || this.getCreatedBy().equals(user.getId()) || isAssignedTo(user);
    }

    //in days
    @JsonIgnore
    public static long getAverageAge(Collection<WorkOrder> completeWorkOrders) {
        List<Long> completionTimes = completeWorkOrders.stream().map(workOrder -> Helper.getDateDiff(workOrder.getCreatedAt(), workOrder.getCompletedOn(), TimeUnit.DAYS)).collect(Collectors.toList());
        return completionTimes.size() == 0 ? 0 : completionTimes.stream().mapToLong(value -> value).sum() / completionTimes.size();
    }
}
