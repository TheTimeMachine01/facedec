package com.application.facedec.entity;

import com.application.facedec.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        List<Role> existingRoles = roleRepository.findAll();
        List<String> existingRoleNames = existingRoles.stream().map(Role::getName).toList();

        Arrays.stream(Role.RoleType.values()) // Use Role.RoleType here
                .filter(roleType -> !existingRoleNames.contains(roleType.getName()))
                .forEach(roleType -> {
                    Role role = new Role();
                    role.setName(roleType.getName());
                    roleRepository.save(role);
                });
    }
}