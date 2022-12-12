package com.grash.mapper;

import com.grash.dto.PartQuantityPatchDTO;
import com.grash.dto.PartQuantityShowDTO;
import com.grash.model.PartQuantity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {PartMapper.class, PurchaseOrderMapper.class, WorkOrderMapper.class})
public interface PartQuantityMapper {
    PartQuantity updatePartQuantity(@MappingTarget PartQuantity entity, PartQuantityPatchDTO dto);

    @Mappings({})
    PartQuantityPatchDTO toDto(PartQuantity model);

    PartQuantityShowDTO toShowDto(PartQuantity model);

}
