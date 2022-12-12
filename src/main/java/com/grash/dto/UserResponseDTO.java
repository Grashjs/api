package com.grash.dto;

import com.grash.model.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserResponseDTO {

    private Integer id;
    @ApiModelProperty(position = 1)
    private String username;
    @ApiModelProperty(position = 2)
    private String email;
    @ApiModelProperty(position = 3)
    private Role role;

    private String jobTitle;

    private String firstName;

    private String lastName;

    private String phone;

    private boolean ownsCompany;

    private Long companyId;

    private Long companySettingsId;

    private Long userSettingsId;

}
