package com.example.accelerometer;

public class PointCalculatorController extends StatisticsPageActivity {

    public static String calculateScore(double highestAcceleration,
                                        double averageAcceleration,
                                        double percentageTimeAboveLimit,
                                        int aggressiveAccelerationCount,
                                        int aggressiveBrakingCount,
                                        long startTimestamp,
                                        long endTimestamp) {
        int highestAccelerationPoints = calculateAccelerationPoints(highestAcceleration);
        int averageAccelerationPoints = calculateAverageAccelerationPoints(averageAcceleration);
        int percentageTimeAboveLimitPoints = calculatePercentageTimeAboveLimitPoints(percentageTimeAboveLimit);
        int aggressiveAccelerationCountPoints = calculateAggresiveAccelerationCountPoints(aggressiveAccelerationCount, startTimestamp, endTimestamp);
        int aggressiveBrakingCountCountPoints = calculateAggressiveBrakingCountPoints(aggressiveBrakingCount, startTimestamp, endTimestamp);

        int overallScore = highestAccelerationPoints + averageAccelerationPoints + percentageTimeAboveLimitPoints + aggressiveAccelerationCountPoints + aggressiveBrakingCountCountPoints;

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
        double accelerationThreshold = 2.98;
        int pointsPerUnit = 10;
        double excessAcceleration = highestAcceleration - accelerationThreshold;
        if (excessAcceleration <= 0) {
            return 0;
        } else {
            int points = (int) Math.round(excessAcceleration * pointsPerUnit);
            return Math.min(points, 50);
        }
    }

    private static int calculateAverageAccelerationPoints(double averageAcceleration) {
        double accelerationThreshold = 2.98;
        int pointsPerUnit = 1;
        double excessAcceleration = Math.abs(averageAcceleration - accelerationThreshold);
        if (excessAcceleration <= 0) {
            return 0;
        } else {
            int points = (int) Math.round(excessAcceleration * pointsPerUnit);
            return Math.min(points, 50);
        }
    }

    private static int calculatePercentageTimeAboveLimitPoints(double percentageTimeAboveLimit) {
        int maxPoints = 50;
        int minPoints = 0;
        double maxPercentage = 10;
        double minPercentage = 0;

        int points = (int) Math.round(((percentageTimeAboveLimit - minPercentage) / (maxPercentage - minPercentage)) * (maxPoints - minPoints) + minPoints);
        points = Math.max(minPoints, points);
        points = Math.min(maxPoints, points);

        return points;
    }

    private static int calculateAggresiveAccelerationCountPoints(int aggressiveAccelerationCount, long startTimestamp, long endTimestamp) {
        int maxPoints = 10;
        int minPoints = 0;
        int maxCount = 50;
        int minCount = 10;

        double numDays = (endTimestamp - startTimestamp) / (1000.0 * 60 * 60 * 24);
        int points = (int) Math.round(((aggressiveAccelerationCount - minCount) / (double) (maxCount - minCount)) * (maxPoints - minPoints) + minPoints) * (int) numDays;
        points = Math.max(minPoints, points);
        points = Math.min(maxPoints, points);

        return points;
    }

    private static int calculateAggressiveBrakingCountPoints(int aggressiveBrakingCount, long startTimestamp, long endTimestamp) {
        int maxPoints = 10;
        int minPoints = 0;
        int maxCount = 50;
        int minCount = 10;

        double numDays = (long) Math.ceil((endTimestamp - startTimestamp) / (1000.0 * 60 * 60 * 24));
        int points = (int) Math.round(((aggressiveBrakingCount - minCount) / (double) (maxCount - minCount)) * (maxPoints - minPoints) + minPoints) * (int) numDays;
        points = Math.max(minPoints, points);
        points = Math.min(maxPoints, points);

        return points;
    }
}