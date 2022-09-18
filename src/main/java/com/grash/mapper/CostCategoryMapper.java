package com.grash.mapper;

import com.grash.dto.CategoryPatchDTO;
import com.grash.model.CostCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CostCategoryMapper {
    CostCategory updateCostCategory(@MappingTarget CostCategory entity, CategoryPatchDTO dto);

    @Mappings({})
    CategoryPatchDTO toDto(CostCategory model);
}
