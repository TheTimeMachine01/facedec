package com.application.facedec.entity.Rule;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity to store centralized, editable company rules and configurations.
 * We assume the record with ID 1L is the active, default rule set.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "company_rule")
public class CompanyRule {

    // Using a fixed ID 1L for the primary configuration record
    @Id
    private Long id = 1L;

    // The initial number of leaves granted to a new employee.
    private int initialAnnualLeaveCount;

    // Add more rule fields here as needed (e.g., maxSickDays, workingHoursPerDay, etc.)



}

