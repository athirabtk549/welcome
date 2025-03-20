package com.welcome;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginServlet extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    String username = request.getParameter("username");
	    String password = request.getParameter("password");
	    try (Connection conn = DbConnection.getConnection()) {
	        String query = "SELECT username FROM users WHERE username = ? AND password = ?";
	        PreparedStatement stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        stmt.setString(2, password);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            String loggedInUser = rs.getString("username"); 
	            HttpSession session = request.getSession();
	            session.setAttribute("username", loggedInUser);
	            
	            response.sendRedirect("Home.jsp"); 
	        } else {
	            request.setAttribute("loginError", "Invalid username or password!"); 
	            request.getRequestDispatcher("index.jsp").forward(request, response); 
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        request.setAttribute("loginError", "Something went wrong. Please try again.");
	        request.getRequestDispatcher("index.jsp").forward(request, response);
	    }
	}
}