package com.welcome;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class BookingHistoryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        List<Map<String, String>> bookingList = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DbConnection.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT b.booking_id, b.train_number, b.departure, b.destination, b.travel_date, b.fare, " +
                    "p.passenger_name, p.age, p.gender " +
                    "FROM bookings b " +
                    "JOIN passengers p ON b.booking_id = p.booking_id " +
                    "WHERE b.username = ?"
            );
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> booking = new HashMap<>();
                booking.put("booking_id", rs.getString("booking_id"));
                booking.put("train_number", rs.getString("train_number"));
                booking.put("departure", rs.getString("departure"));
                booking.put("destination", rs.getString("destination"));
                booking.put("travel_date", rs.getString("travel_date"));
                booking.put("fare", rs.getString("fare"));
                booking.put("passenger_name", rs.getString("passenger_name"));
                booking.put("age", rs.getString("age"));
                booking.put("gender", rs.getString("gender"));
                bookingList.add(booking);

                // Debugging - Ensure these are within the loop
                System.out.println("Booking ID: " + rs.getString("booking_id"));
                System.out.println("Passenger Name: " + rs.getString("passenger_name"));
                System.out.println("Age: " + rs.getString("age"));
                System.out.println("Gender: " + rs.getString("gender"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Closing resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Set the list as a request attribute
        request.setAttribute("bookingList", bookingList);
        // Forward the request to the JSP page
        request.getRequestDispatcher("bookinghistory.jsp").forward(request, response);
    }
}
