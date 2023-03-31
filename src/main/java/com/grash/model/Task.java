package com.grash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.abstracts.CompanyAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Task extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @NotNull
    private TaskBase taskBase;

    private String notes;

    private String value;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<File> images = new ArrayList<>();

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private WorkOrder workOrder;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private PreventiveMaintenance preventiveMaintenance;

    public Task(TaskBase taskBase, WorkOrder workOrder, PreventiveMaintenance preventiveMaintenance, Company company, String value) {
        this.taskBase = taskBase;
        this.workOrder = workOrder;
        this.value = value;
        this.preventiveMaintenance = preventiveMaintenance;
        this.setCompanyId(company.getId());
    }
}
