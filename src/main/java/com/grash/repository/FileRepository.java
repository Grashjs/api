package com.grash.repository;

import com.grash.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;

public interface FileRepository extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    Collection<File> findByCompanyId(Long id);
}
