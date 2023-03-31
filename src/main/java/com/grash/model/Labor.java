package com.grash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.abstracts.Time;
import com.grash.model.enums.TimeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Labor extends Time {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private OwnUser assignedTo;

    private boolean includeToTotalTime = true;

    private boolean logged = false;

    private long hourlyRate;

    private Date startedAt;

    private TimeStatus status = TimeStatus.STOPPED;

    @ManyToOne
    private TimeCategory timeCategory;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private WorkOrder workOrder;

    public long getCost() {
        return hourlyRate * this.getDuration() / 3600;
    }

    public Labor(OwnUser user, long hourlyRate, Date startedAt, WorkOrder workOrder, Company company, boolean logged, TimeStatus status) {
        this.assignedTo = user;
        this.hourlyRate = hourlyRate;
        this.startedAt = startedAt;
        this.workOrder = workOrder;
        this.setCompanyId(company.getId());
        this.status = status;
        this.logged = logged;
    }
}
