package com.grash.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.abstracts.CompanyAudit;
import com.grash.model.enums.AssetStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Asset extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private boolean archived;

    private boolean hasChildren;

    @OneToOne
    private File image;

    @ManyToOne
    private Location location;

    @ManyToOne
    private Asset parentAsset;

    private String area;

    private String description;

    private String barCode;

    private Date downAt;

    @ManyToOne
    private AssetCategory category;

    @NotNull
    private String name;

    @ManyToOne
    private OwnUser primaryUser;

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Asset_User_Associations",
            joinColumns = @JoinColumn(name = "id_asset"),
            inverseJoinColumns = @JoinColumn(name = "id_user"),
            indexes = {
                    @Index(name = "idx_asset_user_asset_id", columnList = "id_asset"),
                    @Index(name = "idx_asset_user_user_id", columnList = "id_user")
            })
    private List<OwnUser> assignedTo = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Asset_Team_Associations",
            joinColumns = @JoinColumn(name = "id_asset"),
            inverseJoinColumns = @JoinColumn(name = "id_team"),
            indexes = {
                    @Index(name = "idx_asset_team_asset_id", columnList = "id_asset"),
                    @Index(name = "idx_asset_team_team_id", columnList = "id_team")
            })
    private List<Team> teams = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Asset_Vendor_Associations",
            joinColumns = @JoinColumn(name = "id_asset"),
            inverseJoinColumns = @JoinColumn(name = "id_vendor"),
            indexes = {
                    @Index(name = "idx_asset_vendor_asset_id", columnList = "id_asset"),
                    @Index(name = "idx_asset_vendor_vendor_id", columnList = "id_vendor")
            })
    private List<Vendor> vendors = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Asset_Customer_Associations",
            joinColumns = @JoinColumn(name = "id_asset"),
            inverseJoinColumns = @JoinColumn(name = "id_customer"),
            indexes = {
                    @Index(name = "idx_asset_customer_asset_id", columnList = "id_asset"),
                    @Index(name = "idx_asset_customer_customer_id", columnList = "id_customer")
            })
    private List<Customer> customers = new ArrayList<>();

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Asset_Part_Associations",
            joinColumns = @JoinColumn(name = "id_asset"),
            inverseJoinColumns = @JoinColumn(name = "id_part"),
            indexes = {
                    @Index(name = "idx_asset_part_asset_id", columnList = "id_asset"),
                    @Index(name = "idx_asset_part_part_id", columnList = "id_part")
            })
    private List<Part> parts = new ArrayList<>();

    @OneToOne
    private Deprecation deprecation;

    private Date warrantyExpirationDate;

    private Date inServiceDate;

    private String additionalInfos;

    private String serialNumber;

    private String model;

    private AssetStatus status = AssetStatus.OPERATIONAL;

    private long uptime;

    private long downtime = 0L;

    @ManyToMany
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinTable(name = "T_Asset_File_Associations",
            joinColumns = @JoinColumn(name = "id_asset"),
            inverseJoinColumns = @JoinColumn(name = "id_file"),
            indexes = {
                    @Index(name = "idx_asset_file_asset_id", columnList = "id_asset"),
                    @Index(name = "idx_asset_file_file_id", columnList = "id_file")
            })
    private List<File> files = new ArrayList<>();

}



