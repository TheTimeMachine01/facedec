package com.application.facedec.repository.Rule;

import com.application.facedec.entity.Rule.CompanyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRuleRepository extends JpaRepository<CompanyRule, Long> {
    // Basic CRUD operations are inherited for managing the rule configuration.
}

