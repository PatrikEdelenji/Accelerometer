package com.example.accelerometer;

import android.util.Log;

public class PointCalculatorController extends StatisticsPageView {

    public static String calculateScore(double highestAcceleration, double averageAcceleration, double timeSpentAboveLimit, double percentageTimeAboveLimit, int aggresiveAccelerationCount, int aggresiveBrakingCount, long startTimestamp, long endTimestamp) {
        // Calculate the score based on the statistics
        int highestAccelerationPoints = calculateAccelerationPoints(highestAcceleration);
        int averageAccelerationPoints = calculateAverageAccelerationPoints(averageAcceleration);
        int percentageTimeAboveLimitPoints = calculatePercentageTimeAboveLimitPoints(percentageTimeAboveLimit);
        int aggresiveAccelerationCountPoints = calculateAggresiveAccelerationCountPoints(aggresiveAccelerationCount, startTimestamp, endTimestamp);
        int aggresiveBrakingCountCountPoints = calculateAggressiveBrakingCountCountPoints(aggresiveBrakingCount, startTimestamp, endTimestamp);


        int overallScore = highestAccelerationPoints + averageAccelerationPoints + percentageTimeAboveLimitPoints + aggresiveAccelerationCountPoints + aggresiveBrakingCountCountPoints;

        if (overallScore >= 0 && overallScore <= 25) {
            return "Pažljiv vozač!";
        } else if (overallScore >= 26 && overallScore <= 75) {
            return "Blago agresivni vozač!";
        } else if (overallScore >= 76 && overallScore <= 125) {
            return "Agresivni vozač!";
        } else if (overallScore >= 126 && overallScore <= 175) {
            return "Poprilično agresivni vozač!";
        } else if (overallScore >= 176 && overallScore <= 250) {
            return "Jako agresivni vozač!";
        } else {
            return "Nedovoljno informacija!";
        }



    }

    private static int calculateAccelerationPoints(double highestAcceleration) {
        double accelerationThreshold = 2.98; // example threshold value in m/s^2
        int pointsPerUnit = 10; // example points per unit of acceleration above threshold
        double excessAcceleration = highestAcceleration - accelerationThreshold;
        if (excessAcceleration <= 0) {
            return 0; // no points awarded if highest acceleration is below threshold
        } else {
            int points = (int) Math.round(excessAcceleration * pointsPerUnit);
            return Math.min(points, 50); // limit points to 50
        }
    }

    private static int calculateAverageAccelerationPoints(double averageAcceleration) {
        double accelerationThreshold = 2.98; // example threshold value in m/s^2
        int pointsPerUnit = 1; // example points per unit of acceleration above/below threshold
        double excessAcceleration = Math.abs(averageAcceleration - accelerationThreshold);
        if (excessAcceleration <= 0) {
            return 0; // no points awarded if average acceleration is within threshold
        } else {
            int points = (int) Math.round(excessAcceleration * pointsPerUnit);
            return Math.min(points, 50); // limit points to 50
        }
    }

    private static int calculatePercentageTimeAboveLimitPoints(double percentageTimeAboveLimit) {
        int maxPoints = 50; // maximum points that can be earned
        int minPoints = 0; // minimum points that can be earned
        double maxPercentage = 0.2; // maximum percentage above limit that earns maxPoints
        double minPercentage = 0.05; // minimum percentage above limit that earns minPoints

        // calculate the points based on the percentage above limit
        int points = (int) Math.round(((percentageTimeAboveLimit - minPercentage) / (maxPercentage - minPercentage)) * (maxPoints - minPoints) + minPoints);

        // ensure that the points are within the allowed range
        points = Math.max(minPoints, points);
        points = Math.min(maxPoints, points);

        return points;
    }

    private static int calculateAggresiveAccelerationCountPoints(int aggresiveAccelerationCount, double startTimestamp, double endTimestamp) {
        int maxPoints = 10; // maximum points that can be earned
        int minPoints = 0; // minimum points that can be earned
        int maxCount = 50; // maximum number of aggressive accelerations that earns maxPoints
        int minCount = 10; // minimum number of aggressive accelerations that earns minPoints

        // calculate the points based on the number of aggressive accelerations
        double numDays = (endTimestamp - startTimestamp) / (1000.0 * 60 * 60 * 24);
        int points = (int) Math.round(((aggresiveAccelerationCount - minCount) / (double) (maxCount - minCount)) * (maxPoints - minPoints) + minPoints) * (int) numDays;

        // ensure that the points are within the allowed range
        points = Math.max(minPoints, points);
        points = Math.min(maxPoints, points);

        return points;
    }

    private static int calculateAggressiveBrakingCountCountPoints(int aggresiveBrakingCount, double startTimestamp, double endTimestamp) {
        int maxPoints = 10; // maximum points that can be earned
        int minPoints = 0; // minimum points that can be earned
        int maxCount = 50; // maximum count of aggressive braking events within the time period that earns maxPoints
        int minCount = 10; // minimum count of aggressive braking events within the time period that earns minPoints

        // calculate the number of days in the time period
        double numDays = (long) Math.ceil((endTimestamp - startTimestamp) / (1000.0 * 60 * 60 * 24));

        // calculate the points based on the count of aggressive braking events within the time period
        int points = (int) Math.round(((aggresiveBrakingCount - minCount) / (double) (maxCount - minCount)) * (maxPoints - minPoints) + minPoints) * (int) numDays;

        // ensure that the points are within the allowed range
        points = Math.max(minPoints, points);
        points = Math.min(maxPoints, points);

        return points;
    }
}