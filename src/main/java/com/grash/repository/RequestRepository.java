package com.grash.repository;

import com.grash.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;

public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {
    Collection<Request> findByCompanyId(@Param("x") Long id);

    Collection<Request> findByCreatedAtBetweenAndCompanyId(Date date1, Date date2, Long id);
}
