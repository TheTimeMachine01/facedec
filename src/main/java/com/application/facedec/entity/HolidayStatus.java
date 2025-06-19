package com.application.facedec.entity;

/**
 * Enumeration representing the holiday status for an attendance record.
 * This helps in categorizing whether a specific attendance date falls on
 * a working day, a recognized holiday, or a type of leave.
 * <ul>
 *  <li>NA (Not Applicable): Just a Regular Day.</li>
 *  <li>CH (Company Holiday): Holiday by company.</li>
 *  <li>L (Leave): An approved Leave.</li>
 *  <li>A (Absent): Absent without any approved leave.</li>
 * </ul>

 */
public enum HolidayStatus {
    NA, CH, L, A
}
