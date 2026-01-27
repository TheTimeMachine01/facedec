package com.application.facedec.repository.User;

import com.application.facedec.entity.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Override
    Optional<Role> findById(Long Long);

    Optional<Role> findByName(String role);
}
