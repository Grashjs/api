package com.grash.model.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@Data
@FilterDef(name = "companyFilter", parameters = {@ParamDef(name = "companyId", type = "int")}, defaultCondition = "(company_id = :companyId)")
@Filter(name = "companyFilter")
public class CompanyAudit extends Audit {
    @Column(name = "company_id", updatable = false)
    @JsonIgnore
    @NotNull
    private Long companyId;
}
