package Model;

import ENUM.*;
import Interfaces.*;
import Util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Fire Department in the disaster response system. This class
 * implements the Actioner interface and provides methods for managing
 * fire-related incidents and communication.
 * 
 * @author 12223508
 */
public class Fire implements Actioner {

    private DisasterType currentDisaster;
    private String departmentName;

    /**
     * Constructs a new Fire department instance. Initializes the department
     * name.
     */
    public Fire() {
        this.departmentName = "Fire Department";
    }

    /**
     * Returns the name of the department.
     *
     * @return The name of the Fire Department.
     */
    @Override
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * Provides information about the current status of the Fire Department.
     *
     * @return A string indicating the Fire Department's readiness.
     */
    @Override
    public String informStatus() {
        return "Fire Department is ready to respond to incidents.";
    }

    /**
     * Updates the status of the Fire Department for a given report. This method
     * updates both the report object and the database.
     *
     * @param report The report to be updated.
     * @param status The new response status.
     * @throws SQLException If there's an error updating the database.
     */
    public void updateStatus(Report report, ResponseStatus status) throws SQLException {
        report.setDepartmentStatus(Department.FIRE_DEPARTMENT, status);
        // Update the database
        DatabaseConnection.updateDepartmentStatus(report.getId(), Department.FIRE_DEPARTMENT, status);
    }

    /**
     * Adds a new entry to the communication log for a given report. This method
     * updates both the report object and the database.
     *
     * @param report The report to which the log entry will be added.
     * @param entry The new log entry to be added.
     * @throws SQLException If there's an error updating the database.
     */
    public void addCommunicationLogEntry(Report report, String entry) throws SQLException {
        String currentLog = report.getCommunicationLog();
        String updatedLog = currentLog + "\n" + entry;
        report.setCommunicationLog(updatedLog);
        // Update the database
        DatabaseConnection.updateCommunicationLog(report.getId(), updatedLog);
    }

    /**
     * Loads active reports from the database.
     *
     * @return A list of active reports for the Fire Department.
     * @throws SQLException If there's an error querying the database.
     */
    public List<Report> getActiveReports() throws SQLException {
        return DatabaseConnection.getActiveReportsForDepartment(Department.FIRE_DEPARTMENT);
    }

    /**
     * Appends disaster-specific details to the StringBuilder.
     *
     * @param details The StringBuilder to append to
     * @param report The report containing the disaster information
     */
    public void appendDisasterSpecificDetails(StringBuilder details, Report report) throws SQLException {
        Report fullReport = DatabaseConnection.getReportById(report.getId());
        switch (fullReport.getDisasterType()) {
            case "Wildfire":
                details.append("Fire Intensity: ").append(fullReport.getFireIntensity()).append("\n");
                details.append("Affected Area Size: ").append(fullReport.getAffectedAreaSize()).append("\n");
                details.append("Nearby Infrastructure: ").append(fullReport.getNearbyInfrastructure()).append("\n");
                break;
            case "Hurricane":
                details.append("Wind Speed: ").append(fullReport.getWindSpeed()).append("\n");
                details.append("Flood Risk: ").append(fullReport.getFloodRisk()).append("\n");
                details.append("Evacuation Status: ").append(fullReport.getEvacuationStatus()).append("\n");
                break;
            case "Earthquake":
                details.append("Magnitude: ").append(fullReport.getMagnitude()).append("\n");
                details.append("Depth: ").append(fullReport.getDepth()).append("\n");
                details.append("Aftershocks Expected: ").append(fullReport.getAftershocksExpected()).append("\n");
                break;
            case "Flood":
                details.append("Water Level: ").append(fullReport.getWaterLevel()).append("\n");
                details.append("Flood Evacuation Status: ").append(fullReport.getFloodEvacuationStatus()).append("\n");
                details.append("Infrastructure Damage: ").append(fullReport.getInfrastructureDamage()).append("\n");
                break;
            case "Landslide":
                details.append("Slope Stability: ").append(fullReport.getSlopeStability()).append("\n");
                details.append("Blocked Roads: ").append(fullReport.getBlockedRoads()).append("\n");
                details.append("Casualties/Injuries: ").append(fullReport.getCasualtiesInjuries()).append("\n");
                break;
            default:
                details.append("Disaster Description: ").append(fullReport.getDisasterDescription()).append("\n");
                details.append("Estimated Impact: ").append(fullReport.getEstimatedImpact()).append("\n");
                break;
        }
    }
}