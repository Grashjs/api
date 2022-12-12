package com.grash.mapper;

import com.grash.dto.UserMiniDTO;
import com.grash.dto.UserPatchDTO;
import com.grash.dto.UserResponseDTO;
import com.grash.dto.UserSignupRequest;
import com.grash.model.OwnUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {
    OwnUser updateUser(@MappingTarget OwnUser entity, UserPatchDTO dto);

    @Mappings({@Mapping(source = "company.id", target = "companyId"),
            @Mapping(source = "company.companySettings.id", target = "companySettingsId"),
            @Mapping(source = "userSettings.id", target = "userSettingsId")})
    UserResponseDTO toDto(OwnUser model);

    @Mappings({})
    OwnUser toModel(UserSignupRequest dto);

    UserMiniDTO toMiniDto(OwnUser user);
}
