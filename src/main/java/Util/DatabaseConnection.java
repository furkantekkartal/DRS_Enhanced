package Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ENUM.*;
import Model.Report;

/**
 * This class manages the database connection and operations for the disaster response system.
 * It provides methods to retrieve database configuration, establish a connection,
 * and perform various database operations.
 * 
 * @author 12223508
 */
public class DatabaseConnection {
    
    // Database configuration constants
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "disaster_response";
    private static final String USER = "root";
    private static final String PASS = "pass";

    public static String getDB_URL() {
        return DB_URL;
    }

    public static String getDB_NAME() {
        return DB_NAME;
    }

    public static String getUSER() {
        return USER;
    }

    public static String getPASS() {
        return PASS;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseConnection() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    /**
     * Establishes and returns a connection to the database.
     * @return A Connection object representing the database connection.
     * @throws SQLException If a database access error occurs or the URL is null.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
    }

    /**
     * Validates user login credentials.
     * @param username The username to validate.
     * @param password The password to validate.
     * @param role The role to validate.
     * @return A string indicating the login result.
     */
    public static String validateLogin(String username, String password, String role) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String storedRole = rs.getString("role");
                
                if (!password.equals(storedPassword)) {
                    return "INCORRECT_PASSWORD";
                }
                
                // Check if the stored role matches the selected role
                // Allow admin to access any role
                if (!storedRole.equalsIgnoreCase("admin") && !storedRole.equalsIgnoreCase(role)) {
                    return "ROLE_MISMATCH";
                }
                
                return "LOGIN_SUCCESS";
            } else {
                return "USER_NOT_FOUND";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR: " + e.getMessage();
        }
    }

    /**
     * Retrieves the role of a user from the database.
     * @param username The username of the user.
     * @return The role of the user, or an empty string if not found.
     */
    public static String getUserRole(String username) {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Retrieves all reports from the database.
     * @return A list of Report objects.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Report> getAllReports() throws SQLException {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }
        }
        return reports;
    }

    /**
     * Retrieves disaster status reports from the database.
     * @return A list of Report objects with pending or in-progress status.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Report> getDisasterStatusReports() throws SQLException {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE response_status IN ('Pending', 'In Progress')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }
        }
        return reports;
    }

    /**
     * Updates department assignments for a report in the database.
     * @param reportId The ID of the report to update.
     * @param assignments A map of department assignments and their response statuses.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateDepartmentAssignments(int reportId, Map<Department, ResponseStatus> assignments) throws SQLException {
        String sql = "UPDATE reports SET fire_department_status = ?, health_department_status = ?, "
                   + "law_enforcement_status = ?, meteorology_status = ?, geoscience_status = ?, "
                   + "utility_companies_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, getStatusString(assignments, Department.FIRE_DEPARTMENT));
            pstmt.setString(2, getStatusString(assignments, Department.HEALTH_DEPARTMENT));
            pstmt.setString(3, getStatusString(assignments, Department.LAW_ENFORCEMENT));
            pstmt.setString(4, getStatusString(assignments, Department.METEOROLOGY));
            pstmt.setString(5, getStatusString(assignments, Department.GEOSCIENCE));
            pstmt.setString(6, getStatusString(assignments, Department.UTILITY_COMPANIES));
            pstmt.setInt(7, reportId);
            pstmt.executeUpdate();
        }
    }

    private static String getStatusString(Map<Department, ResponseStatus> assignments, Department department) {
        ResponseStatus status = assignments.get(department);
        return status != null ? status.name() : ResponseStatus.NOT_RESPONSIBLE.name();
    }

    /**
     * Updates a report in the database.
     * @param report The Report object to update.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateReport(Report report) throws SQLException {
        String sql = "UPDATE reports SET response_status = ?, resources_needed = ?, communication_log = ?, priority_level = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, report.getResponseStatus());
            pstmt.setString(2, report.getResourcesNeeded());
            pstmt.setString(3, report.getCommunicationLog());
            pstmt.setString(4, report.getPriorityLevel());
            pstmt.setInt(5, report.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the communication log for a report in the database.
     * @param reportId The ID of the report to update.
     * @param log The updated communication log.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateCommunicationLog(int reportId, String log) throws SQLException {
        String sql = "UPDATE reports SET communication_log = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the resources needed for a report in the database.
     * @param reportId The ID of the report to update.
     * @param resources The updated resources needed.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateResourcesNeeded(int reportId, String resources) throws SQLException {
        String sql = "UPDATE reports SET resources_needed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resources);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the coordinates for a report in the database.
     * @param reportId The ID of the report to update.
     * @param latitude The new latitude.
     * @param longitude The new longitude.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateCoordinates(int reportId, double latitude, double longitude) throws SQLException {
        String sql = "UPDATE reports SET latitude = ?, longitude = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setInt(3, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Creates a Report object from a ResultSet.
     * @param rs The ResultSet containing report data.
     * @return A new Report object.
     * @throws SQLException If a database access error occurs.
     */
    private static Report createReportFromResultSet(ResultSet rs) throws SQLException {
        Report report = new Report(
            rs.getInt("id"),
            rs.getString("disaster_type"),
            rs.getString("location"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getString("date_time"),
            rs.getString("reporter_name"),
            rs.getString("contact_info"),
            rs.getString("response_status")
        );

        report.setCreatedAt(rs.getString("created_at"));
        report.setFireIntensity(rs.getString("fire_intensity"));
        report.setAffectedAreaSize(rs.getString("affected_area_size"));
        report.setNearbyInfrastructure(rs.getString("nearby_infrastructure"));
        report.setWindSpeed(rs.getString("wind_speed"));
        report.setFloodRisk(rs.getBoolean("flood_risk"));
        report.setEvacuationStatus(rs.getString("evacuation_status"));
        report.setMagnitude(rs.getString("magnitude"));
        report.setDepth(rs.getString("depth"));
        report.setAftershocksExpected(rs.getBoolean("aftershocks_expected"));
        report.setWaterLevel(rs.getString("water_level"));
        report.setFloodEvacuationStatus(rs.getString("flood_evacuation_status"));
        report.setInfrastructureDamage(rs.getString("infrastructure_damage"));
        report.setSlopeStability(rs.getString("slope_stability"));
        report.setBlockedRoads(rs.getString("blocked_roads"));
        report.setCasualtiesInjuries(rs.getString("casualties_injuries"));
        report.setDisasterDescription(rs.getString("disaster_description"));
        report.setEstimatedImpact(rs.getString("estimated_impact"));
        report.setResourcesNeeded(rs.getString("resources_needed"));
        report.setCommunicationLog(rs.getString("communication_log"));
        report.setPriorityLevel(rs.getString("priority_level"));

        for (Department dept : Department.values()) {
            String statusString = rs.getString(dept.name().toLowerCase() + "_status");
            ResponseStatus status = (statusString != null && !statusString.isEmpty())
                    ? ResponseStatus.valueOf(statusString)
                    : ResponseStatus.NOT_RESPONSIBLE;
            report.setDepartmentStatus(dept, status);
        }

        return report;
    }
}