package com.mlprograms.rechenmax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

/*
 * WORK IN PROGESS DO NOT USE !!!
 */
public class ConvertEngine {
    public static final String DEG                  = "Deg";
    public static final String RAD                  = "Rad";
    public static final String PITCH                = "Pitch";
    public static final String GON                  = "Gon";
    public static final String MRAD                 = "Mrad";
    public static final String MINUTEOFTHEARC       = "MinuteOfTheArc";
    public static final String SECONDARC            = "SecondArc";

    public static final String SQUAREMILLIMETER     = "SquareMillimeter";
    public static final String SQUARECENTIMETER     = "SquareCentimeter";
    public static final String SQUAREMETER          = "SquareMeter";
    public static final String SQUAREKILOMETER      = "SquareKilometer";
    public static final String AR                   = "Ar";
    public static final String DEKAR                = "Dekar";
    public static final String HECTARES             = "Hectares";
    public static final String SQUAREINCH           = "SquareInch";
    public static final String SQUAREFEET           = "SquareFeet";
    public static final String SQUAREYARD           = "SquareYard";
    public static final String ACRE                 = "Acre";
    public static final String SQUAREMILES          = "SquareMiles";

    public static final String BIT                  = "Bit";
    public static final String BYTE                 = "Byte";
    public static final String KILOBIT              = "Kilobit";
    public static final String KILOBYTE             = "Kilobyte";
    public static final String MEGABIT              = "Megabit";
    public static final String MEGABYTE             = "Megabyte";
    public static final String GIGABIT              = "Gigabit";
    public static final String GIGABYTE             = "Gigabyte";
    public static final String TERABIT              = "Terabit";
    public static final String TERABYTE             = "Terabyte";
    public static final String PETABIT              = "Petabit";
    public static final String PETABYTE             = "Petabyte";

    public static final String PIKOMETER            = "Pikometer";
    public static final String NANOMETER            = "Nanometer";
    public static final String MIKROMETER           = "Mikrometer";
    public static final String MILLIMETER           = "Millimeter";
    public static final String CENTIMETER           = "Centimeter";
    public static final String DEZIMETER            = "Dezimeter";
    public static final String METER                = "Meter";
    public static final String HEKTOMETER           = "Hektometer";
    public static final String KILOMETER            = "Kilometer";
    public static final String FEET                 = "Feet";
    public static final String YARD                 = "Yard";
    public static final String MILES                = "Miles";
    public static final String SEAMILES             = "Seamiles";
    public static final String LIGHTYEAR            = "Lightyear";
    public static final String ASTRONOMICALUNIT     = "Astronomicalunit";

    public static final String MIKROLITER           = "Mikroliter";
    public static final String MILLILITER           = "Milliliter";
    public static final String DEZILITER            = "Deziliter";
    public static final String LITER                = "Liter";
    public static final String ZENTILITER           = "Zentiliter";
    public static final String METRISCHETASSE       = "MetrischeTasse";
    public static final String KUBIKMILLIMETER      = "Kubikmillimeter";
    public static final String KUBIKZENTIMETER      = "Kubikzentimeter";
    public static final String KUBIKMETER           = "Kubikmeter";
    public static final String KUBIKINCH            = "KubikInch";
    public static final String KUBIKFEET            = "KubikFeet";
    public static final String KUBIKYARD            = "KubikYard";
    public static final String GILLSUS              = "GillsUS";
    public static final String GILLSUK              = "GillsUK";
    public static final String TEASPOONSUS          = "TeaspoonsUS";
    public static final String TEASPOONSUK          = "TeaspoonsUK";
    public static final String TABLESPOONSUS        = "TablespoonsUS";
    public static final String TABLESPOONSUK        = "TablespoonsUK";
    public static final String CUPSUS               = "CupsUS";
    public static final String CUPSUK               = "CupsUK";
    public static final String GALLONUS             = "GallonUS";
    public static final String GALLONUK             = "GallonUK";
    public static final String BARRELS              = "Barrels";

    public static final String ATOMAREMASSENEINHEIT = "Mikroliter";
    public static final String MIKROGRAMM           = "Milliliter";
    public static final String MILLIGRAMM           = "Deziliter";
    public static final String ZENTIGRAMM           = "Liter";
    public static final String GRAMM                = "Zentiliter";
    public static final String HEKTOGRAMM           = "MetrischeTasse";
    public static final String DEKAGRAMM            = "Kubikmillimeter";
    public static final String KARAT                = "Kubikzentimeter";
    public static final String NEWTON               = "Kubikmeter";
    public static final String KILOGRAMM            = "KubikInch";
    public static final String TONNE                = "KubikFeet";
    public static final String KILOTONNE            = "KubikYard";
    public static final String GRAINS               = "GillsUS";
    public static final String DRAMS                = "GillsUK";
    public static final String UNZEN                = "TeaspoonsUS";
    public static final String PUND                = "TeaspoonsUK";
    public static final String SHORTTONS            = "TablespoonsUS";
    public static final String LONGTONS             = "TablespoonsUK";

    // HashMap to store conversion factors for each unit
    private static final HashMap<String, Double> convertFactors = new HashMap<>();

    static {
        convertFactors.put(DEG,                  1.0);
        convertFactors.put(RAD,                  Math.PI / 180.0);
        convertFactors.put(PITCH,                0.0572958);
        convertFactors.put(GON,                  1.11111);
        convertFactors.put(MRAD,                 1000.0);
        convertFactors.put(MINUTEOFTHEARC,       0.0166667);
        convertFactors.put(SECONDARC,            0.000277778);

        convertFactors.put(SQUAREMILLIMETER,     1e-6);
        convertFactors.put(SQUARECENTIMETER,     0.0001);
        convertFactors.put(SQUAREMETER,          1.0);
        convertFactors.put(SQUAREKILOMETER,      1e6);
        convertFactors.put(AR,                   100.0);
        convertFactors.put(DEKAR,                1000.0);
        convertFactors.put(HECTARES,             10000.0);
        convertFactors.put(SQUAREINCH,           0.00064516);
        convertFactors.put(SQUAREFEET,           0.092903);
        convertFactors.put(SQUAREYARD,           0.836127);
        convertFactors.put(ACRE,                 4046.86);
        convertFactors.put(SQUAREMILES,          2589988.11);

        convertFactors.put(BIT,                  0.125);
        convertFactors.put(BYTE,                 1.0);
        convertFactors.put(KILOBIT,              128.0);
        convertFactors.put(KILOBYTE,             1024.0);
        convertFactors.put(MEGABIT,              131072.0);
        convertFactors.put(MEGABYTE,             1048576.0);
        convertFactors.put(GIGABIT,              134217728.0);
        convertFactors.put(GIGABYTE,             1073741824.0);
        convertFactors.put(TERABIT,              137438953472.0);
        convertFactors.put(TERABYTE,             1099511627776.0);
        convertFactors.put(PETABIT,              140737488355328.0);
        convertFactors.put(PETABYTE,             1125899906842624.0);

        convertFactors.put(PIKOMETER,        1e-12);
        convertFactors.put(NANOMETER,        1e-9);
        convertFactors.put(MIKROMETER,       1e-6);
        convertFactors.put(MILLIMETER,       0.001);
        convertFactors.put(CENTIMETER,       0.01);
        convertFactors.put(DEZIMETER,        0.1);
        convertFactors.put(METER,            1.0);
        convertFactors.put(HEKTOMETER,       100.0);
        convertFactors.put(KILOMETER,        1000.0);
        convertFactors.put(FEET,             0.3048);
        convertFactors.put(YARD,             0.9144);
        convertFactors.put(MILES,            1609.344);
        convertFactors.put(SEAMILES,         1852.0);
        convertFactors.put(LIGHTYEAR,        9.461e15);
        convertFactors.put(ASTRONOMICALUNIT, 1.496e11);

        convertFactors.put(MIKROLITER,           1e-9);
        convertFactors.put(MILLILITER,           1e-6);
        convertFactors.put(DEZILITER,            1e-4);
        convertFactors.put(LITER,                0.001);
        convertFactors.put(ZENTILITER,           1e-5);
        convertFactors.put(METRISCHETASSE,       0.25);
        convertFactors.put(KUBIKMILLIMETER,      1e-9);
        convertFactors.put(KUBIKZENTIMETER,      1e-6);
        convertFactors.put(KUBIKMETER,           1.0);
        convertFactors.put(KUBIKINCH,            1.63871e-5);
        convertFactors.put(KUBIKFEET,            0.0283168);
        convertFactors.put(KUBIKYARD,            0.764555);
        convertFactors.put(GILLSUS,              0.000118294);
        convertFactors.put(GILLSUK,              0.000142065);
        convertFactors.put(TEASPOONSUS,          0.00000492892);
        convertFactors.put(TEASPOONSUK,          0.00000591939);
        convertFactors.put(TABLESPOONSUS,        0.0000147868);
        convertFactors.put(TABLESPOONSUK,        0.0000177582);
        convertFactors.put(CUPSUS,               0.000236588);
        convertFactors.put(CUPSUK,               0.000284131);
        convertFactors.put(GALLONUS,             0.00378541);
        convertFactors.put(GALLONUK,             0.00454609);
        convertFactors.put(BARRELS,              0.158987);

        convertFactors.put(ATOMAREMASSENEINHEIT, 1.66054e-27);
        convertFactors.put(MIKROGRAMM,           1e-9);
        convertFactors.put(MILLIGRAMM,           1e-6);
        convertFactors.put(ZENTIGRAMM,           0.01);
        convertFactors.put(GRAMM,                0.001);
        convertFactors.put(HEKTOGRAMM,           0.1);
        convertFactors.put(DEKAGRAMM,            10.0);
        convertFactors.put(KARAT,                0.0002);
        convertFactors.put(NEWTON,               0.101972);
        convertFactors.put(KILOGRAMM,            1.0);
        convertFactors.put(TONNE,                1000.0);
        convertFactors.put(KILOTONNE,            1000000.0);
        convertFactors.put(GRAINS,               6.47989e-5);
        convertFactors.put(DRAMS,                0.00177185);
        convertFactors.put(UNZEN,                0.0283495);
        convertFactors.put(PUND,                 0.453592);
        convertFactors.put(SHORTTONS,            907.185);
        convertFactors.put(LONGTONS,             1016.05);
    }

    // Method to convert value from one unit to another
    public static String convert(double value, String fromUnit, String toUnit) {
        // Check if both units are valid
        if (!convertFactors.containsKey(fromUnit) || !convertFactors.containsKey(toUnit)) {
            throw new IllegalArgumentException("Invalid unit(s) specified.");
        }

        // Retrieve conversion factors for fromUnit and toUnit
        double fromFactor = convertFactors.get(fromUnit);
        double toFactor = convertFactors.get(toUnit);

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