package com.grash.model;

import com.grash.model.enums.FieldType;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class FieldConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fieldName;

    private FieldType fieldType;

    @ManyToOne
    @NotNull
    private WorkRequestConfiguration workRequestConfiguration;

}
