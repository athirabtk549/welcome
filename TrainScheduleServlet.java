package com.welcome;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.gson.Gson;

public class TrainScheduleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String dayOfWeek = request.getParameter("day");

        if (dayOfWeek == null || dayOfWeek.isEmpty()) {
            out.write("[]");
            return;
        }

        List<Train> trains = new ArrayList<>();
        String jdbcURL = "jdbc:mysql://localhost:3306/trainbookingdb";
        String dbUser = "root";
        String dbPassword = "root";

        int lastTrainNumber = -1;
        String lastTrainName = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
                String query = "SELECT train_number, train_name FROM train_schedule WHERE FIND_IN_SET(?, days_of_operation)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, dayOfWeek);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int trainNumber = rs.getInt("train_number");
                            String trainName = rs.getString("train_name");
                            trains.add(new Train(trainNumber, trainName));

                            // Store last train details
                            lastTrainNumber = trainNumber;
                            lastTrainName = trainName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.write("[]");
            return;
        }

        // Store last train details in session after loop
        if (lastTrainNumber != -1 && lastTrainName != null) {
            HttpSession session = request.getSession();
            session.setAttribute("trainNumber", lastTrainNumber);
            session.setAttribute("trainName", lastTrainName);
        }

        String jsonResponse = new Gson().toJson(trains);
        out.write(jsonResponse);
    }

    static class Train {
        int train_number;
        String train_name;

        public Train(int train_number, String train_name) {
            this.train_number = train_number;
            this.train_name = train_name;
        }
    }
}
