package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grash.model.abstracts.CompanyAudit;
import com.grash.model.enums.FileType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class File extends CompanyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String url;

    private FileType type = FileType.OTHER;
    
    @ManyToOne
    private Task task;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Asset_File_Associations",
            joinColumns = @JoinColumn(name = "id_file"),
            inverseJoinColumns = @JoinColumn(name = "id_asset"),
            indexes = {
                    @Index(name = "idx_file_asset_file_id", columnList = "id_file"),
                    @Index(name = "idx_file_asset_asset_id", columnList = "id_asset")
            })
    private List<Asset> asset = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Part_File_Associations",
            joinColumns = @JoinColumn(name = "id_file"),
            inverseJoinColumns = @JoinColumn(name = "id_part"),
            indexes = {
                    @Index(name = "idx_file_part_file_id", columnList = "id_file"),
                    @Index(name = "idx_file_part_part_id", columnList = "id_part")
            })
    private List<Part> parts = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_Request_File_Associations",
            joinColumns = @JoinColumn(name = "id_file"),
            inverseJoinColumns = @JoinColumn(name = "id_request"),
            indexes = {
                    @Index(name = "idx_file_request_file_id", columnList = "id_file"),
                    @Index(name = "idx_file_request_request_id", columnList = "id_request")
            })
    private List<Request> Requests = new ArrayList<>();

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "T_WorkOrder_File_Associations",
            joinColumns = @JoinColumn(name = "id_file"),
            inverseJoinColumns = @JoinColumn(name = "id_work_order"),
            indexes = {
                    @Index(name = "idx_file_work_order_file_id", columnList = "id_file"),
                    @Index(name = "idx_file_work_order_work_order_id", columnList = "id_work_order")
            })
    private List<WorkOrder> workOrders = new ArrayList<>();

    public File(String name, String url, Company company, FileType fileType) {
        this.name = name;
        this.url = url;
        this.type = fileType;
        this.setCompany(company);
    }
}
