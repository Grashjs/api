package com.grash.service;

import com.grash.model.File;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    public File create(File File) {
        return fileRepository.save(File);
    }

    public File update(File File) {
        return fileRepository.save(File);
    }

    public Collection<File> getAll() {
        return fileRepository.findAll();
    }

    public void delete(Long id) {
        fileRepository.deleteById(id);
    }

    public Optional<File> findById(Long id) {
        return fileRepository.findById(id);
    }

    public Collection<File> findByCompany(Long id) {
        return fileRepository.findByCompany_Id(id);
    }

    public boolean hasAccess(OwnUser user, File file) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(file.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, File fileReq) {
        return true;
    }
}
