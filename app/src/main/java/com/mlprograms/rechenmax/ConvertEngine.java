package com.mlprograms.rechenmax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

/*
 * WORK IN PROGESS DO NOT USE !!!
 */
public class ConvertEngine {
    // Define constants for unit names
    public static final String PIKOMETER        = "Pikometer";
    public static final String NANOMETER        = "Nanometer";
    public static final String MIKROMETER       = "Mikrometer";
    public static final String MILLIMETER       = "Millimeter";
    public static final String CENTIMETER       = "Centimeter";
    public static final String DEZIMETER        = "Dezimeter";
    public static final String METER            = "Meter";
    public static final String HEKTOMETER       = "Hektometer";
    public static final String KILOMETER        = "Kilometer";
    public static final String FEET             = "Feet";
    public static final String YARD             = "Yard";
    public static final String MILES            = "Miles";
    public static final String SEAMILES         = "Seamiles";
    public static final String LIGHTYEAR        = "Lightyear";
    public static final String ASTRONOMICALUNIT = "Astronomicalunit";

    // HashMap to store conversion factors for each unit
    private static final HashMap<String, Double> convertFactorsDistance = new HashMap<>();

    // Initialize conversion factors
    static {
        convertFactorsDistance.put(PIKOMETER, 1e-12);
        convertFactorsDistance.put(NANOMETER, 1e-9);
        convertFactorsDistance.put(MIKROMETER, 1e-6);
        convertFactorsDistance.put(MILLIMETER, 0.001);
        convertFactorsDistance.put(CENTIMETER, 0.01);
        convertFactorsDistance.put(DEZIMETER, 0.1);
        convertFactorsDistance.put(METER, 1.0);
        convertFactorsDistance.put(HEKTOMETER, 100.0);
        convertFactorsDistance.put(KILOMETER, 1000.0);
        convertFactorsDistance.put(FEET, 0.3048);
        convertFactorsDistance.put(YARD, 0.9144);
        convertFactorsDistance.put(MILES, 1609.344);
        convertFactorsDistance.put(SEAMILES, 1852.0);
        convertFactorsDistance.put(LIGHTYEAR, 9.461e15);
        convertFactorsDistance.put(ASTRONOMICALUNIT, 1.496e11);
    }

    // Method to convert value from one unit to another
    public static String convert(double value, String fromUnit, String toUnit) {
        // Check if both units are valid
        if (!convertFactorsDistance.containsKey(fromUnit) || !convertFactorsDistance.containsKey(toUnit)) {
            throw new IllegalArgumentException("Invalid unit(s) specified.");
        }

        // Retrieve conversion factors for fromUnit and toUnit
        double fromFactor = convertFactorsDistance.get(fromUnit);
        double toFactor = convertFactorsDistance.get(toUnit);

        // Calculate the result using conversion factors
        double result = value * (fromFactor / toFactor);

        // Convert result to BigDecimal to handle precision
        BigDecimal resultBigDecimal = BigDecimal.valueOf(result);

        // Round the result to remove unnecessary decimal places
        resultBigDecimal = resultBigDecimal.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();

        // Convert the result to a string
        return resultBigDecimal.toPlainString();
    }
}