package com.grash.model;

import com.grash.model.abstracts.BasicInfos;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@NoArgsConstructor
public class Customer extends BasicInfos {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String vendorType;

    private String description;

    private double rate;

    @OneToOne
    private BillingInfos billingInfos;

    @ManyToMany
    @JoinTable( name = "T_Part_customer_Associations",
            joinColumns = @JoinColumn( name = "idCustomer" ),
            inverseJoinColumns = @JoinColumn( name = "idPart" ) )
    private Collection<Part> parts;

}
