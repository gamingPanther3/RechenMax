package com.mlprograms.rechenmax;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class RechenMaxCalculatorUnitTest {

    @Test
    public void testAddition() {
        assertEquals("3", CalculatorEngine.calculate("1+2"));
        assertEquals("12,5", CalculatorEngine.calculate("10+2,5"));
    }

    @Test
    public void testSubtraction() {
        assertEquals("5", CalculatorEngine.calculate("8-3"));
        assertEquals("7,5", CalculatorEngine.calculate("10-2,5"));
    }

    @Test
    public void testMultiplication() {
        assertEquals("6", CalculatorEngine.calculate("2*3"));
        assertEquals("15", CalculatorEngine.calculate("5*3"));
    }

    @Test
    public void testDivision() {
        assertEquals("2", CalculatorEngine.calculate("6/3"));
        assertEquals("4,2", CalculatorEngine.calculate("21/5"));
    }

    @Test
    public void testExponential() {
        assertEquals("8", CalculatorEngine.calculate("2^3"));
        assertEquals("16", CalculatorEngine.calculate("2^4"));
    }

    @Test
    public void testInvalidInput() {
        assertEquals("Syntax Fehler", CalculatorEngine.calculate("2++3"));
        assertEquals("Syntax Fehler", CalculatorEngine.calculate("5(3+2)"));
    }

    @Test
    public void testLargeResult() {
        assertEquals("Wert zu groß", CalculatorEngine.calculate("1e+1001"));
    }

    @Test
    public void testParentheses() {
        assertEquals("10", CalculatorEngine.calculate("(2+3)*2"));
        assertEquals("25", CalculatorEngine.calculate("5*(3+2)"));
        assertEquals("20", CalculatorEngine.calculate("(4+6)*2"));
        assertEquals("48", CalculatorEngine.calculate("(10+2)*(3+1)"));
    }

    @Test
    public void testNegativeNumbers() {
        assertEquals("-1", CalculatorEngine.calculate("2-3"));
        assertEquals("-5", CalculatorEngine.calculate("0-5"));
        assertEquals("-4", CalculatorEngine.calculate("2*(-2)"));
    }

    @Test
    public void testComplexExpression() {
        assertEquals("15", CalculatorEngine.calculate("(3+5)*2-4/2^2"));
        assertEquals("17", CalculatorEngine.calculate("6*(4-2)+5"));
        assertEquals("22", CalculatorEngine.calculate("2*(3^2)+4"));
        assertEquals("30", CalculatorEngine.calculate("5*(2+3*(4-2))-10"));
        assertEquals("50", CalculatorEngine.calculate("5*(2+3^2)-10/2"));
        assertEquals("0,66666666667", CalculatorEngine.calculate("1/(2-3/6)"));
    }

    @Test
    public void testExtremeCases() {
        assertEquals("0", CalculatorEngine.calculate("0"));
        assertEquals("1", CalculatorEngine.calculate("1"));
        assertEquals("0.000000000000000001", CalculatorEngine.calculate("1e-18"));
        assertEquals("-1000000000000000000", CalculatorEngine.calculate("-1e+18"));
        assertEquals("1234567890123456789.012345678901", CalculatorEngine.calculate("1,234567890123456789012345678901e+18"));
        assertEquals("1", CalculatorEngine.calculate("1,0"));
        assertEquals("-0,1234567890123456789012345678901", CalculatorEngine.calculate("-0,1234567890123456789012345678901"));
    }

    @Test
    public void testChallengingExpressions() {
        assertEquals("50", CalculatorEngine.calculate("5*(2+3^2)-10/2"));
        assertEquals("0,66666666667", CalculatorEngine.calculate("1/(2-3/6)"));
        assertEquals("-40,090217132", CalculatorEngine.calculate("-2*(3^2,1)+4/(-0,2)"));
        assertEquals("4", CalculatorEngine.calculate("2^2"));
        assertEquals("-11", CalculatorEngine.calculate("5-(2+3^2)-10/2"));
        assertEquals("25", CalculatorEngine.calculate("7*2^2-3"));
    }

    @Test
    public void testTrigonometricFunctions() {
        String expressionSin = "sin(30)";
        String resultSin = CalculatorEngine.calculate(expressionSin);
        assertEquals(String.valueOf(Math.sin(Math.toRadians(30))), resultSin);

        assertEquals(String.valueOf(Math.cos(Math.toRadians(45))), CalculatorEngine.calculate("cos(45)"));

        String expressionTan = "tan(60)";
        String resultTan = CalculatorEngine.calculate(expressionTan);
        assertEquals(String.valueOf(Math.tan(Math.toRadians(60))), resultTan);
    }
}
