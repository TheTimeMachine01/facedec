package com.application.facedec.service.Attendance;


import com.application.facedec.dto.Attendance.DailyLog;
import com.application.facedec.dto.Attendance.TodayLogResponse;
import com.application.facedec.entity.Attendance.Attendance;
import com.application.facedec.entity.User.Employee;
import com.application.facedec.entity.Face.FaceMatchStatus;
import com.application.facedec.entity.User.HolidayStatus;
import com.application.facedec.repository.Attendance.AttendanceRepository;
import com.application.facedec.repository.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Marks an employee's "in" time attendance for the current day.
     * This method will create a new attendance record if one doesn't exist for today.
     * It includes location data and face matching status.
     *
     * @param employee The Employee entity for whom attendance is to be marked.
     * @param latitude The latitude coordinate of the user's location.
     * @param longitude The longitude coordinate of the user's location.
     * @param isFaceMatched A boolean indicating if the face was successfully matched.
     * @return The created or updated Attendance record if successful, or null if already marked or an error occurred.
     */
    @Transactional
    public Attendance markInTime(Employee employee, Double latitude, Double longitude, Long fcss, Boolean isFaceMatched) {
        LocalDate today = LocalDate.now();

        // Check if "in" time is already marked for today for this employee
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(employee, today);

        if (existingAttendance.isPresent() && existingAttendance.get().getInTime() != null) {
            System.out.println(STR."In time already marked for employee \{employee.getId()} on \{today}");
            return null; // In time already marked for today
        }

//        Late Policy

        System.out.println(STR."Is Face Matched:\{isFaceMatched}");
        Attendance attendance;
        String inLocationString = String.format("%.6f, %.6f", latitude, longitude);
        if (existingAttendance.isPresent()) {
            // If a record exists (e.g., for a holiday pre-marked without times), update it
            attendance = existingAttendance.get();
            attendance.setDate(today);
            attendance.setInTime(LocalTime.now());
            attendance.setInLocation(inLocationString);
            System.out.println(isFaceMatched);
            attendance.setFaceData(isFaceMatched ? FaceMatchStatus.MATCHED : FaceMatchStatus.NOT_MATCHED);
            attendance.setFcss(fcss);
            // Ensure holiday status is NA if it was set to something else and now they are clocking in
            if (attendance.getHolidayStatus() != null && attendance.getHolidayStatus() != HolidayStatus.NA) {
                // If they are clocking in, it's no longer a 'leave' or 'absent' day for this record
                attendance.setHolidayStatus(HolidayStatus.NA);
            }
        } else {
            // Create a new attendance record
            attendance = new Attendance(
                    employee,
                    today,
                    LocalTime.now(),
                    today.getDayOfWeek(),
                    isFaceMatched ? FaceMatchStatus.MATCHED : FaceMatchStatus.NOT_MATCHED,
                    fcss,
                    latitude,
                    longitude
            );
            // Default holidayStatus is set to NA by the constructor/columnDefinition
        }

        try {
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            System.err.println(STR."Error saving in-time attendance: \{e.getMessage()}"); // Use err for errors
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Marks an employee's "out" time attendance for the current day.
     * This method assumes an "in" record already exists for the day.
     *
     * @param employee The Employee entity for whom attendance is to be marked.
     * @param latitude The latitude coordinate of the user's location at logout.
     * @param longitude The longitude coordinate of the user's location at logout.
     * @return The updated Attendance record if successful, or null if no "in" record found or an error occurred.
     */
    @Transactional
    public Attendance markOutTime(Employee employee, Double latitude, Double longitude) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(employee, today);

        if (existingAttendance.isEmpty()) {
            System.out.println(STR."No 'in' record found for employee \{employee.getId()} on \{today} to mark 'out' time.");
            return null; // Cannot mark out time if no in time was recorded
        }

        Attendance attendance = existingAttendance.get();

        if (attendance.getOutTime() != null) {
            System.out.println(STR."Out time already marked for employee \{employee.getId()} on \{today}");
            return null; // Out time already marked for today
        }

        attendance.setOutTime(LocalTime.now());
        String outLocationString = String.format("%.6f, %.6f", latitude, longitude);
        attendance.setOutLocation(outLocationString); // Set new outLocation field

        try {
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            System.err.println(STR."Error saving out-time attendance: \{e.getMessage()}"); // Use err for errors
            e.printStackTrace();
            return null;
        }
    }

     public boolean hasEmployeeMarkedInTimeForToday(Employee employee, LocalDate date) {
         return attendanceRepository.findByUserAndDate(employee, date)
                                    .filter(a -> a.getInTime() != null)
                                    .isPresent();
     }

    /**
     * Marks a specific date as a holiday, leave, or absent day for an employee.
     * If an attendance record already exists for the date, its holiday status is updated.
     * Otherwise, a new minimal attendance record is created.
     *
     * @param employee The Employee entity for whom the holiday status is being set.
     * @param date The date to be marked.
     * @param status The HolidayStatus (CH, L, A, NA) to apply.
     * @return The created or updated Attendance record if successful, or null if an error occurred.
     */
    @Transactional
    public Attendance markHoliday(Employee employee, LocalDate date, HolidayStatus status) {
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(employee, date);

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
            // If someone was previously clocked in/out but now it's a holiday, clear times
            if (status != HolidayStatus.NA && (attendance.getInTime() != null || attendance.getOutTime() != null)) {
                attendance.setInTime(null);
                attendance.setOutTime(null);
                attendance.setFaceData(null); // Clear face data as well
            }
            attendance.setHolidayStatus(status);
        } else {
            // Create a new record just for the holiday status
            attendance = new Attendance(employee, date, date.getDayOfWeek(), status);
        }

        try {
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an employee has already marked "in" time attendance for the current day.
     *
     * @param employee The Employee entity to check.
     * @return true if "in" time is marked for today, false otherwise.
     */
    public boolean hasMarkedInTimeToday(Employee employee) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> attendance = attendanceRepository.findByUserAndDate(employee, today);
        return attendance.isPresent() && attendance.get().getInTime() != null;
    }

    /**
     * Checks if an employee has already marked "out" time attendance for the current day.
     *
     * @param employee The Employee entity to check.
     * @return true if "out" time is marked for today, false otherwise.
     */
    public boolean hasMarkedOutTimeToday(Employee employee) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> attendance = attendanceRepository.findByUserAndDate(employee, today);
        return attendance.isPresent() && attendance.get().getOutTime() != null;
    }

    public TodayLogResponse getTodayLog(Employee employee) {

        LocalDate today = LocalDate.now();

        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserAndDate(employee, today);
        TodayLogResponse todayLogResponse = new TodayLogResponse();
        todayLogResponse.setDate(today);

        if (attendanceOptional.isPresent()) {
            // Attendance record found: populate with actual data
            Attendance attendance = attendanceOptional.get();

            // Use the actual date from attendance if needed, but 'today' is usually correct
            // todayLogResponse.setDate(attendance.getDate());

            // NOTE: Convert to String safely. Assuming inTime/outTime are some Time objects
            todayLogResponse.setInTime(String.valueOf(attendance.getInTime()));
            todayLogResponse.setOutTime(String.valueOf(attendance.getOutTime()));
            todayLogResponse.setHolidayStatus(String.valueOf(attendance.getHolidayStatus()));
        } else {
            // Attendance record NOT found: use default/placeholder values
            todayLogResponse.setInTime("00:00:00.000");
            todayLogResponse.setOutTime("00:00:00.000");
            todayLogResponse.setHolidayStatus(HolidayStatus.NA.toString());
        }
        return todayLogResponse;
    }

    /**
     * Retrieves the attendance logs for an employee for a specific month and year.
     * If month/year are null, it defaults to the current period.
     *
     * @param employee The authenticated employee.
     * @param month Optional month (1-12).
     * @param year Optional year.
     * @return A list of DailyLogDTOs covering every day of the month.
     */
    public List<DailyLog> getMonthlyLog(Employee employee, Integer month, Integer year) {

        YearMonth targetMonth;
        LocalDate today = LocalDate.now();

        if (month != null && year != null) {
            targetMonth = YearMonth.of(year, month);
        } else {
            targetMonth = YearMonth.from(today);
        }

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<Attendance> monthlyRecords = attendanceRepository.findByUserAndDateBetween(employee, startDate, endDate);

        java.util.Map<LocalDate, Attendance> recordsByDate = monthlyRecords.stream()
                .collect(Collectors.toMap(Attendance::getDate, record -> record));

        List<DailyLog> monthlyLog = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            DailyLog logEntry = new DailyLog();
            logEntry.setDate(currentDate);

            if(recordsByDate.containsKey(currentDate)) {
                Attendance record = recordsByDate.get(currentDate);
                logEntry.setInTime(String.valueOf(record.getInTime()));
                logEntry.setOutTime(String.valueOf(record.getOutTime()));
                logEntry.setHolidayStatus(String.valueOf(record.getHolidayStatus()));
            } else {
                // No record found, check if it's a weekend (assuming weekend is non-working/holiday)
                if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    logEntry.setHolidayStatus("WEEKEND");
                }
                // If it's a weekday and no record, it defaults to 00:00 and NA/not set (which the DTO handles)
            }

            monthlyLog.add(logEntry);
            currentDate = currentDate.plusDays(1);
        }

        return monthlyLog;
    }


}

