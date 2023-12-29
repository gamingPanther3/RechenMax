package com.mlprograms.rechenmax;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class RechenMaxCalculatorUnitTest {

    @Test
    public void testAddition() {
        assertEquals("3", CalculatorActivity.calculate("1+2"));
        assertEquals("12,5", CalculatorActivity.calculate("10+2,5"));
    }

    @Test
    public void testSubtraction() {
        assertEquals("5", CalculatorActivity.calculate("8-3"));
        assertEquals("7,5", CalculatorActivity.calculate("10-2,5"));
    }

    @Test
    public void testMultiplication() {
        assertEquals("6", CalculatorActivity.calculate("2*3"));
        assertEquals("15", CalculatorActivity.calculate("5*3"));
    }

    @Test
    public void testDivision() {
        assertEquals("2", CalculatorActivity.calculate("6/3"));
        assertEquals("4,2", CalculatorActivity.calculate("21/5"));
    }

    @Test
    public void testExponential() {
        assertEquals("8", CalculatorActivity.calculate("2^3"));
        assertEquals("16", CalculatorActivity.calculate("2^4"));
    }

    @Test
    public void testInvalidInput() {
        assertEquals("Syntax Fehler", CalculatorActivity.calculate("2++3"));
        assertEquals("Syntax Fehler", CalculatorActivity.calculate("5(3+2)"));
    }

    @Test
    public void testLargeResult() {
        assertEquals("Wert zu groß", CalculatorActivity.calculate("1e+1001"));
    }

    @Test
    public void testParentheses() {
        assertEquals("10", CalculatorActivity.calculate("(2+3)*2"));
        assertEquals("25", CalculatorActivity.calculate("5*(3+2)"));
        assertEquals("20", CalculatorActivity.calculate("(4+6)*2"));
        assertEquals("48", CalculatorActivity.calculate("(10+2)*(3+1)"));
    }

    @Test
    public void testNegativeNumbers() {
        assertEquals("-1", CalculatorActivity.calculate("2-3"));
        assertEquals("-5", CalculatorActivity.calculate("0-5"));
        assertEquals("-4", CalculatorActivity.calculate("2*(-2)"));
    }

    @Test
    public void testComplexExpression() {
        assertEquals("15", CalculatorActivity.calculate("(3+5)*2-4/2^2"));
        assertEquals("17", CalculatorActivity.calculate("6*(4-2)+5"));
        assertEquals("22", CalculatorActivity.calculate("2*(3^2)+4"));
        assertEquals("30", CalculatorActivity.calculate("5*(2+3*(4-2))-10"));
        assertEquals("50", CalculatorActivity.calculate("5*(2+3^2)-10/2"));
        assertEquals("0,66666666667", CalculatorActivity.calculate("1/(2-3/6)"));
    }

    @Test
    public void testExtremeCases() {
        assertEquals("0", CalculatorActivity.calculate("0"));
        assertEquals("1", CalculatorActivity.calculate("1"));
        assertEquals("0.000000000000000001", CalculatorActivity.calculate("1e-18"));
        assertEquals("-1000000000000000000", CalculatorActivity.calculate("-1e+18"));
        assertEquals("1234567890123456789.012345678901", CalculatorActivity.calculate("1,234567890123456789012345678901e+18"));
        assertEquals("1", CalculatorActivity.calculate("1,0"));
        assertEquals("-0,1234567890123456789012345678901", CalculatorActivity.calculate("-0,1234567890123456789012345678901"));
    }

    @Test
    public void testChallengingExpressions() {
        assertEquals("50", CalculatorActivity.calculate("5*(2+3^2)-10/2"));
        assertEquals("0,66666666667", CalculatorActivity.calculate("1/(2-3/6)"));
        assertEquals("-40,090217132", CalculatorActivity.calculate("-2*(3^2,1)+4/(-0,2)"));
        assertEquals("4", CalculatorActivity.calculate("2^2"));
        assertEquals("-11", CalculatorActivity.calculate("5-(2+3^2)-10/2"));
        assertEquals("25", CalculatorActivity.calculate("7*2^2-3"));
    }

    @Test
    public void testTrigonometricFunctions() {
        String expressionSin = "sin(30)";
        String resultSin = CalculatorActivity.calculate(expressionSin);
        assertEquals(String.valueOf(Math.sin(Math.toRadians(30))), resultSin);

        assertEquals(String.valueOf(Math.cos(Math.toRadians(45))), CalculatorActivity.calculate("cos(45)"));

        String expressionTan = "tan(60)";
        String resultTan = CalculatorActivity.calculate(expressionTan);
        assertEquals(String.valueOf(Math.tan(Math.toRadians(60))), resultTan);
    }
}
