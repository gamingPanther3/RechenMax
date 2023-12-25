package com.mlprograms.rechenmax;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host),
 *
 * @see <a href="http://d,android,com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testCalculatorActivity() {
        // addition
        assertEquals("3", CalculatorActivity.calculate("1+2"));
        assertEquals("12,5", CalculatorActivity.calculate("10+2,5"));

        // subtraction
        assertEquals("5", CalculatorActivity.calculate("8-3"));
        assertEquals("7,5", CalculatorActivity.calculate("10-2,5"));

        // multiplication
        assertEquals("6", CalculatorActivity.calculate("2*3"));
        assertEquals("15", CalculatorActivity.calculate("5*3"));

        // division
        assertEquals("2", CalculatorActivity.calculate("6/3"));
        assertEquals("4,2", CalculatorActivity.calculate("21/5"));

        // exponential
        assertEquals("8", CalculatorActivity.calculate("2^3"));
        assertEquals("16", CalculatorActivity.calculate("2^4"));

        // square Root
        assertEquals("2", CalculatorActivity.calculate("√(4)"));
        assertEquals("3", CalculatorActivity.calculate("√(9)"));

        // factorial
        assertEquals("120", CalculatorActivity.calculate("5!"));
        assertEquals("1", CalculatorActivity.calculate("0!"));

        // scientific notation
        assertEquals("1000", CalculatorActivity.calculate("1e3"));
        assertEquals("0,000001", CalculatorActivity.calculate("1e-6"));
        assertEquals("2500000000", CalculatorActivity.calculate("2,5e+9"));
        assertEquals("123,456", CalculatorActivity.calculate("1,23456e+2"));
        assertEquals("0,0000000123456", CalculatorActivity.calculate("1,23456e-8"));
        assertEquals("1,23", CalculatorActivity.calculate("1,23e+0"));

        // invalid input
        assertEquals("Syntax Fehler", CalculatorActivity.calculate("2++3"));
        assertEquals("Syntax Fehler", CalculatorActivity.calculate("5(3+2)"));

        // large result
        //assertEquals("Wert zu groß", CalculatorActivity.calculate("1e+1000"));

        // parentheses
        assertEquals("10", CalculatorActivity.calculate("(2+3)*2"));
        assertEquals("25", CalculatorActivity.calculate("5*(3+2)"));
        assertEquals("20", CalculatorActivity.calculate("(4+6)*2"));
        assertEquals("48", CalculatorActivity.calculate("(10+2)*(3+1)"));

        // negative numbers
        assertEquals("-1", CalculatorActivity.calculate("2-3"));
        assertEquals("-5", CalculatorActivity.calculate("0-5"));
        assertEquals("-4", CalculatorActivity.calculate("2*(-2)"));
        assertEquals("-25", CalculatorActivity.calculate("-5*(3+2)"));

        // complex expression
        assertEquals("15", CalculatorActivity.calculate("(3+5)*2-4/2^2"));
        assertEquals("17", CalculatorActivity.calculate("6*(4-2)+5"));
        assertEquals("22", CalculatorActivity.calculate("2*(3^2)+4"));
        assertEquals("30", CalculatorActivity.calculate("5*(2+3*(4-2))-10"));
        assertEquals("50", CalculatorActivity.calculate("5*(2+3^2)-10/2"));
        assertEquals("0,66666666667", CalculatorActivity.calculate("1/(2-3/6)"));
        assertEquals("-40,090217132", CalculatorActivity.calculate("-2*(3^2,1)+4/(-0,2)"));
        assertEquals("0,00000088", CalculatorActivity.calculate("(2,2e-3)*(4e-4)"));

        // edge cases
        assertEquals("Kein Teilen durch 0", CalculatorActivity.calculate("1/0"));
        assertEquals("Wert zu groß", CalculatorActivity.calculate("1e+1000+1e+999"));
        assertEquals("Nur reelle Zahlen", CalculatorActivity.calculate("√(-4)"));
        assertEquals("Syntax Fehler", CalculatorActivity.calculate("2*(3^2+4"));
        assertEquals("Wert zu groß", CalculatorActivity.calculate("10^10000"));

        // extreme cases
        assertEquals("0", CalculatorActivity.calculate("0"));
        assertEquals("1", CalculatorActivity.calculate("1"));
        assertEquals("0,000000000000000001", CalculatorActivity.calculate("1e-18"));
        assertEquals("-1000000000000000000", CalculatorActivity.calculate("-1e+18"));
        assertEquals("999999999999999999,999999999999999999", CalculatorActivity.calculate("1e+18-0,000000000000000001"));
        assertEquals("1,2345678901e+18", CalculatorActivity.calculate("1,234567890123456789012345678901e+18"));
        assertEquals("1", CalculatorActivity.calculate("1,0"));
        assertEquals("-0,1234567890123456789012345678901", CalculatorActivity.calculate("-0,1234567890123456789012345678901"));

        // challenging expressions
        assertEquals("50", CalculatorActivity.calculate("5*(2+3^2)-10/2"));
        assertEquals("0,66666666667", CalculatorActivity.calculate("1/(2-3/6)"));
        assertEquals("-40,090217132", CalculatorActivity.calculate("-2*(3^2,1)+4/(-0,2)"));
        assertEquals("4", CalculatorActivity.calculate("2^2"));
        assertEquals("-11", CalculatorActivity.calculate("5-(2+3^2)-10/2"));
        assertEquals("25", CalculatorActivity.calculate("7*2^2-3"));
    }
}