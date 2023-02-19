package com.grash.repository;

import com.grash.model.WorkOrderIdPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkOrderIdPreferencesRepository extends JpaRepository<WorkOrderIdPreferences, Long> {

    Collection<WorkOrderIdPreferences> findByCompanySettings_Id(Long id);
}
