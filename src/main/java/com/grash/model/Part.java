package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.abstracts.CompanyAudit;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Part_User_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_user"),
            indexes = {
                    @Index(name = "idx_part_user_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_user_user_id", columnList = "id_user")
            })
    private List<OwnUser> assignedTo = new ArrayList<>();

    private String barcode;

    private String description;

    private String category;

    private int quantity;

    private String area;

    private String additionalInfos;

    private boolean nonStock;

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Part_File_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_file"),
            indexes = {
                    @Index(name = "idx_part_file_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_file_file_id", columnList = "id_file")
            })
    private List<File> files = new ArrayList<>();

    @OneToOne
    private File image;

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Part_Customer_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_customer"),
            indexes = {
                    @Index(name = "idx_part_customer_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_customer_customer_id", columnList = "id_customer")
            })
    private List<Customer> customers = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Part_Vendor_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_vendor"),
            indexes = {
                    @Index(name = "idx_part_vendor_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_vendor_vendor_id", columnList = "id_vendor")
            })
    private List<Vendor> vendors = new ArrayList<>();
    
    @ManyToMany
    @JsonIgnore
    private List<PreventiveMaintenance> preventiveMaintenances = new ArrayList<>();

    private int minQuantity;

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Part_Team_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_team"),
            indexes = {
                    @Index(name = "idx_part_team_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_team_team_id", columnList = "id_team")
            })
    private List<Team> teams = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Asset_Part_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_asset"),
            indexes = {
                    @Index(name = "idx_part_asset_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_asset_asset_id", columnList = "id_asset")
            })
    private List<Asset> assets = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_MultiParts_Part_Associations",
            joinColumns = @JoinColumn(name = "id_part"),
            inverseJoinColumns = @JoinColumn(name = "id_multi_parts"),
            indexes = {
                    @Index(name = "idx_part_multi_parts_part_id", columnList = "id_part"),
                    @Index(name = "idx_part_multi_parts_multi_parts_id", columnList = "id_multi_parts")
            })
    private List<MultiParts> multiParts = new ArrayList<>();
}
