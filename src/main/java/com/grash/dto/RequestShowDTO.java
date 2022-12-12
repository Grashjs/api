package com.grash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestShowDTO extends WorkOrderBaseShowDTO {
    private boolean cancelled;

    private WorkOrderMiniDTO workOrder;
}
