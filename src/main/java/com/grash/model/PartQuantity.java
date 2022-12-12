package com.grash.model;

import com.grash.model.abstracts.CompanyAudit;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
public class PartQuantity extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Min(value = 0L, message = "The value must be positive")
    private int quantity;

    @ManyToOne
    @NotNull
    private Part part;

    @ManyToOne
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    private WorkOrder workOrder;

    public PartQuantity(Company company, Part part, WorkOrder workOrder, PurchaseOrder purchaseOrder, int quantity) {
        this.setCompany(company);
        this.part = part;
        this.workOrder = workOrder;
        this.purchaseOrder = purchaseOrder;
        this.quantity = quantity;
    }
}
