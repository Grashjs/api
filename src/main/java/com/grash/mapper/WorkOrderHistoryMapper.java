package com.grash.mapper;

import com.grash.dto.WorkOrderHistoryShowDTO;
import com.grash.model.WorkOrderHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface WorkOrderHistoryMapper {
    @Mappings({})
    WorkOrderHistoryShowDTO toShowDto(WorkOrderHistory model);
}
