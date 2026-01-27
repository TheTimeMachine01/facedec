package com.application.facedec.controller.Rule;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.dto.Rule.RuleUpdate;
import com.application.facedec.entity.Rule.CompanyRule;
import com.application.facedec.service.Rule.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private SecurityUtils securityUtils;


    private ResponseEntity<String> checkAuth() {
        if (securityUtils.getAuthenticatedUser() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required.");
        }
        return null;
    }

    /**
     * GET endpoint for any authenticated user to view the current company rules.
     * Endpoint: /api/rules/
     */
    @GetMapping("/")
    public ResponseEntity<?> getRules() {
        ResponseEntity<String> authError = checkAuth();
        if (authError != null) return authError;

        try {
            CompanyRule rules = ruleService.getCompanyRules();
            return ResponseEntity.ok(rules);
        } catch (IllegalStateException e) {
            // Catches the error if the configuration record ID 1L is missing
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.out.println(STR."Error fetching company rules:\{e}");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve company rules.");
        }
    }

    /**
     * POST endpoint for Admins to update the initial annual leave count.
     * Endpoint: /api/rules/initial-leave
     * Requires: JSON body containing RuleUpdateDTO
     */
    @PostMapping("/initial-leave")
    public ResponseEntity<?> updateInitialLeave(@RequestBody RuleUpdate dto) {
        ResponseEntity<String> authError = checkAuth();
        if (authError != null) return authError;

        try {
            CompanyRule updatedRules = ruleService.updateInitialLeaveCount(dto.getNewInitialAnnualLeaveCount());
            return ResponseEntity.ok(updatedRules);

        } catch (SecurityException e) {
            // Catches the exception thrown by the service if the user is not an Admin
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN) // 403 Forbidden
                    .body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            System.out.println(STR."Error updating initial leave count:\{e}");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update initial leave count.");
        }
    }
}
