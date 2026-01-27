package com.application.facedec.entity.Attendance;

//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.Table;

import com.application.facedec.entity.User.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity to store the current available leave balance for an employee.
 * Uses the Employee's ID as the primary key.
 */
@Entity
@Data
//@AllArgsConstructor
@NoArgsConstructor
@Table(name = "leave_balance")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Employee user;

    @Column(name = "balance_month", nullable = false)
    private int month; // 1-12

    @Column(name = "balance_year", nullable = false)
    private int year;

    // The remaining available leave days for the employee for this specific month/year
    private int remainingLeaves;

    // --- UPDATED Custom constructor for initialization ---
    /**
     * Constructor for creating a new monthly leave balance record.
     * @param user The employee to whom the balance belongs.
     * @param remainingLeaves The initial number of leaves for this period.
     * @param month The month (1-12) the balance applies to.
     * @param year The year the balance applies to.
     */
    public LeaveBalance(Employee user, int remainingLeaves, int month, int year) {
        this.user = user;
        this.remainingLeaves = remainingLeaves;
        this.month = month;
        this.year = year;
    }
}
