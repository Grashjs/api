package com.grash.dto;

import com.grash.model.File;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
public class PartShowDTO extends AuditShowDTO {
    private Long id;

    private String name;

    private double cost;

    private String category;

    private boolean nonStock;

    private String barcode;

    private String description;

    private int quantity;

    private String additionalInfos;

    private String area;

    private int minQuantity;

    private LocationMiniDTO location;

    private File image;

    private Collection<UserMiniDTO> assignedTo;

    private Collection<File> files;

    private Collection<CustomerMiniDTO> customers;

    private Collection<VendorMiniDTO> vendors;

    private Collection<TeamMiniDTO> teams;
}
