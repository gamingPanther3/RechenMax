package com.mlprograms.rechenmax;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorActivity {
    public static BigDecimal applyOperator(final BigDecimal operand1, final BigDecimal operand2, final String operator, final MathContext mc) {
        switch (operator) {
            case "+":
                return operand1.add(operand2);
            case "-":
                return operand1.subtract(operand2);
            case "*":
                return operand1.multiply(operand2);
            case "/":
                if (operand2.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalArgumentException("Division durch Null");
                } else {
                    return operand1.divide(operand2, mc);
                }
            default:
                throw new IllegalArgumentException("Unbekannter Operator: " + operator);
        }
    }
    public static String calculate(final String calc) {
        try {
            final String expression = convertScientificToDecimal(
                    calc.replace('×', '*').replace('÷', '/').replace("=", "").replace(".", "").replace(",", ".").replace(" ", ""));
            final List<String> tokens = tokenize(expression);
            final BigDecimal result = evaluate(tokens);
            if (result.toString().replace(".", "").replace(",", "").length() >= 12) {
                return String.format(Locale.GERMANY ,"%.6e", result);
            } else {
                return result.toPlainString().replace('.', ',');
            }
        } catch (final ArithmeticException e) {
            return e.getMessage();
        }
    }
    public static String convertScientificToDecimal(final String str) {
        final Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?e[+-]\\d+");
        final Matcher matcher = pattern.matcher(str);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final BigDecimal bd = new BigDecimal(matcher.group().replace(",", "."));
            matcher.appendReplacement(sb, bd.toPlainString().replace(".", ","));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    public static BigDecimal evaluate(final List<String> tokens) {
        final List<String> postfixTokens = infixToPostfix(tokens);
        return evaluatePostfix(postfixTokens);
    }
    public static BigDecimal evaluatePostfix(final List<String> postfixTokens) {
        final List<BigDecimal> stack = new ArrayList<>();
        final MathContext mc = new MathContext(6, RoundingMode.HALF_UP); // Runden auf 5 Dezimalstellen
        for (final String token : postfixTokens) {
            if (isNumber(token)) {
                stack.add(new BigDecimal(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Nicht genügend Operanden für den Operator: " + token);
                }
                final BigDecimal operand2 = stack.remove(stack.size() - 1);
                final BigDecimal operand1 = stack.remove(stack.size() - 1);
                final BigDecimal result = applyOperator(operand1, operand2, token, mc);
                stack.add(result);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Ungültiger Ausdruck");
        }
        return stack.get(0);
    }
    public static List<String> infixToPostfix(final List<String> infixTokens) {
        final List<String> postfixTokens = new ArrayList<>();
        final List<String> operatorStack = new ArrayList<>();
        for (final String token : infixTokens) {
            if (isNumber(token)) {
                postfixTokens.add(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty() && precedence(operatorStack.get(operatorStack.size() - 1)) >= precedence(token)) {
                    postfixTokens.add(operatorStack.remove(operatorStack.size() - 1));
                }
                operatorStack.add(token);
            }
        }
        while (!operatorStack.isEmpty()) {
            postfixTokens.add(operatorStack.remove(operatorStack.size() - 1));
        }
        return postfixTokens;
    }
    public static boolean isNumber(final String token) {
        try {
            new BigDecimal(token);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }
    public static boolean isOperator(final String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }
    public static int precedence(final String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                throw new IllegalArgumentException("Unbekannter Operator: " + operator);
        }
    }
    public static List<String> tokenize(final String expression) {
        final List<String> tokens = new ArrayList<>();
        final StringBuilder currentToken = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                currentToken.append(c);
            } else if (c == '+' || c == '*' || c == '/' || c == '-') {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(Character.toString(c));
            } else if (c == ' ') {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            }
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        return tokens;
    }
}
