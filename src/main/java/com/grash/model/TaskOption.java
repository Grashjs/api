package com.grash.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.grash.model.abstracts.CompanyAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskOption extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_base_id")
    @JsonBackReference
    private TaskBase taskBase;

    public TaskOption(String label, Company company, TaskBase taskBase) {
        this.label = label;
        this.taskBase = taskBase;
        this.setCompanyId(company.getId());
    }
}
