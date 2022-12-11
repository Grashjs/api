package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grash.model.abstracts.Worker;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Customer extends Worker {

    private String customerType;

    private String description;

    private String name;
    private String address;
    private String phone;
    private String website;
    private String email;

    private String billingName;

    private String billingAddress;

    private String billingAddress2;

    @OneToOne
    private Currency billingCurrency;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Part_customer_Associations",
            joinColumns = @JoinColumn(name = "id_customer"),
            inverseJoinColumns = @JoinColumn(name = "id_part"),
            indexes = {
                    @Index(name = "idx_customer_part_customer_id", columnList = "id_customer"),
                    @Index(name = "idx_customer_part_part_id", columnList = "id_part")
            })
    private List<Part> parts = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Location_Customer_Associations",
            joinColumns = @JoinColumn(name = "id_customer"),
            inverseJoinColumns = @JoinColumn(name = "id_location"),
            indexes = {
                    @Index(name = "idx_customer_location_customer_id", columnList = "id_customer"),
                    @Index(name = "idx_customer_location_location_id", columnList = "id_location")
            })
    private List<Location> locations = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Asset_Customer_Associations",
            joinColumns = @JoinColumn(name = "id_customer"),
            inverseJoinColumns = @JoinColumn(name = "id_asset"),
            indexes = {
                    @Index(name = "idx_customer_asset_customer_id", columnList = "id_customer"),
                    @Index(name = "idx_customer_asset_asset_id", columnList = "id_asset")
            })
    private List<Asset> assets = new ArrayList<>();

}
