package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grash.model.abstracts.WorkOrderBase;
import com.grash.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder extends WorkOrderBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private OwnUser completedBy;

    private Date completedOn;

    private Status status = Status.OPEN;

    @OneToOne
    private File signature;

    private boolean archived;

    @ManyToOne
    @JsonIgnore
    private Request parentRequest;


    @ManyToOne
    private PreventiveMaintenance parentPreventiveMaintenance;

}
