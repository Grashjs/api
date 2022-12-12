package com.grash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.abstracts.Time;
import com.grash.model.enums.TimeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class AdditionalTime extends Time {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private OwnUser assignedTo;

    private boolean includeToTotalTime = true;

    private boolean primaryTime = false;

    private double hourlyRate;

    private Date startedAt;

    private TimeStatus status = TimeStatus.STOPPED;

    @ManyToOne
    private TimeCategory timeCategory;

    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private WorkOrder workOrder;

    public AdditionalTime(OwnUser user, double hourlyRate, Date startedAt, WorkOrder workOrder, Company company, boolean primaryTime, TimeStatus status) {
        this.assignedTo = user;
        this.hourlyRate = hourlyRate;
        this.startedAt = startedAt;
        this.workOrder = workOrder;
        this.setCompany(company);
        this.status = status;
        this.primaryTime = primaryTime;
    }
}
