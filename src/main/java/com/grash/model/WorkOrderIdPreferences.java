package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
public class WorkOrderIdPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String correctivePrefix;
    private String preventivePrefix;
    @OneToOne
    @NotNull
    @JsonIgnore
    private CompanySettings companySettings;

    public WorkOrderIdPreferences(CompanySettings companySettings) {
        this.companySettings = companySettings;
    }
}
