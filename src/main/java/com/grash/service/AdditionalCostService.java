package com.grash.service;

import com.grash.dto.AdditionalCostPatchDTO;
import com.grash.exception.CustomException;
import com.grash.model.AdditionalCost;
import com.grash.repository.AdditionalCostRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdditionalCostService {

    private final AdditionalCostRepository additionalCostRepository;

    private final ModelMapper modelMapper;

    public AdditionalCost create(AdditionalCost additionalCost) {
        return additionalCostRepository.save(additionalCost);
    }

    public AdditionalCost update(Long id, AdditionalCostPatchDTO additionalCost) {
        if (additionalCostRepository.existsById(id)) {
            AdditionalCost savedAdditionalCost = additionalCostRepository.findById(id).get();
            modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
            modelMapper.map(additionalCost, savedAdditionalCost);
            return additionalCostRepository.save(savedAdditionalCost);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<AdditionalCost> getAll() {
        return additionalCostRepository.findAll();
    }

    public void delete(Long id) {
        additionalCostRepository.deleteById(id);
    }

    public Optional<AdditionalCost> findById(Long id) {
        return additionalCostRepository.findById(id);
    }


}
