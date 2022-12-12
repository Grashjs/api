package com.grash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartQuantityShowDTO extends AuditShowDTO {
    private Long id;

    private int quantity;
    private PartMiniDTO part;

    private PurchaseOrderMiniDTO purchaseOrder;

    private WorkOrderMiniDTO workOrder;
}
