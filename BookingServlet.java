package com.welcome;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;

import javax.servlet.RequestDispatcher;
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

        // Retrieving booking details from request
        String trainNumber = request.getParameter("train_number");
        String departure = request.getParameter("departure");
        String destination = request.getParameter("destination");

        if (departure.equalsIgnoreCase(destination)) {
            request.setAttribute("updateMessage", "Departure and Destination cannot be the same.");
            request.getRequestDispatcher("booking.jsp").forward(request, response);
            return;
        }

        String travelDate = request.getParameter("travel_date");
        String travelClass = request.getParameter("travel_class");
        String[] passengerNames = request.getParameterValues("passengerName[]");
        String[] ages = request.getParameterValues("age[]");
        String[] genders = request.getParameterValues("gender[]");
        
        session.setAttribute("trainNumber", trainNumber);
        session.setAttribute("departure", departure);
        session.setAttribute("destination", destination);
        session.setAttribute("travelDate", travelDate);
        session.setAttribute("travelClass", travelClass);
        session.setAttribute("passengerNames", passengerNames);
        session.setAttribute("ages", ages);
        session.setAttribute("genders", genders);


        // Calculate fare
        double fare = calculateFare(departure, destination, travelClass);
        session.setAttribute("fare", fare);

        request.setAttribute("trainNumber", trainNumber);
        request.setAttribute("departure", departure);
        request.setAttribute("destination", destination);
        request.setAttribute("travelDate", travelDate);
        request.setAttribute("travelClass", travelClass);
        request.setAttribute("fare", fare);
        	
        if (passengerNames != null) {
            // Iterate over passengerNames array and print each name
            for (int i = 0; i < passengerNames.length; i++) {
                System.out.println("Passenger " + (i + 1) + ": " + passengerNames[i]);
            }
        }

        System.out.println("Passenger Names: " + Arrays.toString(passengerNames));
        System.out.println("Ages: " + Arrays.toString(ages));
        System.out.println("Genders: " + Arrays.toString(genders));

        String action = request.getParameter("action");
        if ("edit".equals(action)) {
            response.sendRedirect("booking.jsp?edit=true"); // Redirect to booking.jsp in edit mode
        } else {
            response.sendRedirect("booking-details.jsp");  // Proceed to booking-details.jsp
        }
    }
    // Method to calculate fare based on travel class and distance
    private double calculateFare(String departure, String destination, String travelClass) {
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
        return distance * farePerKm;
    }
}
