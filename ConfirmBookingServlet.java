package com.welcome;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfirmBookingServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ConfirmBookingServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Retrieve session attributes with null checks
        String trainNumber = (String) session.getAttribute("trainNumber");
        String departure = (String) session.getAttribute("departure");
        String destination = (String) session.getAttribute("destination");
        String travelDate = (String) session.getAttribute("travelDate");
        String travelClass = (String) session.getAttribute("travelClass");
        String[] passengerNames = (String[]) session.getAttribute("passengerNames");
        String[] ages = (String[]) session.getAttribute("ages");
        String[] genders = (String[]) session.getAttribute("genders");
        Double fare = (Double) session.getAttribute("fare"); // Prevents ClassCastException
        String username = (String) session.getAttribute("username");

        // Handle null fare case
        if (fare == null) {
            fare = 0.0; // Default to 0 if fare is missing
        }

        Connection conn = null;
        PreparedStatement bookingStmt = null;
        PreparedStatement passengerStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert booking details
            String bookingQuery = "INSERT INTO bookings (train_number, departure, destination, travel_date, travel_class, username, fare) VALUES (?, ?, ?, ?, ?, ?, ?)";
            bookingStmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS);
            bookingStmt.setString(1, trainNumber);
            bookingStmt.setString(2, departure);
            bookingStmt.setString(3, destination);
            bookingStmt.setString(4, travelDate);
            bookingStmt.setString(5, travelClass);
            bookingStmt.setString(6, username);
            bookingStmt.setDouble(7, fare);

            int rowsAffected = bookingStmt.executeUpdate();
            if (rowsAffected > 0) {
                // Get the auto-generated booking ID
                generatedKeys = bookingStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int bookingId = generatedKeys.getInt(1);

                    // Insert passenger details
                    String passengerQuery = "INSERT INTO passengers (booking_id, passenger_name, age, gender) VALUES (?, ?, ?, ?)";
                    passengerStmt = conn.prepareStatement(passengerQuery);

                    for (int i = 0; i < passengerNames.length; i++) {
                        passengerStmt.setInt(1, bookingId);
                        passengerStmt.setString(2, passengerNames[i]);
                        passengerStmt.setInt(3, Integer.parseInt(ages[i]));
                        passengerStmt.setString(4, genders[i]);
                        passengerStmt.addBatch();
                    }

                    passengerStmt.executeBatch();
                    conn.commit(); // Commit transaction

                 // After successfully confirming the booking for a passenger
                    List<String> confirmedPassengers = (List<String>) session.getAttribute("confirmedPassengers");
                    if (confirmedPassengers == null) {
                        confirmedPassengers = new ArrayList<>();
                    }
                    for (String name : passengerNames) {
                        confirmedPassengers.add(name);  // Store names instead of indices
                    }
                    session.setAttribute("confirmedPassengers", confirmedPassengers);


                    // Set attributes and redirect
                    session.setAttribute("bookingMessage", "Ticket Confirmed Successfully!");
                    session.setAttribute("bookingId", bookingId);
                    response.sendRedirect("booking-details.jsp");
                    return; // Ensure no further execution
                }
            } else {
                LOGGER.log(Level.SEVERE, "Booking insertion failed.");
                throw new SQLException("Booking insertion failed.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during booking confirmation", e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback failed", ex);
                }
            }
            response.sendRedirect("error.jsp?message=Database%20Error.%20Please%20Try%20Again.");
        } finally {
            closeResources(generatedKeys, conn, bookingStmt, passengerStmt);  // Ensure all resources are closed
        }
    }

    // Method to close database resources
    private void closeResources(ResultSet rs, Connection conn, PreparedStatement... statements) {
        try {
            if (rs != null) rs.close();  // Close the ResultSet
            for (PreparedStatement stmt : statements) {
                if (stmt != null) stmt.close();  // Close each PreparedStatement
            }
            if (conn != null) conn.close();  // Close the Connection
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing resources", e);  // Log if an error occurs while closing resources
        }
    }
} 