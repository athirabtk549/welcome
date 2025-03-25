package com.welcome;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BookingServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("updateFare".equals(action)) {
            String fareStr = request.getParameter("fare");
            if (fareStr != null && !fareStr.isEmpty()) {
                try {
                    double fare = Double.parseDouble(fareStr);
                    session.setAttribute("fare", fare);
                    response.getWriter().write("Fare updated successfully.");
                } catch (NumberFormatException e) {
                    response.getWriter().write("Invalid fare value.");
                }
            }
            return;
        }

        String trainNumber = request.getParameter("train_number");
        String departure = request.getParameter("departure");
        String destination = request.getParameter("destination");

        if (departure != null && departure.equalsIgnoreCase(destination)) {
            request.setAttribute("updateMessage", "Departure and Destination cannot be the same.");
            request.getRequestDispatcher("booking.jsp").forward(request, response);
            return;
        }

        String travelDate = request.getParameter("travel_date");
        String travelClass = request.getParameter("travel_class");
        String[] passengerNames = request.getParameterValues("passengerName[]");
        String[] ages = request.getParameterValues("age[]");
        String[] genders = request.getParameterValues("gender[]");
        int passengerCount = (passengerNames != null) ? passengerNames.length : 1;

        session.setAttribute("trainNumber", trainNumber);
        session.setAttribute("departure", departure);
        session.setAttribute("destination", destination);
        session.setAttribute("travelDate", travelDate);
        session.setAttribute("travelClass", travelClass);
        session.setAttribute("passengerNames", passengerNames);
        session.setAttribute("ages", ages);
        session.setAttribute("genders", genders);

        double fare = calculateFare(departure, destination, travelClass, passengerCount);
        session.setAttribute("fare", fare);

        request.setAttribute("trainNumber", trainNumber);
        request.setAttribute("departure", departure);
        request.setAttribute("destination", destination);
        request.setAttribute("travelDate", travelDate);
        request.setAttribute("travelClass", travelClass);
        request.setAttribute("fare", session.getAttribute("fare"));

        request.getRequestDispatcher("booking-details.jsp").forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String departure = request.getParameter("departure");
        String destination = request.getParameter("destination");
        String travelClass = request.getParameter("travel_class");
        String passengerCountStr = request.getParameter("passenger_count");

        if (departure == null || destination == null || travelClass == null || departure.equals(destination)) {
            response.getWriter().write("0");
            return;
        }

        int passengerCount = (passengerCountStr != null && !passengerCountStr.isEmpty()) ? Integer.parseInt(passengerCountStr) : 1;
        double totalFare = calculateFare(departure, destination, travelClass, passengerCount);
        session.setAttribute("fare", totalFare);

        response.setContentType("text/plain");
        response.getWriter().write(String.valueOf(totalFare));
    }

    private double calculateFare(String departure, String destination, String travelClass, int passengerCount) {
        int distance = DistanceCalculator.getDistance(departure, destination);
        double farePerKm;
        switch (travelClass) {
            case "3E": farePerKm = 2.0; break;
            case "1A": farePerKm = 5.0; break;
            case "2A": farePerKm = 4.0; break;
            case "3A": farePerKm = 3.0; break;
            case "CC": farePerKm = 2.5; break;
            case "EC": farePerKm = 4.5; break;
            case "2S": farePerKm = 1.0; break;
            case "SL": farePerKm = 1.5; break;
            default: throw new IllegalArgumentException("Invalid travel class.");
        }
        return distance * farePerKm * passengerCount;
    }
}
