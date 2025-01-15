package com.application.facedec.service;

import com.application.facedec.dto.RegistrationRequest;
import com.application.facedec.entity.Role;
import com.application.facedec.entity.RoleName;
import com.application.facedec.repository.RoleRepository;
import com.application.facedec.repository.UserRepository;
import com.application.facedec.entity.User;
import com.application.facedec.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    public User registerUser(RegistrationRequest register) {
        if (userRepository.existsByEmail(register.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        User user = new User();
        user.setName(register.getName());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));

        Role userRole = roleRepository.findByRoleName(RoleName.USER)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(RoleName.USER);
                            return roleRepository.save(newRole);
                        });

        user.getRoles().add(userRole);

        return userRepository.save(user);
    }

    public User assignRoleToUser(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));

        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Convert User to Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getRoleName().name())
                        .toList()
                        .toArray(String[]::new))
                .build();
    }
}

