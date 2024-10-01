package Model;

import ENUM.*;
import Util.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * The Coordinator class manages disaster reports, department assignments, and
 * interactions with various services.
 *
 * @author 12223508
 */
public class Coordinator {

    private List<Report> reports;
    private List<Report> disasterStatusReports;
    private Map<String, Object> departments;
    private Geoscience gisService;

    /**
     * Constructs a new Coordinator object and initializes its fields.
     */
    public Coordinator() {
        reports = new ArrayList<>();
        disasterStatusReports = new ArrayList<>();
        departments = new HashMap<>();
        gisService = new Geoscience(null); // Initialize with appropriate DisasterType if needed
        initializeDepartments();
    }

    /**
     * Initializes the departments map with available services.
     */
    private void initializeDepartments() {
        departments.put("GIS", gisService);
        // Add other departments as needed
    }

    /**
     * Loads all reports from the database into the reports list.
     */
    public void loadReportsFromDatabase() {
        reports.clear();
        try {
            List<Report> loadedReports = DatabaseConnection.getAllReports();
            reports.addAll(loadedReports);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading reports from database: " + e.getMessage());
        }
    }

    /**
     * Loads disaster status reports with 'Pending' or 'In Progress' status from
     * the database.
     */
    public void loadDisasterStatusReports() {
        disasterStatusReports.clear();
        try {
            List<Report> loadedStatusReports = DatabaseConnection.getDisasterStatusReports();
            disasterStatusReports.addAll(loadedStatusReports);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading disaster status reports: " + e.getMessage());
        }
    }

    /**
     * Updates department assignments for a given report in the database and in
     * memory.
     *
     * @param report The report to update
     * @param assignments A map of department assignments and their response
     * statuses
     */
    public void updateDepartmentAssignments(Report report, Map<Department, ResponseStatus> assignments) {
        try {
            DatabaseConnection.updateDepartmentAssignments(report.getId(), assignments);
            for (Map.Entry<Department, ResponseStatus> entry : assignments.entrySet()) {
                if (entry.getValue() != null) {
                    report.setDepartmentStatus(entry.getKey(), entry.getValue());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating department assignments: " + e.getMessage());
        }
    }

    /**
     * Updates a report's information in the database.
     *
     * @param report The report to update
     */
    public void updateReport(Report report) {
        try {
            DatabaseConnection.updateReport(report);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating report: " + e.getMessage());
        }
    }

    /**
     * Adds a new communication log entry to a report and updates the database.
     *
     * @param report The report to update
     * @param log The new log entry to add
     */
    public void addCommunicationLog(Report report, String logEntry) {
        String currentLog = report.getCommunicationLog();
        String updatedLog = (currentLog == null || currentLog.isEmpty()) ? logEntry : currentLog + "\n" + logEntry;
        report.setCommunicationLog(updatedLog);

        try {
            DatabaseConnection.updateCommunicationLog(report.getId(), updatedLog);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating communication log: " + e.getMessage());
        }
    }

    /**
     * Adds a new resource needed to a report and updates the database.
     *
     * @param report The report to update
     * @param resource The new resource to add
     */
    public void addResourceNeeded(Report report, String resource) {
        String currentResources = report.getResourcesNeeded();
        String updatedResources = (currentResources == null || currentResources.isEmpty()) ? resource : currentResources + "\n" + resource;
        report.setResourcesNeeded(updatedResources);

        try {
            DatabaseConnection.updateResourcesNeeded(report.getId(), updatedResources);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating resources needed: " + e.getMessage());
        }
    }

    /**
     * Updates the coordinates of a report and saves the changes to the
     * database.
     *
     * @param report The report to update
     * @param latitude The new latitude
     * @param longitude The new longitude
     */
    public void updateCoordinates(Report report, double latitude, double longitude) {
        report.setLatitude(latitude);
        report.setLongitude(longitude);

        try {
            DatabaseConnection.updateCoordinates(report.getId(), latitude, longitude);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating coordinates: " + e.getMessage());
        }
    }

    /**
     * Gets the coordinates for a given location using the GIS service.
     *
     * @param location The location to get coordinates for
     * @return An array containing the latitude and longitude
     */
    public double[] getCoordinates(String location) {
        return gisService.getCoordinates(location);
    }

    /**
     * Gets the weather information for given coordinates.
     *
     * @param latitude The latitude of the location
     * @param longitude The longitude of the location
     * @return A Meteorology object containing weather information
     */
    public Meteorology getWeather(double latitude, double longitude) {
        return Meteorology.getWeather(latitude, longitude);
    }

    /**
     * Gets the list of all reports.
     *
     * @return The list of all reports
     */
    public List<Report> getReports() {
        return reports;
    }

    /**
     * Gets the list of disaster status reports.
     *
     * @return The list of disaster status reports
     */
    public List<Report> getDisasterStatusReports() {
        return disasterStatusReports;
    }

    /**
     * Gets the map of departments.
     *
     * @return The map of departments
     */
    public Map<String, Object> getDepartments() {
        return departments;
    }

    /**
     * Gets the status update for a specific department.
     *
     * @param departmentKey The key of the department
     * @return The status update of the department
     */
    public String getDepartmentUpdate(String departmentKey) {
        Object department = departments.get(departmentKey);
        if (department instanceof Geoscience) {
            return ((Geoscience) department).getStatusUpdate();
        }
// Add other department types here as needed
        return "Department information not available";
    }

    public String calculatePriority(Report report) {

        StringBuilder calculationProcess = new StringBuilder();
        int score = 0;

        String disasterType = report.getDisasterType();
        calculationProcess.append("Disaster Type: ").append(disasterType).append("\n\n");

        // Disaster type impact
        switch (report.getDisasterType()) {
            case "Wildfire":
                score += 3;
                calculationProcess.append("   - Disaster: High impact           +3 points\n");
                break;
            case "Hurricane":
                score += 3;
                calculationProcess.append("   - Disaster: High impact           +3 points\n");
                break;
            case "Earthquake":
                score += 3;
                calculationProcess.append("   - Disaster: High impact           +3 points\n");
                break;
            case "Flood":
                score += 2;
                calculationProcess.append("   - Disaster:Medium impact          +2 points\n");
                break;
            case "Landslide":
                score += 2;
                calculationProcess.append("   - Disaster:  Medium  impact       +2 points\n");
                break;
            default:
                calculationProcess.append("   - Disaster: Unknown impact        +0 points\n");
                score += 0;
        }

// Weather impact
        Meteorology.WeatherImpact weatherImpact = analyzeWeatherImpact(report);
        if (weatherImpact != null) {
            switch (weatherImpact.getRiskLevel()) {
                case "High Impect":
                    score += 5;
                    calculationProcess.append("   - Weather: High impact              +5 points\n");
                    break;
                case "Medium Impect":
                    score += 3;
                    calculationProcess.append("   - Weather: Medium impact            +3 points\n");
                    break;
                case "Low Impect":
                    score += 1;
                    calculationProcess.append("   - Weather: Low impact               +1 points\n");
                    break;
                default:
                    calculationProcess.append("   - Weather: Minimal impact           +0 points\n");
                    score += 0;
            }
        }

// Affected area or population
        if (report.getAffectedAreaSize() != null && !report.getAffectedAreaSize().isEmpty()) {
            try {
                double area = Double.parseDouble(report.getAffectedAreaSize());
                if (area > 1000) {
                    score += 4;
                    calculationProcess.append("   - Affected area: >1000            +4 points\n");
                } else if (area > 100) {
                    score += 3;
                    calculationProcess.append("   - Affected area: [100-999]        +3 points\n");
                } else if (area > 10) {
                    score += 2;
                    calculationProcess.append("   - Affected area: [10-99]          +2 points\n");
                } else {
                    calculationProcess.append("   - Affected area: <10              +1 point\n");
                    score += 1;
                }
            } catch (NumberFormatException e) {
// If parsing fails, assume moderate impact
                score += 0;
                calculationProcess.append("   - Affected area: 0                +0 point\n");
            }
        }
// New: Time sensitivity
        LocalDateTime reportTime = LocalDateTime.parse(report.getDateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long hoursSinceReport = ChronoUnit.HOURS.between(reportTime, LocalDateTime.now());
        if (hoursSinceReport < 6) {
            score += 3;
            calculationProcess.append("   - Time: Very recent(<6hr)         +3 points\n");
        } else if (hoursSinceReport < 24) {
            score += 2;
            calculationProcess.append("   - Time: Recent [6-24hr]           +2 points\n");
        } else if (hoursSinceReport < 72) {
            score += 1;
            calculationProcess.append("   - Time: Older [24-72hr]           +1 points\n");
        }

// New: Critical infrastructure
        if (report.getNearbyInfrastructure() != null && affectsCriticalInfrastructure(report)) {
            score += 5;
            calculationProcess.append("   - Critical infrastructure:        +5 points\n");
        }

// New: Cascading effects
        if (hasPotentialCascadingEffects(report)) {
            score += 3;
            calculationProcess.append("   - Cascading effects:              +3 points\n");
        }

        calculationProcess.append("\nFinal score: ").append(score).append("\n\n");

// Calculate final priority
        String priority;
        if (score >= 8) {
            priority = "Critical";
        } else if (score >= 5) {
            priority = "High";
        } else if (score >= 3) {
            priority = "Medium";
        } else {
            priority = "Low";
        }

        calculationProcess.append("\nScore Chart: "
                + "\n    score >= 8  : Critical "
                + "\n    score >= 5  : High "
                + "\n    score >= 3  : Medium "
                + "\n    score < 2   : Low").append(priority);
//System.out.println(calculationProcess.toString());

        return priority + "|" + calculationProcess.toString();
    }

    private boolean affectsCriticalInfrastructure(Report report) {
        String nearbyInfrastructure = report.getNearbyInfrastructure();
        if (nearbyInfrastructure == null) {
            return false; // or handle this case as appropriate for your application
        }
        return nearbyInfrastructure.toLowerCase().contains("hospital")
                || nearbyInfrastructure.toLowerCase().contains("power plant");
    }

    private int getAvailableResources() {
        // This would need to be implemented based on your system's resource tracking
        return 30; // Placeholder value
    }

    private boolean hasPotentialCascadingEffects(Report report) {
        return report.getDisasterType() != null
                && report.getDisasterType().equalsIgnoreCase("Earthquake")
                && report.getNearbyInfrastructure() != null
                && report.getNearbyInfrastructure().toLowerCase().contains("dam");
    }

    public Meteorology.WeatherImpact analyzeWeatherImpact(Report report) {
        Meteorology weather = getWeather(report.getLatitude(), report.getLongitude());
        if (weather != null) {
            Meteorology.WeatherImpact impact = weather.analyzeWeatherImpact(report.getDisasterType());
            //System.out.println("Weather Impact Analysis:\n" + impact.toString());
            return impact;
        }
        System.out.println("Weather Impact Analysis: Unable to retrieve weather data.");
        return null;
    }

}