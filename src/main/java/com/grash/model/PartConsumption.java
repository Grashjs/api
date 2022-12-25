package com.grash.model;

import com.grash.model.abstracts.CompanyAudit;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
public class PartConsumption extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Min(value = 0L, message = "The value must be positive")
    private int quantity;

    @ManyToOne
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Part part;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkOrder workOrder;

    public PartConsumption(Company company, Part part, WorkOrder workOrder, int quantity) {
        this.setCompany(company);
        this.part = part;
        this.workOrder = workOrder;
        this.quantity = quantity;
    }
}
