package com.grash.model;

import com.grash.model.abstracts.CategoryAbstract;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class PartCategory extends CategoryAbstract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public PartCategory(String name, CompanySettings companySettings) {
        super(name, companySettings);
    }
}
