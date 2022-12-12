package com.grash.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.grash.model.abstracts.CompanyAudit;
import com.grash.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class TaskBase extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String label;

    private TaskType taskType = TaskType.SUBTASK;

    @OneToMany(
            mappedBy = "taskBase",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private Collection<TaskOption> options = new ArrayList<>();

    @ManyToOne
    private OwnUser user;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Meter meter;
}
