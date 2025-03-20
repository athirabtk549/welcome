package com.welcome;
 
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
public class CancelTicketsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
 
        String username = (String) session.getAttribute("username");
        String bookingId = request.getParameter("booking_id");
 
        if (bookingId == null || bookingId.isEmpty()) {
            response.sendRedirect("BookingHistoryServlet?error=InvalidBookingId");
            return;
        }
 
        Connection conn = null;
        PreparedStatement insertStmt = null;
        PreparedStatement deleteStmt = null;
 
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/trainbookingdb", "root", "root");
 
           
            conn.setAutoCommit(false);
 
            
            String insertSQL = "INSERT INTO cancelled_bookings (booking_id, username, train_number, departure, destination, travel_date, travel_class) "
                             + "SELECT booking_id, username, train_number, departure, destination, travel_date, travel_class FROM bookings WHERE booking_id = ? AND username = ?";
            insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, bookingId);
            insertStmt.setString(2, username);
            int rowsInserted = insertStmt.executeUpdate();
 
            if (rowsInserted > 0) {
                String deleteSQL = "DELETE FROM bookings WHERE booking_id = ? AND username = ?";
                deleteStmt = conn.prepareStatement(deleteSQL);
                deleteStmt.setString(1, bookingId);
                deleteStmt.setString(2, username);
                int rowsDeleted = deleteStmt.executeUpdate();
 
                if (rowsDeleted > 0) {
                    conn.commit(); 
                    response.sendRedirect("BookingHistoryServlet?message=TicketCancelled");
                } else {
                    conn.rollback(); 
                    response.sendRedirect("BookingHistoryServlet?error=CancellationFailed");
                }
            } else {
                conn.rollback();
                response.sendRedirect("BookingHistoryServlet?error=InsertFailed");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            response.sendRedirect("BookingHistoryServlet?error=DatabaseError");
        } finally {
            try {
                if (insertStmt != null) insertStmt.close();
                if (deleteStmt != null) deleteStmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}