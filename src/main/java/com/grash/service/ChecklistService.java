package com.grash.service;

import com.grash.dto.ChecklistPatchDTO;
import com.grash.dto.ChecklistPostDTO;
import com.grash.exception.CustomException;
import com.grash.model.*;
import com.grash.model.enums.RoleType;
import com.grash.repository.CheckListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {
    private final CheckListRepository checklistRepository;
    private final CompanySettingsService companySettingsService;
    private final TaskBaseService taskBaseService;

    public Checklist create(Checklist Checklist) {
        return checklistRepository.save(Checklist);
    }

    public Checklist createPost(ChecklistPostDTO checklistReq, Company company) {
        List<TaskBase> taskBases = checklistReq.getTaskBases().stream()
                .map(taskBaseDto -> taskBaseService.createFromTaskBaseDTO(taskBaseDto, company)).collect(Collectors.toList());
        Checklist checklist = Checklist.builder()
                .name(checklistReq.getName())
                .companySettings(checklistReq.getCompanySettings())
                .taskBases(taskBases)
                .category(checklistReq.getCategory())
                .description(checklistReq.getDescription())
                .build();
        return checklistRepository.save(checklist);
    }

    public Checklist update(Long id, ChecklistPatchDTO checklistReq, Company company) {
        if (checklistRepository.existsById(id)) {
            Checklist savedChecklist = checklistRepository.getById(id);
            savedChecklist.getTaskBases().forEach(taskBase -> taskBaseService.delete(taskBase.getId()));
            List<TaskBase> taskBases = checklistReq.getTaskBases().stream()
                    .map(taskBaseDto -> taskBaseService.createFromTaskBaseDTO(taskBaseDto, company)).collect(Collectors.toList());
            savedChecklist.setTaskBases(taskBases);
            return checklistRepository.save(savedChecklist);

        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Checklist> getAll() {
        return checklistRepository.findAll();
    }

    public void delete(Long id) {
        checklistRepository.deleteById(id);
    }

    public Optional<Checklist> findById(Long id) {
        return checklistRepository.findById(id);
    }

    public Collection<Checklist> findByCompanySettings(Long id) {
        return checklistRepository.findByCompanySettings_Id(id);
    }

    public boolean hasAccess(OwnUser user, Checklist checklist) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(checklist.getCompanySettings().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, ChecklistPostDTO checklistReq) {
        Optional<CompanySettings> optionalCompanySettings = companySettingsService.findById(checklistReq.getCompanySettings().getId());

        //@NotNull fields
        boolean first = optionalCompanySettings.isPresent() && optionalCompanySettings.get().getId().equals(user.getCompany().getCompanySettings().getId());

        return first;
    }

    public boolean canPatch(OwnUser user, ChecklistPatchDTO checklistReq) {
        return true;
    }
}
