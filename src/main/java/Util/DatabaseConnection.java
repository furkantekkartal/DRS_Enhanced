package Util;

import java.sql.*;

/**
 * This class manages the database connection and operations for the disaster
 * response system. It provides methods to retrieve database configuration,
 * establish a connection, and perform various database operations.
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
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A Connection object representing the database connection.
     * @throws SQLException If a database access error occurs or the URL is
     * null.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
    }

    /**
     * Validates user login credentials.
     *
     * @param username The username to validate.
     * @param password The password to validate.
     * @param role The role to validate.
     * @return A string indicating the login result.
     */
    public static String validateLogin(String username, String password, String selectedRole) {
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
                if (!storedRole.equalsIgnoreCase("admin") && !storedRole.equalsIgnoreCase(selectedRole)) {
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
     * Adds a new disaster report to the database.
     *
     * @param disasterType The type of disaster.
     * @param location The location of the disaster.
     * @param reporterName The name of the reporter.
     * @param contactInfo The contact information of the reporter.
     * @param details Additional details about the disaster.
     * @return A string indicating the result of the operation.
     */
    public static String addReport(String disasterType, String location, String reporterName, String contactInfo, String details) {
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

    /**
     * Retrieves all disaster reports from the database.
     *
     * @return A string containing all report data, or an error message.
     */
    public static String getReports() {
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

    /**
     * Updates a specific field of a disaster report.
     *
     * @param reportId The ID of the report to update.
     * @param field The field to update.
     * @param value The new value for the field.
     * @return A string indicating the result of the operation.
     */
    public static String updateReport(String reportId, String field, String value) {
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

    public static String getUserRole(String username) {
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
     * Validates admin login credentials.
     *
     * @param username The username to validate
     * @param password The password to validate
     * @return LOGIN_SUCCESS if credentials are valid, otherwise an error
     * message
     */
    public static String validateAdminLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'admin'";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "LOGIN_SUCCESS";
            } else {
                return "ACCESS_DENIED";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR: " + e.getMessage();
        }
    }
}
