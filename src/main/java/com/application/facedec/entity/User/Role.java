package com.application.facedec.entity.User;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Entity
@Data
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Getter
    public enum RoleType {
        USER("USER"),
        ADMIN("ADMIN"),
        DEV("DEV");

        private final String name;

        RoleType(String name) {
            this.name = name;
        }
    }
}
