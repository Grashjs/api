package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.abstracts.CompanyAudit;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Part extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    private double cost;


    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Part_User_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_user"),
            indexes = {
                    @Index(name = "idx_part_user_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_user_user_id", columnList = "id_user")
            })
    private Set<User> assignedTo;

    private String barcode;

    private String description;

    private int quantity;

    private double area;

    @ManyToOne
    @NotNull
    private Location location;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Part_File_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_file"),
            indexes = {
                    @Index(name = "idx_part_file_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_file_file_id", columnList = "id_file")
            })
    private Set<File> files;

    @OneToOne
    private Image image;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Part_Customer_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_customer"),
            indexes = {
                    @Index(name = "idx_part_customer_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_customer_customer_id", columnList = "id_customer")
            })
    private Set<Customer> assignedCustomer;

    @ManyToMany
    @JsonIgnore
    private Set<WorkOrder> workOrders;

    @ManyToMany
    @JsonIgnore
    private Set<PreventiveMaintenance> preventiveMaintenances;

    private int minQuantity;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Part_Team_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_team"),
            indexes = {
                    @Index(name = "idx_part_team_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_team_team_id", columnList = "id_team")
            })
    private Set<Team> teams;

    @ManyToOne
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Asset asset;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_MultiParts_Part_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_multi_parts"),
            indexes = {
                    @Index(name = "idx_part_multi_parts_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_multi_parts_multi_parts_id", columnList = "id_multi_parts")
            })
    private Set<MultiParts> multiParts;
}
