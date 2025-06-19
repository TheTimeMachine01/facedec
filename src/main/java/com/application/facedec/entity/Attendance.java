package com.application.facedec.entity;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_status", columnDefinition = "VARCHAR(255) DEFAULT 'NA'")
    private HolidayStatus holidayStatus;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public Attendance(Employee employee, LocalDate today, LocalTime now, DayOfWeek dayOfWeek, FaceMatchStatus faceMatchStatus, Double latitude, Double longitude) {
        this.user = employee; // Correctly assign employee
        this.date = today;     // Correctly assign date
        this.inTime = now; // Correctly assign inTime
        this.day = dayOfWeek;       // Correctly assign day
        this.faceData = faceMatchStatus;
        this.latitude = latitude;
        this.longitude = longitude;
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