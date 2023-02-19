package com.grash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.RoleCode;
import com.grash.model.enums.RoleType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
public class CompanySettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private GeneralPreferences generalPreferences = new GeneralPreferences(this);

    @OneToOne(cascade = CascadeType.ALL)
    private WorkOrderConfiguration workOrderConfiguration = new WorkOrderConfiguration(this);

    @OneToOne(cascade = CascadeType.ALL)
    private WorkOrderRequestConfiguration WorkOrderRequestConfiguration = new WorkOrderRequestConfiguration(this);

    @OneToOne(cascade = CascadeType.ALL)
    private WorkOrderIdPreferences workOrderIdPreferences = new WorkOrderIdPreferences(this);

    @OneToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Company company;

//    @OneToOne
//   private AssetFieldsConfiguration assetFieldsConfiguration;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companySettings", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Role> roleList = getDefaultRoles();

    public CompanySettings(Company company) {
        this.company = company;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companySettings", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CostCategory> costCategories = new ArrayList<>(createCostCategories(Arrays.asList("Drive cost", "Vendor cost", "Other cost", "Inspection cost", "Wrench cost")));

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companySettings", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TimeCategory> timeCategories = new ArrayList<>(createTimeCategories(Arrays.asList("Drive time", "Vendor time", "Other time", "Inspection time", "Wrench time")));

    private Role createRole(String name,
                            boolean paid,
                            RoleCode code,
                            List<PermissionEntity> createPermissions,
                            List<PermissionEntity> editOtherPermissions,
                            List<PermissionEntity> deleteOtherPermissions,
                            List<PermissionEntity> viewOtherPermissions,
                            List<PermissionEntity> viewPermissions
    ) {
        return Role.builder()
                .roleType(RoleType.ROLE_CLIENT)
                .companySettings(this)
                .code(code)
                .name(name)
                .paid(paid)
                .createPermissions(new HashSet<>(createPermissions))
                .editOtherPermissions(new HashSet<>(editOtherPermissions))
                .deleteOtherPermissions(new HashSet<>(deleteOtherPermissions))
                .viewOtherPermissions(new HashSet<>(viewOtherPermissions))
                .viewPermissions(new HashSet<>(viewPermissions))
                .build();

    }

    private List<CostCategory> createCostCategories(List<String> costCategories) {
        return costCategories.stream().map(costCategory -> new CostCategory(costCategory, this)).collect(Collectors.toList());
    }

    private List<TimeCategory> createTimeCategories(List<String> timeCategories) {
        return timeCategories.stream().map(timeCategory -> new TimeCategory(timeCategory, this)).collect(Collectors.toList());
    }

    private Set<Role> getDefaultRoles() {
        List<PermissionEntity> allEntities = Arrays.asList(PermissionEntity.values());
        return new HashSet<>(Arrays.asList(
                createRole("Administrator", true, RoleCode.ADMIN, allEntities, allEntities, allEntities, allEntities, allEntities),
                createRole("Limited Administrator", true, RoleCode.LIMITED_ADMIN,
                        allEntities.stream().filter(permissionEntity -> !permissionEntity.equals(PermissionEntity.PEOPLE_AND_TEAMS)).collect(Collectors.toList()),
                        allEntities.stream().filter(permissionEntity -> !permissionEntity.equals(PermissionEntity.PEOPLE_AND_TEAMS)).collect(Collectors.toList()),
                        Collections.emptyList(), allEntities, allEntities.stream().filter(permissionEntity -> permissionEntity != PermissionEntity.SETTINGS).collect(Collectors.toList())),
                createRole("Technician", true, RoleCode.TECHNICIAN, Arrays.asList(PermissionEntity.WORK_ORDERS, PermissionEntity.ASSETS, PermissionEntity.LOCATIONS, PermissionEntity.FILES), Collections.emptyList(), Collections.emptyList(), Arrays.asList(PermissionEntity.WORK_ORDERS, PermissionEntity.LOCATIONS, PermissionEntity.ASSETS), Arrays.asList(PermissionEntity.WORK_ORDERS, PermissionEntity.LOCATIONS, PermissionEntity.ASSETS, PermissionEntity.CATEGORIES)),
                createRole("Limited Technician", true, RoleCode.LIMITED_TECHNICIAN, Arrays.asList(PermissionEntity.FILES), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Arrays.asList(PermissionEntity.WORK_ORDERS, PermissionEntity.CATEGORIES)),
                createRole("View Only", false, RoleCode.VIEW_ONLY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), allEntities, allEntities.stream().filter(permissionEntity -> permissionEntity != PermissionEntity.SETTINGS).collect(Collectors.toList())),
                createRole("Requester", false, RoleCode.REQUESTER, Arrays.asList(PermissionEntity.REQUESTS, PermissionEntity.FILES), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Arrays.asList(PermissionEntity.REQUESTS, PermissionEntity.CATEGORIES))
        ));
    }
}
