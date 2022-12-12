package com.grash.service;

import com.grash.model.WorkOrderHistory;
import com.grash.repository.WorkOrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkOrderHistoryService {
    private final WorkOrderHistoryRepository workOrderHistoryRepository;

    public WorkOrderHistory create(WorkOrderHistory workOrderHistory) {
        return workOrderHistoryRepository.save(workOrderHistory);
    }

    public WorkOrderHistory update(WorkOrderHistory workOrderHistory) {
        return workOrderHistoryRepository.save(workOrderHistory);
    }

    public Collection<WorkOrderHistory> getAll() {
        return workOrderHistoryRepository.findAll();
    }

    public void delete(Long id) {
        workOrderHistoryRepository.deleteById(id);
    }

    public Optional<WorkOrderHistory> findById(Long id) {
        return workOrderHistoryRepository.findById(id);
    }

    public Collection<WorkOrderHistory> findByWorkOrder(Long id) {
        return workOrderHistoryRepository.findByWorkOrder_Id(id);
    }
}
