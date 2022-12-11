package com.grash.model.abstracts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.grash.model.OwnUser;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
@JsonDeserialize
public abstract class Worker extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double rate;

    public static boolean isUser(Worker worker) {
        return worker.getClass().equals(OwnUser.class);
    }
}
