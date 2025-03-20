package com.welcome;

import java.util.HashMap;
import java.util.Map;

public class DistanceCalculator {
    private static final Map<String, Map<String, Integer>> distanceMap = new HashMap<>();

    static {
        distanceMap.put("Thiruvananthapuram", Map.of(
            "Ernakulam", 220, "Kollam", 65, "Thrissur", 270, "Alappuzha", 150, "Aluva", 235, "Guruvayur", 290));

        distanceMap.put("Ernakulam", Map.of(
            "Thiruvananthapuram", 220, "Kollam", 155, "Thrissur", 80, "Alappuzha", 50, "Aluva", 25, "Guruvayur", 100));

        distanceMap.put("Kollam", Map.of(
            "Thiruvananthapuram", 65, "Ernakulam", 155, "Thrissur", 200, "Alappuzha", 90, "Aluva", 170, "Guruvayur", 225));

        distanceMap.put("Thrissur", Map.of(
            "Thiruvananthapuram", 270, "Ernakulam", 80, "Kollam", 200, "Alappuzha", 130, "Aluva", 55, "Guruvayur", 25));

        distanceMap.put("Alappuzha", Map.of(
            "Thiruvananthapuram", 150, "Ernakulam", 50, "Kollam", 90, "Thrissur", 130, "Aluva", 70, "Guruvayur", 150));

        distanceMap.put("Aluva", Map.of(
            "Thiruvananthapuram", 235, "Ernakulam", 25, "Kollam", 170, "Thrissur", 55, "Alappuzha", 70, "Guruvayur", 80));

        distanceMap.put("Guruvayur", Map.of(
            "Thiruvananthapuram", 290, "Ernakulam", 100, "Kollam", 225, "Thrissur", 25, "Alappuzha", 150, "Aluva", 80));
    }

    public static int getDistance(String from, String to) {
        return distanceMap.getOrDefault(from, Map.of()).getOrDefault(to, -1); 
    }
}
