package com.grash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestPatchDTO extends WorkOrderBasePatchDTO {
    private boolean cancelled;
}
