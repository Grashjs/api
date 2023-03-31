package com.grash.repository;

import com.grash.model.PartConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Date;

public interface PartConsumptionRepository extends JpaRepository<PartConsumption, Long> {
    Collection<PartConsumption> findByCompanyId(Long id);

    Collection<PartConsumption> findByWorkOrder_Id(Long id);

    Collection<PartConsumption> findByPart_Id(Long id);

    Collection<PartConsumption> findByCreatedAtBetweenAndCompanyId(Date
                                                                           date1, Date date2, Long companyId);


    Collection<PartConsumption> findByWorkOrder_IdAndPart_Id(Long workOrderId, Long partId);
}
