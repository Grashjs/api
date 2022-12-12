package com.grash.model.abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.*;
import com.grash.model.enums.Priority;
import lombok.Data;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@MappedSuperclass
public abstract class WorkOrderBase extends CompanyAudit {
    private Date dueDate;
    private Priority priority = Priority.NONE;
    private int estimatedDuration;
    private String description;
    @NotNull
    private String title;
    private boolean requiredSignature;

    @OneToOne
    private File image;

    @ManyToOne
    private WorkOrderCategory category;

    @ManyToOne
    private Location location;

    @ManyToOne
    private Team team;

    @ManyToOne
    private OwnUser primaryUser;

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<OwnUser> assignedTo = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Customer> customers = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<File> files = new ArrayList<>();

    @ManyToOne
    private Asset asset;

}
