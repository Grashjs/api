package com.grash.service;

import com.grash.dto.RolePatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.RoleMapper;
import com.grash.model.OwnUser;
import com.grash.model.Role;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.RoleType;
import com.grash.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final CompanySettingsService companySettingsService;

    public Role create(Role Role) {
        return roleRepository.save(Role);
    }

    public Role update(Long id, RolePatchDTO role) {
        if (roleRepository.existsById(id)) {
            Role savedRole = roleRepository.findById(id).get();
            return roleRepository.save(roleMapper.updateRole(savedRole, role));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Role> getAll() {
        return roleRepository.findAll();
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Collection<Role> findByCompany(Long id) {
        return roleRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, Role role) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(role.getCompanySettings().getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Role roleReq) {
        if (roleReq.getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return false;
        }
        if (user.getRole().getViewPermissions().contains(PermissionEntity.SETTINGS)) {
            return canPatch(user, roleMapper.toPatchDto(roleReq));
        } else return false;
    }

    public boolean canPatch(OwnUser user, RolePatchDTO roleReq) {
        Long companyId = user.getCompany().getId();
        return true;
    }
}
