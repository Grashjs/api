package com.grash.dto;

import com.grash.model.File;
import com.grash.model.PreventiveMaintenance;
import com.grash.model.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class WorkOrderShowDTO extends WorkOrderBaseShowDTO {

    private UserMiniDTO completedBy;

    private Date completedOn;

    private boolean archived;

    private RequestMiniDTO parentRequest;

    private PreventiveMaintenance parentPreventiveMaintenance;

    private File signature;

    private Status status;

}
