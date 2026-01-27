package com.application.facedec.service.Rule;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.entity.Rule.CompanyRule;
import com.application.facedec.repository.Rule.CompanyRuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleService {

    private static final Long CONFIG_ID = 1L;

    @Autowired
    private CompanyRuleRepository companyRuleRepository;

    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Retrieves the primary company rule configuration (ID 1L).
     * @return The CompanyRule entity.
     * @throws IllegalStateException if the configuration record does not exist.
     */
    public CompanyRule getCompanyRules() {
        return companyRuleRepository.findById(CONFIG_ID)
                .orElseThrow(() -> new IllegalStateException("Core company configuration record not found. Database initialization required."));
    }

    /**
     * Updates the initial annual leave count in the configuration.
     * Requires Admin role for access.
     * @param newCount The new number of initial leaves.
     * @return The updated CompanyRule entity.
     * @throws SecurityException if the authenticated user is not an Admin.
     */
    @Transactional
    public CompanyRule updateInitialLeaveCount(int newCount) {

        // --- RBAC Check: Enforce Admin Role ---
        if (!securityUtils.hasAdminRole()) {
            System.out.println("Unauthorized user attempted to update company rules.");
            throw new SecurityException("Access denied. Only system administrators can update company rules.");
        }

        // Fetch and update the configuration record
        CompanyRule rules = getCompanyRules();
        rules.setInitialAnnualLeaveCount(newCount);

//        logger.info("Admin {} updated initial annual leave count to {}",
//                securityUtils.getAuthenticatedUser().getId(), newCount);

        return companyRuleRepository.save(rules);
    }
}
