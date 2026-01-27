package com.application.facedec.entity.Attendance;

import com.application.facedec.entity.User.Employee;
import com.application.facedec.entity.Face.FaceMatchStatus;
import com.application.facedec.entity.User.HolidayStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "attendance_date"})
}) // Explicitly naming the table
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Employee user;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day; // Enum for Monday, Tuesday, etc.

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    @Column(name = "in_time")
    private LocalTime inTime;

    @Column(name = "out_time")
    private LocalTime outTime;

    @Enumerated(EnumType.STRING)
    private FaceMatchStatus faceData;

    private Long fcss; // Face Comparison Similarity Score

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_status", columnDefinition = "VARCHAR(255) DEFAULT 'NA'")
    private HolidayStatus holidayStatus;

    @Column(name = "in_location", length = 50)
    private String inLocation;

    @Column(name = "out_location", length = 50)
    private String outLocation;

    public Attendance(Employee employee, LocalDate date, LocalTime inTime, DayOfWeek dayOfWeek, FaceMatchStatus faceMatchStatus, Long fcss, Double latitude, Double longitude) {
        this.user = employee; // Correctly assign employee
        this.date = date;     // Correctly assign date
        this.inTime = inTime; // Correctly assign inTime
        this.day = dayOfWeek;       // Correctly assign day
        this.faceData = faceMatchStatus;
        this.fcss = fcss;
        this.inLocation = String.format("%.6f, %.6f", latitude, longitude);
        this.holidayStatus = HolidayStatus.NA; // Default to NA
    }

    public Attendance(Employee user, LocalDate date, DayOfWeek dayOfWeek, HolidayStatus status) {
        this.user = user;
        this.date = date;
        this.day = dayOfWeek; // Added for completeness, although not strictly needed if only holiday status is set.
        this.holidayStatus = status; // Assign the status parameter
        // inTime, outTime, faceData, latitude, longitude will be null initially
    }
}