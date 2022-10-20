package com.grash.mapper;

import com.grash.dto.CustomerPatchDTO;
import com.grash.dto.CustomerShowDTO;
import com.grash.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer updateCustomer(@MappingTarget Customer entity, CustomerPatchDTO dto);

    @Mappings({})
    CustomerPatchDTO toDto(Customer model);

    CustomerShowDTO toShowDto(Customer model);
}
