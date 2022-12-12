package com.grash.repository;

import com.grash.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface FileRepository extends JpaRepository<File, Long> {
    Collection<File> findByCompany_Id(Long id);
}
