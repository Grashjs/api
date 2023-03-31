package com.grash.repository;

import com.grash.model.WorkOrder;
import com.grash.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
    Collection<WorkOrder> findByCompanyId(Long id);

    Collection<WorkOrder> findByAsset_Id(Long id);

    Collection<WorkOrder> findByLocation_Id(Long id);

    Collection<WorkOrder> findByParentPreventiveMaintenance_Id(Long id);

    Collection<WorkOrder> findByPrimaryUser_Id(Long id);

    Collection<WorkOrder> findByCompletedBy_Id(Long id);

    Collection<WorkOrder> findByPriorityAndCompanyId(Priority priority, Long companyId);

    Collection<WorkOrder> findByCategory_Id(Long id);

    Collection<WorkOrder> findByCompletedOnBetweenAndCompanyId(Date date1, Date date2, Long id);

    Collection<WorkOrder> findByCreatedBy(Long id);

    Collection<WorkOrder> findByDueDateBetweenAndCompanyId(Date date1, Date date2, Long id);

    Optional<WorkOrder> findByIdAndCompanyId(Long id, Long companyId);

    Collection<WorkOrder> findByCreatedByAndCreatedAtBetween(Long id, Date date1, Date date2);

    Collection<WorkOrder> findByCompletedBy_IdAndCreatedAtBetween(Long id, Date date1, Date date2);
}
