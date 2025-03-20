package com.welcome;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CancelledTicketsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        List<Ticket> cancelledTickets = new ArrayList<>();

        try (Connection con = DbConnection.getConnection()) {
            String sql = "SELECT booking_id, username, train_number, departure, destination, travel_date,travel_class FROM cancelled_bookings WHERE username = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Ticket ticket = new Ticket();
                        ticket.setBookingId(rs.getInt("booking_id"));
                        ticket.setTrainNumber(rs.getString("train_number"));
                        ticket.setDeparture(rs.getString("departure"));
                        ticket.setDestination(rs.getString("destination"));
                        ticket.setTravelDate(rs.getString("travel_date"));
                        ticket.setTravelClass(rs.getString("travel_class"));
                        cancelledTickets.add(ticket);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        request.setAttribute("cancelledTickets", cancelledTickets);
        request.getRequestDispatcher("cancelledTickets.jsp").forward(request, response);
    }
}
