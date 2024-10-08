package Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ENUM.*;
import Model.Report;
import Model.User;
import Server.DRSServer;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class manages the database connection and operations for the disaster
 * response system. It provides methods to retrieve database configuration,
 * establish a connection, and perform various database operations.
 *
 * @author 12223508
 */
public class DatabaseConnection {

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

    public static String getServerHost() {
        return SERVER_HOST;
    }

    public static int getServerPort() {
        return SERVER_PORT;
    }

    public static int getTimeout() {
        return TIMEOUT;
    }

    // Database configuration constants
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "disaster_response";
    private static final String USER = "root";
    private static final String PASS = "pass";

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 5000; // Port your server listens on
    public static final int TIMEOUT = 2000; // Timeout in milliseconds

    private static DRSServer server;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    public DatabaseConnection() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public static void setServer(DRSServer drsServer) {
        server = drsServer;
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A Connection object representing the database connection.
     * @throws SQLException If a database access error occurs or the URL is
     * null.
     */
    public static Connection getConnection() throws SQLException {
        if (isServerRunning()) {
            return DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
        } else {
            throw new SQLException("Cannot connect to the database because the server is not running.");
        }
    }

    public static boolean isServerRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), TIMEOUT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates user login credentials.
     *
     * @param username The username to validate.
     * @param password The password to validate.
     * @param role The role to validate.
     * @return A string indicating the login result.
     */
    public static String validateLogin(String username, String password, String role) {
        if (server != null && !server.isRunning()) {
            return "SERVER_NOT_RUNNING";
        }

        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                if (storedRole.equalsIgnoreCase("admin")
                        || storedRole.equalsIgnoreCase(role)) {
                    return "LOGIN_SUCCESS";
                }else if(storedRole.startsWith("Utility_") 
                        && role.equals("UtilityCompanies")){
                        return "LOGIN_SUCCESS"; 
                 } else {
                    return "ROLE_MISMATCH";
                }
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
     *
     * @param username The username of the user.
     * @return The role of the user, or an empty string if not found.
     */
    public static String getUserRole(String username) {
        if (server != null && !server.isRunning()) {
            return "SERVER_NOT_RUNNING";
        }

        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
     *
     * @return A list of Report objects.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Report> getAllReports() throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }
        }
        return reports;
    }

    /**
     * Retrieves disaster status reports from the database.
     *
     * @return A list of Report objects with pending or in-progress status.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Report> getDisasterStatusReports() throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE response_status IN ('Pending', 'In Progress')";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }
        }
        return reports;
    }

    /**
     * Updates department assignments for a report in the database.
     *
     * @param reportId The ID of the report to update.
     * @param assignments A map of department assignments and their response
     * statuses.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateDepartmentAssignments(int reportId, Map<Department, ResponseStatus> assignments) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "UPDATE reports SET fire_department_status = ?, health_department_status = ?, "
                + "law_enforcement_status = ?, meteorology_status = ?, geoscience_status = ?, "
                + "utility_companies_status = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
     *
     * @param report The Report object to update.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateReport(Report report) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "UPDATE reports SET response_status = ?, resources_needed = ?, communication_log = ?, priority_level = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, report.getResponseStatus());
            pstmt.setString(2, report.getResourcesNeeded());
            pstmt.setString(3, report.getCommunicationLog());
            pstmt.setString(4, report.getPriorityLevel());
            pstmt.setInt(5, report.getId());
            pstmt.executeUpdate();
        }
    }

    public static void updateReportFromEditWindow(Report report) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "UPDATE reports SET "
                + "disaster_type = ?, location = ?, latitude = ?, longitude = ?, date_time = ?, "
                + "reporter_name = ?, contact_info = ?, fire_intensity = ?, affected_area_size = ?, "
                + "nearby_infrastructure = ?, wind_speed = ?, flood_risk = ?, evacuation_status = ?, "
                + "magnitude = ?, depth = ?, aftershocks_expected = ?, water_level = ?, "
                + "flood_evacuation_status = ?, infrastructure_damage = ?, slope_stability = ?, "
                + "blocked_roads = ?, casualties_injuries = ?, disaster_description = ?, "
                + "estimated_impact = ?, response_status = ?, assigned_department = ?, "
                + "resources_needed = ?, communication_log = ?, priority_level = ?, "
                + "fire_department_status = ?, health_department_status = ?, law_enforcement_status = ?, "
                + "meteorology_status = ?, geoscience_status = ?, utility_companies_status = ?, "
                + "utility_electricity_status = ?, utility_water_status = ?, utility_gas_status = ?, "
                + "utility_telecommunications_status = ? "
                + "WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, report.getDisasterType());
            pstmt.setString(2, report.getLocation());
            pstmt.setDouble(3, report.getLatitude());
            pstmt.setDouble(4, report.getLongitude());
            pstmt.setString(5, report.getDateTime());
            pstmt.setString(6, report.getReporterName());
            pstmt.setString(7, report.getContactInfo());
            pstmt.setString(8, report.getFireIntensity());
            pstmt.setString(9, report.getAffectedAreaSize());
            pstmt.setString(10, report.getNearbyInfrastructure());
            pstmt.setString(11, report.getWindSpeed());
            pstmt.setBoolean(12, report.getFloodRisk());
            pstmt.setString(13, report.getEvacuationStatus());
            pstmt.setString(14, report.getMagnitude());
            pstmt.setString(15, report.getDepth());
            pstmt.setBoolean(16, report.getAftershocksExpected());
            pstmt.setString(17, report.getWaterLevel());
            pstmt.setString(18, report.getFloodEvacuationStatus());
            pstmt.setString(19, report.getInfrastructureDamage());
            pstmt.setString(20, report.getSlopeStability());
            pstmt.setString(21, report.getBlockedRoads());
            pstmt.setString(22, report.getCasualtiesInjuries());
            pstmt.setString(23, report.getDisasterDescription());
            pstmt.setString(24, report.getEstimatedImpact());
            pstmt.setString(25, report.getResponseStatus());
            pstmt.setString(26, report.getAssignedDepartment());
            pstmt.setString(27, report.getResourcesNeeded());
            pstmt.setString(28, report.getCommunicationLog());
            pstmt.setString(29, report.getPriorityLevel());
            pstmt.setString(30, report.getDepartmentStatus(Department.FIRE_DEPARTMENT).name());
            pstmt.setString(31, report.getDepartmentStatus(Department.HEALTH_DEPARTMENT).name());
            pstmt.setString(32, report.getDepartmentStatus(Department.LAW_ENFORCEMENT).name());
            pstmt.setString(33, report.getDepartmentStatus(Department.METEOROLOGY).name());
            pstmt.setString(34, report.getDepartmentStatus(Department.GEOSCIENCE).name());
            pstmt.setString(35, report.getDepartmentStatus(Department.UTILITY_COMPANIES).name());
            pstmt.setString(36, report.getDepartmentStatus(Department.UTILITY_ELECTRICITY).name());
            pstmt.setString(37, report.getDepartmentStatus(Department.UTILITY_WATER).name());
            pstmt.setString(38, report.getDepartmentStatus(Department.UTILITY_GAS).name());
            pstmt.setString(39, report.getDepartmentStatus(Department.UTILITY_TELECOMMUNICATIONS).name());
            pstmt.setInt(40, report.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating report failed, no rows affected.");
            }
            System.out.println("Report updated successfully in the database.");
        }
    }

    /**
     * Updates the communication log for a report in the database.
     *
     * @param reportId The ID of the report to update.
     * @param log The updated communication log.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateCommunicationLog(int reportId, String log) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "UPDATE reports SET communication_log = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the resources needed for a report in the database.
     *
     * @param reportId The ID of the report to update.
     * @param resources The updated resources needed.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateResourcesNeeded(int reportId, String resources) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "UPDATE reports SET resources_needed = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resources);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the coordinates for a report in the database.
     *
     * @param reportId The ID of the report to update.
     * @param latitude The new latitude.
     * @param longitude The new longitude.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateCoordinates(int reportId, double latitude, double longitude) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "UPDATE reports SET latitude = ?, longitude = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setInt(3, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the status of a specific department for a report.
     *
     * @param reportId The ID of the report to update.
     * @param department The department whose status is being updated.
     * @param status The new status for the department.
     * @throws SQLException If a database access error occurs.
     */
    public static void updateDepartmentStatus(int reportId, Department department, ResponseStatus status) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String columnName = department.name().toLowerCase() + "_status";
        String sql = "UPDATE reports SET " + columnName + " = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves active reports for a specific department.
     *
     * @param department The department to get reports for.
     * @return A list of active reports for the specified department.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Report> getActiveReportsForDepartment(Department department) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        List<Report> reports = new ArrayList<>();
        String columnName = department.name().toLowerCase() + "_status";
        String sql = "SELECT * FROM reports WHERE response_status IN ('Pending', 'In Progress') "
                + "AND " + columnName + " IS NOT NULL AND " + columnName + " != 'NOT_RESPONSIBLE'";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }
        }
        return reports;
    }

    /**
     * Retrieves a specific report by its ID.
     *
     * @param reportId The ID of the report to retrieve.
     * @return The Report object, or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    public static Report getReportById(int reportId) throws SQLException {
        if (server != null && !server.isRunning()) {
            throw new SQLException("SERVER_NOT_RUNNING");
        }

        String sql = "SELECT * FROM reports WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createReportFromResultSet(rs);
            }
        }
        return null;
    }

    /**
     * Creates a Report object from a ResultSet.
     *
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

    public String addReport(String disasterType, String location, String reporterName, String contactInfo, String details) {
        if (server != null && !server.isRunning()) {
            return "SERVER_NOT_RUNNING";
        }

        String sql = "INSERT INTO reports (disaster_type, location, reporter_name, contact_info, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, disasterType);
            pstmt.setString(2, location);
            pstmt.setString(3, reporterName);
            pstmt.setString(4, contactInfo);
            pstmt.setString(5, details);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return "REPORT_ADDED|" + generatedKeys.getLong(1);
                    }
                }
            }
            return "REPORT_FAILED";
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR";
        }
    }

    public String getReports() {
        if (server != null && !server.isRunning()) {
            return "SERVER_NOT_RUNNING";
        }

        StringBuilder result = new StringBuilder();
        String sql = "SELECT * FROM reports";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.append(rs.getInt("id")).append("|")
                        .append(rs.getString("disaster_type")).append("|")
                        .append(rs.getString("location")).append("|")
                        .append(rs.getString("reporter_name")).append("|")
                        .append(rs.getString("contact_info")).append("|")
                        .append(rs.getString("details")).append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR";
        }
    }

    public String updateReport(String reportId, String field, String value) {
        if (server != null && !server.isRunning()) {
            return "SERVER_NOT_RUNNING";
        }

        String sql = "UPDATE reports SET " + field + " = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setInt(2, Integer.parseInt(reportId));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0 ? "REPORT_UPDATED" : "UPDATE_FAILED";
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR";
        }
    }

    /**
     * Registers a new user in the database.
     *
     * @param user The User object containing user details.
     * @return true if registration is successful, false otherwise.
     */
    public static boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, role, email, full_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Password is not hashed
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getFullName());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Handle duplicate username or email constraint
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    public static boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Checks if an email already exists in the database.
     *
     * @param email The email to check.
     * @return true if the email exists, false otherwise.
     */
    public static boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}
