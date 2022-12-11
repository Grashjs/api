package com.grash.repository;

import com.grash.model.abstracts.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Collection<Worker> findByCompany_Id(Long id);
}
