package com.grash.service;

import com.grash.model.OwnUser;
import com.grash.model.abstracts.Worker;
import com.grash.model.enums.RoleType;
import com.grash.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerRepository workerRepository;

    public Collection<Worker> getAll() {
        return workerRepository.findAll();
    }

    public void delete(Long id) {
        workerRepository.deleteById(id);
    }

    public Optional<Worker> findById(Long id) {
        return workerRepository.findById(id);
    }

    public Collection<Worker> findByCompany(Long id) {
        return workerRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, Worker worker) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(worker.getCompany().getId());
    }

}
