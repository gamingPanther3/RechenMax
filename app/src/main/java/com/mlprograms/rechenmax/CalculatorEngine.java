package com.mlprograms.rechenmax;

/*
 * Copyright (c) 2024 by Max Lemberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static com.mlprograms.rechenmax.NumberHelper.PI;
import static com.mlprograms.rechenmax.NumberHelper.e;
import static com.mlprograms.rechenmax.ParenthesesBalancer.balanceParentheses;
import static java.lang.Math.sqrt;
import static ch.obermuhlner.math.big.DefaultBigDecimalMath.pow;

import android.annotation.SuppressLint;
import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.obermuhlner.math.big.BigDecimalMath;

/**
 * CalculatorActivity
 *
 * @author Max Lemberg
 * @version 2.4.1
 * @date 26.05.2024
 */

public class CalculatorEngine {

    // Declaration of a static variable of type MainActivity. This variable is used to access the methods and variables of the MainActivity class.
    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;

    // Method to set the MainActivity. This method is used to initialize the static variable mainActivity.
    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    private static MathContext MC;
    public static int RESULT_LENGTH;
    public static String CALCULATION_MODE;

    // Declaration of a constant for the root operation.
    public static final String  ROOT            = "√";
    public static final String  THIRD_ROOT      = "³√";

    private static final DataManager dataManager = new DataManager();

    private static List<String> postfixTokens;

    /**
     * A mapping of subscript digits to their corresponding standard numerical digits.
     * This map is used to replace subscript characters (e.g., '₁', '₂') with their equivalent
     * regular digits (e.g., '1', '2') in a string.
     */
    private static final Map<Character, Character> SUBSCRIPT_MAP = new HashMap<>();
    static {
        SUBSCRIPT_MAP.put('₀', '0');
        SUBSCRIPT_MAP.put('₁', '1');
        SUBSCRIPT_MAP.put('₂', '2');
        SUBSCRIPT_MAP.put('₃', '3');
        SUBSCRIPT_MAP.put('₄', '4');
        SUBSCRIPT_MAP.put('₅', '5');
        SUBSCRIPT_MAP.put('₆', '6');
        SUBSCRIPT_MAP.put('₇', '7');
        SUBSCRIPT_MAP.put('₈', '8');
        SUBSCRIPT_MAP.put('₉', '9');
    }

    /**
     * This method calculates the result of a mathematical expression. The expression is passed as a string parameter.
     * <p>
     * It first replaces all the special characters in the expression with their corresponding mathematical symbols.
     * <p>
     * If the expression is in scientific notation, it converts it to decimal notation.
     * <p>
     * It then tokenizes the expression and evaluates it.
     * <p>
     * If the result is too large, it returns "Wert zu groß" (Value too large).
     * If the result is in scientific notation, it formats it to decimal notation.
     * <p>
     * It handles various exceptions such as ArithmeticException, IllegalArgumentException, and other exceptions.
     *
     * @param calc The mathematical expression as a string to be calculated.
     * @return The result of the calculation as a string.
     * @throws ArithmeticException      If there is an arithmetic error in the calculation.
     * @throws IllegalArgumentException If there is an illegal argument in the calculation.
     */
    public static String calculate(String calc) {
        try {
            RESULT_LENGTH = Integer.parseInt(dataManager.getJSONSettingsData("maxNumbersWithoutScrolling", mainActivity.getApplicationContext()).getString("value"));
            MC = new MathContext(RESULT_LENGTH, RoundingMode.HALF_UP);
            CALCULATION_MODE = dataManager.getJSONSettingsData("functionMode", mainActivity.getApplicationContext()).getString("value");

            String trim;
            if (String.valueOf(calc.charAt(0)).equals("+")) {
                calc = calc.substring(1);
            } else if (String.valueOf(calc.charAt(0)).equals("-")) {
                calc = "0" + calc;
            }

            // Replace all the special characters in the expression with their corresponding mathematical symbols
            // important: "е" (German: 'Eulersche-Zahl') and "e" (used for notation) are different characters

            calc = fixExpression(calc);
            String commonReplacements =
                    calc.replace('×', '*')
                        .replace('÷', '/')
                        .replace("=", "")
                        .replace("E", "e")
                        .replace("π", PI)
                        .replaceAll("е", e)
                        .replaceAll(" ", "")
                        .replace("½", "0,5")
                        .replace("⅓", "0,33333333333")
                        .replace("¼", "0,25")
                        .replace("⅕", "0,2")
                        .replace("⅒", "0,1")
                        .replace("%×", "⁒")
                        .replace("%*", "⁒");

            trim = commonReplacements.replace(".", "").replace(",", ".").trim();
            trim = balanceParentheses(trim);

            //Log.e("TRIM", "Trim:" + trim);

            // If the expression is in scientific notation, convert it to decimal notation
            if (isScientificNotation(trim)) {
                DataManager dataManager = new DataManager(mainActivity);
                dataManager.saveToJSONSettings("isNotation", true, mainActivity.getApplicationContext());

                String result = convertScientificToDecimal(trim);
                return removeNonNumeric(result);
            }

            final List<String> tokens = tokenize(trim);

            for (int i = 0; i < tokens.size() - 1; i++) {
                try {
                    if (tokens.get(i).equals("/") && tokens.get(i + 1).equals("-")) {
                        // Handle negative exponent in division
                        tokens.remove(i + 1);
                        tokens.add(i + 1, "NEG_EXPONENT");
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }

            // Evaluate the expression and handle exceptions
            final BigDecimal[] result = evaluate(tokens);

            if (!postfixTokens.isEmpty() && (postfixTokens.get(postfixTokens.size() - 1).startsWith("Pol(") || postfixTokens.get(postfixTokens.size() - 1).startsWith("Rec("))) {
                if (postfixTokens.get(postfixTokens.size() - 1).startsWith("Pol(")) {
                    return  " r="  + mainActivity.formatResultText(result[1].setScale(10, RoundingMode.HALF_UP).toString().replace(".", ",")) +
                            "; θ=" + mainActivity.formatResultText(result[0].setScale(10, RoundingMode.HALF_UP).toString().replace(".", ","));
                } else {
                    return  " x="  + mainActivity.formatResultText(result[1].setScale(10, RoundingMode.HALF_UP).toString().replace(".", ",")) +
                            "; y=" + mainActivity.formatResultText(result[0].setScale(10, RoundingMode.HALF_UP).toString().replace(".", ","));
                }
            } else {
                double resultDouble = result[0].doubleValue();
                // If the result is too large, return "Wert zu groß"
                if (Double.isInfinite(resultDouble)) {
                    return mainActivity.getString(R.string.errorMessage1);
                }

                // return the result in decimal notation
                String finalResult = result[0].stripTrailingZeros().toPlainString().replace('.', ',');
                if(containsOperatorOrFunction(trim)) {
                    return mainActivity.formatResultText(shortedResult(finalResult));
                } else {
                    return mainActivity.formatResultText(finalResult);
                }
            }
        } catch (ArithmeticException e) {
            // Handle exceptions related to arithmetic errors
            if (Objects.equals(e.getMessage(), mainActivity.getString(R.string.errorMessage1))) {
                return mainActivity.getString(R.string.errorMessage1);
            } else {
                return e.getMessage();
            }
        } catch (IllegalArgumentException e) {
            // Handle exceptions related to illegal arguments
            return e.getMessage();
        } catch (Exception e) {
            //Log.i("Exception", e.toString());
            return mainActivity.getString(R.string.errorMessage2);
        }
    }

    /**
     * Tokenizes a mathematical expression, breaking it into individual components such as numbers, operators, and functions.
     *
     * @param expression The input mathematical expression to be tokenized.
     * @return A list of tokens extracted from the expression.
     */
    public static List<String> tokenize(final String expression) {
        // Debugging: Print input expression
        //Log.i("tokenize","Input Expression: " + expression);

        // Remove all spaces from the expression
        String expressionWithoutSpaces = expression.replaceAll("\\s+", "");

        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < expressionWithoutSpaces.length(); i++) {
            char c = expressionWithoutSpaces.charAt(i);

            // If the character is a digit, period, or minus sign (if it's at the beginning, after an opening parenthesis, or after an operator),
            // add it to the current token
            if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || expressionWithoutSpaces.charAt(i - 1) == '('
                    || isOperator(String.valueOf(expressionWithoutSpaces.charAt(i - 1)))
                    || expressionWithoutSpaces.charAt(i - 1) == ','))) {
                currentToken.append(c);
            } else if (i + 3 < expressionWithoutSpaces.length() && expressionWithoutSpaces.startsWith("³√", i)) {
                // If "³√(" is found, handle the cubic root operation
                tokens.add(expressionWithoutSpaces.substring(i, i + 2));
                i += 1;
            } else {
                // If the character is an operator or a parenthesis, add the current token to the list and reset the current token
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                if (i + 3 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 3);
                    if (function.equals("ln(") || (function.startsWith("log") && function.endsWith("("))) {
                        tokens.add(function); // Add the full function name
                        i += 2; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 4 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 4);
                    if (function.equals("sin(") || function.equals("cos(") || function.equals("tan(")
                            || function.equals("Pol(") || function.equals("Rec(") || function.equals("Ran#") || (function.startsWith("log") && function.endsWith("("))) {
                        tokens.add(function); // Add the full function name
                        i += 3; // Skip the next characters (already processed)
                        continue;
                    }
                    if (function.equals("log(")) {
                        tokens.add(function); // Add the full function name
                        i += 3; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 5 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 5);
                    if (function.equals("sinh(") || function.equals("cosh(") || function.equals("tanh(") || (function.startsWith("log") && function.endsWith("("))) {
                        tokens.add(function); // Add the full function name
                        i += 4; // Skip the next characters (already processed)
                        continue;
                    }
                    if (function.equals("log₂(") || function.equals("log₃(") || function.equals("log₄(") ||
                            function.equals("log₅(") || function.equals("log₆(") || function.equals("log₇(") ||
                            function.equals("log₈(") || function.equals("log₉(") || (function.startsWith("log") && function.endsWith("("))) {
                        tokens.add(function); // Add the full function name
                        i += 4; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 6 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 6);
                    if (function.equals("sin⁻¹(") || function.equals("cos⁻¹(") || function.equals("tan⁻¹(") || (function.startsWith("log") && function.endsWith("("))) {
                        tokens.add(function); // Add the full function name
                        i += 5; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 7 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 7);
                    if (function.equals("sinh⁻¹(") || function.equals("cosh⁻¹(") || function.equals("tanh⁻¹(") ||
                            (function.startsWith("log") && function.endsWith("(")) || function.equals("RanInt(")) {
                        tokens.add(function); // Add the full function name
                        i += 6; // Skip the next characters (already processed)
                        continue;
                    }
                }

                tokens.add(Character.toString(c));
            }
        }

        // Add the last token if it exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        // Debugging: Print tokens
        //Log.i("tokenize","Tokens: " + tokens);

        return tokens;
    }

    /**
     * Evaluates a mathematical expression represented as a list of tokens.
     * Converts the expression from infix notation to postfix notation, then evaluates the postfix expression.
     *
     * @param tokens The mathematical expression in infix notation.
     * @return The result of the expression.
     */
    public static BigDecimal[] evaluate(final List<String> tokens) {
        // Convert the infix expression to postfix
        postfixTokens = infixToPostfix(tokens);
        //Log.i("evaluate", "Postfix Tokens: " + postfixTokens);

        // Evaluate the postfix expression and return the result
        return evaluatePostfix(postfixTokens);
    }

    /**
     * Applies an operator to two operands. Supports addition, subtraction, multiplication, division, square root, factorial, and power operations ... .
     * Checks the operator and performs the corresponding operation.
     *
     * @param operand1 The first operand for the operation.
     * @param operand2 The second operand for the operation.
     * @param operator The operator for the operation.
     * @return The result of the operation.
     * @throws IllegalArgumentException If the operator is not recognized or if the second operand for the square root operation is negative.
     */
    public static BigDecimal applyOperator(final BigDecimal operand1, final BigDecimal operand2, final String operator) {

        //System.out.println("Operator: " + operator);
        //System.out.println("Operand1: " + operand1);
        //System.out.println("Operand2: " + operand2);
        //System.out.println("Result: " + operand1.divide(operand2, new MathContext(1000)));

        switch (operator) {
            case "+":
                // Add the two BigDecimals
                return operand1.add(operand2);
            case "-":
                return operand1.subtract(operand2);
            case "*":
                return operand1.multiply(operand2);
            case "/":
                if (operand2.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException(mainActivity.getString(R.string.errorMessage3));
                } else {
                    return operand1.divide(operand2, new MathContext(1000));
                }
            case ROOT:
                if (operand2.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage4));
                } else {
                    return squareRoot(operand2);
                }
            case THIRD_ROOT:
                return thirdRoot(operand2);
            case "!":
                return factorial(operand1);
            case "^":
                return power(operand1, operand2);
            case "С":
                return binomialCoefficientBigDecimal(operand1, operand2);
            case "Ƥ":
                return permutationBigDecimal(operand1, operand2);
            case "⁒":
                return nPercentFromM(operand1, operand2);
            default:
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage5));
        }
    }

    /**
     * Evaluates a mathematical expression represented in postfix notation.
     *
     * @param postfixTokens The list of tokens in postfix notation.
     * @return The result of the expression.
     * @throws IllegalArgumentException If there is a syntax error in the expression or the stack size is not 1 at the end.
     */
    public static BigDecimal[] evaluatePostfix(final List<String> postfixTokens) {
        // Create a stack to store numbers
        final List<BigDecimal> stack = new ArrayList<>();

        // Iterate through each token in the postfix list
        for (final String token : postfixTokens) {
            // Debugging: Print current token
            //Log.i("evaluatePostfix","Token: " + token);

            // If the token is a number, add it to the stack
            if (isNumber(token)) {
                stack.add(new BigDecimal(token));
            } else if (isOperator(token)) {
                // If the token is an operator, apply the operator to the numbers in the stack
                applyOperatorToStack(token, stack);
            } else if (isFunction(token)) {
                // If the token is a function, evaluate the function and add the result to the stack
                evaluateFunction(token, stack);
            } else {
                // If the token is neither a number, operator, nor function, throw an exception
                //Log.i("evaluatePostfix","Token is neither a number nor an operator");
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
            }

            // Debugging: Print current stack
            //Log.i("evaluatePostfix","Stack: " + stack);
        }

        // Check if the function result matches the expected number of values on the stack
        if (!postfixTokens.isEmpty() && (postfixTokens.get(postfixTokens.size() - 1).startsWith("Pol(") || postfixTokens.get(postfixTokens.size() - 1).startsWith("Rec("))) {
            // For functions like "Pol" or "Rec", expect two values on the stack
            if (stack.size() != 2) {
                //Log.i("evaluatePostfix", "Incorrect number of values on the stack");
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
            }

            BigDecimal[] result = {stack.get(0), stack.get(1)};
            return result;
        } else {
            // For other functions, expect only one value on the stack
            if (stack.size() != 1) {
                //Log.i("evaluatePostfix", "Stacksize != 1");
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
            }

            BigDecimal[] result = {stack.get(0)};
            return result;
        }
    }

    /**
     * Applies an operator to numbers in the stack based on the given operator.
     *
     * @param operator The operator to be applied.
     * @param stack    The stack containing numbers.
     */
    private static void applyOperatorToStack(String operator, List<BigDecimal> stack) {
        // If the operator is "!", apply the operator to only one number
        if (operator.equals("!")) {
            final BigDecimal operand1 = stack.remove(stack.size() - 1);
            final BigDecimal result = applyOperator(operand1, BigDecimal.ZERO, operator);
            stack.add(result);
        }
        // If the operator is not "!", apply the operator to two numbers
        else {
            final BigDecimal operand2 = stack.remove(stack.size() - 1);
            // If the operator is not ROOT and THIRDROOT, apply the operator to two numbers
            if (!operator.equals(ROOT) && !operator.startsWith(THIRD_ROOT)) {
                final BigDecimal operand1 = stack.remove(stack.size() - 1);
                final BigDecimal result = applyOperator(operand1, operand2, operator);
                stack.add(result);
            }
            // If the operator is ROOT, apply the operator to only one number
            else {
                BigDecimal result;
                switch (operator) {
                    case ROOT:
                        if (operand2.compareTo(BigDecimal.ZERO) < 0) {
                            // If the operand is negative, throw an exception or handle it as needed
                            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage4));
                        } else {
                            result = squareRoot(operand2);
                        }
                        break;
                    case THIRD_ROOT:
                        result = thirdRoot(operand2);
                        break;
                    default:
                        // Handle other operators if needed
                        throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
                }
                stack.add(result);
            }
        }
    }

    /**
     * Evaluates a mathematical function and adds the result to the stack.
     *
     * @param function The function to be evaluated.
     * @param stack    The stack containing numbers.
     */
    private static void evaluateFunction(String function, List<BigDecimal> stack) {
        Map<String, Function<BigDecimal, BigDecimal>> functionsMap = new HashMap<>();
        functionsMap.put("ln(", CalculatorEngine::ln);
        functionsMap.put("sin(", CalculatorEngine::sin);
        functionsMap.put("sinh(", CalculatorEngine::sinh);
        functionsMap.put("sin⁻¹(", CalculatorEngine::asin);
        functionsMap.put("sinh⁻¹(", CalculatorEngine::asinh);
        functionsMap.put("cos(", CalculatorEngine::cos);
        functionsMap.put("cosh(", CalculatorEngine::cosh);
        functionsMap.put("cos⁻¹(", CalculatorEngine::acos);
        functionsMap.put("cosh⁻¹(", CalculatorEngine::acosh);
        functionsMap.put("tan(", CalculatorEngine::tan);
        functionsMap.put("tanh(", CalculatorEngine::tanh);
        functionsMap.put("tan⁻¹(", CalculatorEngine::atan);
        functionsMap.put("tanh⁻¹(", CalculatorEngine::atanh);

        if (function.startsWith("log") && function.endsWith("(")) {
            int baseStartIndex = 3;
            int baseEndIndex = function.length() - 1;
            String baseString = function.substring(baseStartIndex, baseEndIndex);
            baseString = convertSubscripts(baseString);
            BigDecimal base = new BigDecimal(baseString);

            if("1".equals(base.toString())) {
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage16));
            }

            BigDecimal operand = stack.remove(stack.size() - 1);
            stack.add(logX(operand, base.doubleValue()));
        } else if (function.startsWith("Pol") && function.endsWith("(")) {
            // Cartesian to Polar conversion
            BigDecimal y = stack.remove(stack.size() - 1);
            BigDecimal x = stack.remove(stack.size() - 1);
            BigDecimal[] polar = cartesianToPolar(x, y);
            stack.add(polar[1]); // Push theta
            stack.add(polar[0]); // Push r
        } else if (function.startsWith("Rec") && function.endsWith("(")) {
            // Polar to Cartesian conversion
            BigDecimal theta = stack.remove(stack.size() - 1);
            BigDecimal r = stack.remove(stack.size() - 1);
            BigDecimal[] cartesian = polarToCartesian(r, theta);
            stack.add(cartesian[1]); // Push y
            stack.add(cartesian[0]); // Push x
        } else if (function.startsWith("RanInt") && function.endsWith("(")) {
            int x = Integer.parseInt(String.valueOf(stack.remove(stack.size() - 1)));
            int y = Integer.parseInt(String.valueOf(stack.remove(stack.size() - 1)));

            stack.add(new BigDecimal(randomInteger(x, y)));
        } else if (function.startsWith("Ran") && function.endsWith("#")) {
            BigDecimal n = stack.remove(stack.size() - 1);

            stack.add(new BigDecimal(String.valueOf(randomToNBigDecimal(n))));
        } else {
            Function<BigDecimal, BigDecimal> func = functionsMap.get(function);
            if (func != null) {
                BigDecimal operand = stack.remove(stack.size() - 1);
                stack.add(func.apply(operand));
            } else {
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage14));
            }
        }
    }

    /**
     * Converts a mathematical expression from infix notation to postfix notation.
     *
     * @param infixTokens The list of tokens in infix notation.
     * @return The list of tokens in postfix notation.
     */
    public static List<String> infixToPostfix(final List<String> infixTokens) {
        final List<String> postfixTokens = new ArrayList<>();
        final Stack<String> stack = new Stack<>();

        for (int i = 0; i < infixTokens.size(); i++) {
            final String token = infixTokens.get(i);
            // Debugging: Print current token and stack
            //Log.i("infixToPostfix", "Current Token: " + token);
            //Log.i("infixToPostfix", "Stack: " + stack);

            if (isNumber(token)) {
                postfixTokens.add(token);
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (isOperator(token) && token.equals("-")) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token) && !isFunction(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token) && !isFunction(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    postfixTokens.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) {
                    stack.pop(); // Remove the opening parenthesis
                    if (!stack.isEmpty() && isFunction(stack.peek())) {
                        postfixTokens.add(stack.pop());
                    }
                }
            }

            // Debugging: Print postfixTokens and stack after processing current token
            //Log.i("infixToPostfix", "Postfix Tokens: " + postfixTokens);
            //Log.i("infixToPostfix", "Stack after Token Processing: " + stack);
        }

        while (!stack.isEmpty()) {
            postfixTokens.add(stack.pop());
        }

        // Debugging: Print final postfixTokens
        //Log.i("infixToPostfix", "Final Postfix Tokens: " + postfixTokens);

        return postfixTokens;
    }

    /**
     * Determines the precedence of an operator.
     * Precedence rules determine the order in which expressions involving both unary and binary operators are evaluated.
     *
     * @param operator The operator to be checked.
     * @return The precedence of the operator.
     * @throws IllegalArgumentException If the operator is not recognized.
     */
    public static int precedence(final String operator) {
        // If the operator is an opening parenthesis, return 0
        switch (operator) {
            case "(":
                return 0;

            // If the operator is addition or subtraction, return 1
            case "+":
            case "-":
                return 1;

            // If the operator is multiplication or division, return 2
            case "*":
            case "/":
                return 2;

            // If the operator is exponentiation, return 3
            case "^":
                return 3;

            // If the operator is root, return 4
            case "√":
            case "³√":
                return 4;

            // If the operator is factorial, return 5
            case "!":
                return 5;

            // If the operator is sine, cosine, or tangent ..., return 6
            case "log(":
            case "log₂(":
            case "log₃(":
            case "log₄(":
            case "log₅(":
            case "log₆(":
            case "log₇(":
            case "log₈(":
            case "log₉(":
            case "ln(":
            case "sin(":
            case "cos(":
            case "tan(":
            case "sinh(":
            case "cosh(":
            case "tanh(":
            case "sinh⁻¹(":
            case "cosh⁻¹(":
            case "tanh⁻¹(":
            case "sin⁻¹(":
            case "cos⁻¹(":
            case "tan⁻¹(":
            case "Pol(":
            case "Rec(":
                return 6;
            case "Ran#":
            case "RanInt(":
                return 7;
            case "С":
            case "Ƥ":
            case "⁒":
                return 8;

            // If the operator is not recognized, throw an exception
            default:
                if(operator.startsWith("log") && operator.endsWith("(")) {
                    return 6;
                }
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
        }
    }

    /**
     * @param calculation Is the calculation as a String
     * @return Returns the calculation or the shorted calculation
     */
    private static String shortedResult(String calculation) {
        if(calculation.contains(",") && !mainActivity.isInvalidInput(calculation)) {
            StringBuilder shortedCalculation = new StringBuilder();

            String[] calculationParts = calculation.split(",");
            if(calculationParts.length == 2 && calculationParts[1].length() >= 2) {
                if(calculationParts[0].length() >= RESULT_LENGTH) {
                    shortedCalculation.append(calculationParts[0]).append(",");
                    shortedCalculation.append(calculationParts[1].substring(0, 2));
                } else {
                    shortedCalculation.append(calculationParts[0]).append(",");
                    int addableNumbers = RESULT_LENGTH - calculationParts[0].length();

                    if(addableNumbers > calculationParts[1].length()) {
                        shortedCalculation.append(calculationParts[1]);
                    } else {
                        shortedCalculation.append(calculationParts[1].substring(0, addableNumbers));
                    }
                }
                return shortedCalculation.toString();
            }
        }
        return removeUnnecessaryZeros(calculation);
    }

    /**
     * Fixes mathematical expressions by inserting implicit multiplication symbols (×) where appropriate.
     * This method analyzes an input string representing a mathematical expression and identifies situations
     * where multiplication is implied but not explicitly written. It then inserts the multiplication symbol ('×')
     * in those locations to clarify the expression.
     * Additionally, the method corrects the specific case where "-+" appears in the expression, replacing it with just "-".
     * Examples:
     *   - "2(3+4)" becomes "2×(3+4)"
     *   - "5π" becomes "5×π"
     *   - "3-2+5" remains unchanged
     *
     * @param input The mathematical expression string to be fixed.
     * @return The fixed expression with explicit multiplication symbols and corrected minus sign.
     */
    public static String fixExpression(String input) {
        //Log.i("fixExpression", "Input fixExpression: " + input);

        // Step 1: Fix the expression using the original logic
        StringBuilder stringBuilder = new StringBuilder();
        if (input.length() >= 2) {
            for (int i = 0; i < input.length(); i++) {
                String currentChar = String.valueOf(input.charAt(i));
                String nextChar = "";

                if (i + 1 < input.length()) {
                    nextChar = String.valueOf(input.charAt(i + 1));
                }

                stringBuilder.append(currentChar);
                ////Log.e("fixExpression", "CurrentChar: " + currentChar + " NextChar: " + nextChar);
                ////Log.e("fixExpression", "stringBuilder: " + stringBuilder);

                if (!nextChar.isEmpty() &&
                        ((Character.isDigit(input.charAt(i)) || isSymbol(currentChar)) && nextChar.equals("(")) ||
                        (Character.isDigit(input.charAt(i)) && isSymbol(nextChar)) ||
                        (isSymbol(currentChar) && i + 1 < input.length() && Character.isDigit(input.charAt(i + 1))) ||
                        (currentChar.equals(")") && (i + 1 < input.length() && (Character.isDigit(input.charAt(i + 1)) || isSymbol(nextChar)))) ||
                        (isSymbol(currentChar) && isSymbol(nextChar))) {
                    stringBuilder.append('×');
                }
            }
        }

        // Step 2: Handle the specific case of "-+"
        String fixedExpression = stringBuilder.toString();
        fixedExpression = fixedExpression.replaceAll("-\\+", "-");

        //Log.e("fixExpression", "Fixed Expression: " + addBracketsAroundBinomialCoefficientAndPermutation(fixedExpression));
        return addBracketsAroundBinomialCoefficientAndPermutation(stringBuilder.toString().isEmpty() ? input : fixedExpression);
    }

    /**
     * Adds brackets around binomial coefficient and permutation expressions (nCk, nPk) found in a string,
     * if they are not already enclosed in brackets.
     * <p>
     * This method takes a string that may contain binomial coefficient or permutation expressions in the format "nCk" or "nPk"
     * (e.g., "5C2" or "5P2"). It identifies these expressions using a regular expression and encloses them in parentheses if
     * they are not already enclosed in parentheses.
     * <p>
     * Example:
     *   - Input:  "The answer is 5C2"
     *   - Output: "The answer is (5C2)"
     *
     * @param input The string to be processed.
     * @return The string with parentheses added around binomial coefficient or permutation expressions.
     */
    public static String addBracketsAroundBinomialCoefficientAndPermutation(String input) {
        Pattern pattern = Pattern.compile("(\\d+[СƤ⁒]\\d+)");
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();

        int lastIndex = 0;
        while (matcher.find()) {
            String match = matcher.group(1);
            // Check if the match is already enclosed in parentheses
            if(!(matcher.start() > 0 && input.charAt(matcher.start() - 1) == '(' && matcher.end() < input.length() && input.charAt(matcher.end()) == ')')) {
                result.append(input, lastIndex, matcher.start());
                result.append("(").append(match).append(")");
            } else {
                result.append(input, lastIndex, matcher.end());
            }
            lastIndex = matcher.end();
        }
        result.append(input.substring(lastIndex));

        return result.toString();
    }

    /**
     * Removes trailing zeros from the decimal portion of a number represented as a string.
     * This method takes a string that may represent a decimal number (using a comma as the decimal separator).
     * If the string contains a comma, it removes any trailing zeros after the comma. If all digits after the comma are zeros,
     * the comma itself is also removed.
     *
     * @param result The string representing a number potentially with trailing zeros in the decimal portion.
     * @return The string with unnecessary trailing zeros removed, or the original string if no comma is found.
     */
    private static String removeUnnecessaryZeros(String result) {
        StringBuilder newResult = new StringBuilder();
        StringBuilder tempPart = new StringBuilder();

        if (result.contains(",")) {
            String[] parts = result.split(",");
            newResult.append(parts[0]).append(",");
            tempPart.append(parts[1]);

            for(int x = parts[1].length() - 1; x >= 0; x--) {
                if(String.valueOf(parts[1].charAt(x)).equals("0")) {
                    tempPart.deleteCharAt(x);
                } else {
                    break;
                }
            }

            newResult.append(tempPart);
            return newResult.toString();
        }
        return result;
    }

    /**
     * convertScientificToDecimal method converts a number in scientific notation to decimal representation.
     *
     * @param str The input string in scientific notation.
     * @return The decimal representation of the input string.
     */
    public static String convertScientificToDecimal(final String str) {
        // Define the pattern for scientific notation
        final Pattern pattern = Pattern.compile("([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)");
        final Matcher matcher = pattern.matcher(str);
        final StringBuffer sb = new StringBuffer();

        // Process all matches found in the input string
        while (matcher.find()) {
            // Extract number and exponent parts from the match
            final String numberPart = matcher.group(1);
            String exponentPart = matcher.group(3);

            // Remove the 'e' or 'E' from the exponent part
            if (exponentPart != null) {
                exponentPart = exponentPart.substring(1);
            }

            // Check and handle the case where the exponent is too large
            if (exponentPart != null) {
                final int exponent = Integer.parseInt(exponentPart);

                // Determine the sign of the number and create a BigDecimal object
                assert numberPart != null;
                final String sign = numberPart.startsWith("-") ? "-" : "";
                BigDecimal number = new BigDecimal(numberPart);

                // Negate the number if the input starts with a minus sign
                if (numberPart.startsWith("-")) {
                    number = number.negate();
                }

                // Scale the number by the power of ten specified by the exponent
                BigDecimal scaledNumber;
                if (exponent >= 0) {
                    scaledNumber = number.scaleByPowerOfTen(exponent);
                } else {
                    scaledNumber = number.divide(BigDecimal.TEN.pow(-exponent), MC);
                }

                // Remove trailing zeros and append the scaled number to the result buffer
                String result = sign + scaledNumber.stripTrailingZeros().toPlainString();
                if (result.startsWith(".")) {
                    result = "0" + result;
                }
                matcher.appendReplacement(sb, result);
            }
        }

        // Append the remaining part of the input string to the result buffer
        matcher.appendTail(sb);

        // Check if the result buffer contains two consecutive minus signs and remove one if necessary
        if (sb.indexOf("--") != -1) {
            sb.replace(sb.indexOf("--"), sb.indexOf("--") + 2, "-");
        }

        // Return the final result as a string
        //Log.i("convertScientificToDecimal", "sb:" + sb);
        return sb.toString();
    }

    /**
     * Converts subscript characters to normal characters.
     *
     * @param subscript The string containing subscript characters.
     * @return The string with normal characters.
     */
    private static String convertSubscripts(String subscript) {
        StringBuilder normalString = new StringBuilder();
        for (char ch : subscript.toCharArray()) {
            normalString.append(SUBSCRIPT_MAP.getOrDefault(ch, ch));
        }
        return normalString.toString();
    }

    /**
     * This method removes all non-numeric characters from a string, except for the decimal point and comma.
     * It uses a regular expression to match all characters that are not digits, decimal points, or commas, and replaces them with an empty string.
     *
     * @param str The string to be processed.
     * @return The processed string with all non-numeric characters removed.
     */
    public static String removeNonNumeric(final String str) {
        // Replace all non-numeric and non-decimal point characters in the string with an empty string
        return str.replaceAll("[^0-9.,\\-]", "");
    }

    /**
     * @param calculation The text to be checked.
     * @return true if the token represents a non-functional operator, false otherwise.
     */
    public static boolean containsOperatorOrFunction(final String calculation) {
        return calculation.contains("+") || calculation.contains("-") || calculation.contains("*") || calculation.contains("/") ||
                calculation.contains("×") || calculation.contains("÷") ||
                calculation.contains("^") || calculation.contains("√") || calculation.contains("!") || calculation.contains("³√") ||
                calculation.contains("log") || calculation.contains("ln") || calculation.contains("sin") || calculation.contains("cos") ||
                calculation.contains("tan") || calculation.contains("С") || calculation.contains("Ƥ") || calculation.contains("⁒") ||
                calculation.contains("RanInt(") || calculation.contains("Ran#");
    }

    /**
     * Checks if the given token represents a recognized trigonometric function.
     *
     * @param token The token to be checked.
     * @return true if the token represents a trigonometric function, false otherwise.
     */
    public static boolean isFunction(final String token) {
        // Check if the token is one of the recognized trigonometric functions
        return token.equals("sin(") || token.equals("cos(") || token.equals("tan(") ||
                token.equals("sinh(") || token.equals("cosh(") || token.equals("tanh(") ||
                token.equals("log(") || token.equals("log₂(") || token.equals("log₃(") ||
                token.equals("log₄(") || token.equals("log₅(") || token.equals("log₆(") ||
                token.equals("log₇(") || token.equals("log₈(") || token.equals("log₉(") ||
                token.equals("ln(") || token.equals("sin⁻¹(") || token.equals("cos⁻¹(") ||
                token.equals("tan⁻¹(") || token.equals("sinh⁻¹(") || token.equals("cosh⁻¹(") ||
                token.equals("tanh⁻¹(") || token.equals("Rec(")  || token.equals("Pol(") || token.equals("RanInt(") ||
                token.equals("Ran#")
                ||
                (token.startsWith("log") && token.endsWith("("));
    }

    /**
     * Checks if a given string represents a recognized mathematical symbol.
     * Recognized symbols include:
     *  - ¼ (One quarter)
     *  - ⅓ (One third)
     *  - ½ (One half)
     *  - e (Euler's number)
     *  - π (Pi)
     *
     * @param character The string to check.
     * @return true if the string is a recognized symbol, false otherwise.
     */
    public static boolean isSymbol(final String character) {
        return (String.valueOf(character).equals("¼") || String.valueOf(character).equals("⅓") || String.valueOf(character).equals("½") ||
                String.valueOf(character).equals("⅕") || String.valueOf(character).equals("⅒") ||
                String.valueOf(character).equals("е") || String.valueOf(character).equals("e") || String.valueOf(character).equals("π"));
    }

    /**
     * Checks if the given token represents a recognized non-functional operator.
     *
     * @param token The token to be checked.
     * @return true if the token represents a non-functional operator, false otherwise.
     */
    public static boolean isOperator(final String token) {
        // Check if the token is one of the recognized non-functional operators
        return token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/") ||
                token.contains("×") || token.contains("÷") ||
                token.contains("^") || token.contains("√") || token.contains("!") || token.contains("³√") || token.contains("С") ||
                token.contains("Ƥ") || token.contains("⁒");
    }

    /**
     * Checks if a given string token represents a standard mathematical operator.
     * Standard operators include:
     *   - Addition (+)
     *   - Subtraction (-)
     *   - Multiplication (*) or (×)
     *   - Division (/) or (÷)
     *
     * @param token The string token to check.
     * @return True if the token is a standard operator, false otherwise.
     */
    public static boolean isStandardOperator(final String token) {
        // Check if the token is one of the recognized non-functional operators
        return token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/")
                || token.contains("×") || token.contains("÷");
    }

    /**
     * isScientificNotation method checks if a given string is in scientific notation.
     *
     * @param str The input string to be checked.
     * @return True if the string is in scientific notation, otherwise false.
     */
    public static boolean isScientificNotation(final String str) {
        final String formattedInput = str.replace(",", ".");
        final Pattern pattern = Pattern.compile("^([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)$");
        final Matcher matcher = pattern.matcher(formattedInput);

        return matcher.matches();
    }

    /**
     * Checks if a token is a number.
     * It attempts to create a BigDecimal from the token. If successful, the token is considered a number; otherwise, it is not.
     *
     * @param token The token to be checked.
     * @return True if the token is a number, false otherwise.
     */
    public static boolean isNumber(final String token) {
        // Try to create a new BigDecimal from the token
        try {
            new BigDecimal(token);
            // If successful, the token is a number
            return true;
        }
        // If a NumberFormatException is thrown, the token is not a number
        catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a given angle in degrees is a multiple of 90.
     *
     * @param degrees The angle in degrees to be checked.
     * @return true if the angle is a multiple of 90, false otherwise.
     */
    private static boolean isMultipleOf90(double degrees) {
        // Check if degrees is a multiple of 90
        return Math.abs(degrees % 90) == 0;
    }

    /**
     * Generates a random integer within a specified range (inclusive).
     *
     * @param x The lower bound of the range (inclusive).
     * @param y The upper bound of the range (inclusive).
     * @return A random integer between x and y (both included).
     */
    public static int randomInteger(int x, int y) {
        if (y < x) {
            int temp = x;
            x = y;
            y = temp;
        }
        return new Random().nextInt(y - x + 1) + x;
    }

    /**
     * Generates a random BigDecimal value between 0 (exclusive) and the specified `n` value (inclusive), with appropriate scaling.
     *
     * @param n The upper limit (inclusive) of the random value to be generated. Must be positive.
     * @return A random BigDecimal within the range [0, n], rounded to a scale that matches the precision of `n`.
     * @throws IllegalArgumentException If `n` is less than or equal to zero.
     */
    public static BigDecimal randomToNBigDecimal(BigDecimal n) {
        if (n.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage20));
        }

        BigDecimal randomBigDecimal = new BigDecimal(new Random().nextDouble(), MathContext.DECIMAL64);
        BigDecimal result = randomBigDecimal.multiply(n);

        int scale = calculateScale(n);
        return result.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Determines the appropriate scale (number of decimal places) for a BigDecimal value, based on its magnitude.
     * <p>
     * This method aims to balance precision and readability by adjusting the number of decimal places dynamically.
     * Smaller values receive more decimal places, while larger values are rounded to fewer decimal places.
     *
     * @param n The BigDecimal value for which to determine the scale.
     * @return The appropriate scale (number of decimal places) for the given value.
     */
    private static int calculateScale(BigDecimal n) {
        if (n.compareTo(BigDecimal.ONE) < 0) {
            return 5; // For n < 1, use 5 decimal places
        } else if (n.compareTo(BigDecimal.TEN) < 0) {
            return 4; // For 1 <= n < 10, use 4 decimal places
        } else if (n.compareTo(new BigDecimal("100")) < 0) {
            return 2; // For 10 <= n < 100, use 2 decimal places
        } else if (n.compareTo(new BigDecimal("1000")) < 0) {
            return 1; // For 100 <= n < 1000, use 1 decimal place
        } else {
            return 0; // For n >= 1000, use no decimal places
        }
    }

    /**
     * Calculates the result of raising a BigDecimal base to a BigDecimal exponent.
     * This method efficiently handles various exponent cases:
     *   - Exponent is zero: Returns BigDecimal.ONE (1)
     *   - Exponent is one: Returns the base
     *   - Exponent is negative: Inverts the result of raising the base to the positive exponent
     *   - Exponent has a fractional part: Splits the calculation into integer and fractional parts for efficiency
     *   - Exponent is a positive integer: Uses a recursive approach for efficient calculation
     *
     * @param base     The base of the exponentiation.
     * @param exponent The exponent to raise the base to.
     * @return The result of base raised to the power of exponent.
     * @throws ArithmeticException If the base is zero and the exponent is negative (division by zero).
     */
    public static BigDecimal power(BigDecimal base, BigDecimal exponent) {
        if (exponent.equals(BigDecimal.ZERO)) {
            return BigDecimal.ONE;
        } else if (exponent.equals(BigDecimal.ONE)) {
            return base;
        } else if (exponent.signum() == -1) {
            return BigDecimal.ONE.divide(pow(base, exponent.negate()), MC);
        } else {
            // Handle the case when the exponent has a fractional part
            if (exponent.scale() > 0) {
                BigDecimal integerPart = exponent.setScale(0, RoundingMode.FLOOR);
                BigDecimal fractionalPart = exponent.subtract(integerPart);
                return pow(base, integerPart).multiply(pow(base, fractionalPart));
            } else {
                return base.multiply(pow(base, exponent.subtract(BigDecimal.ONE)));
            }
        }
    }

    /**
     * Calculates the factorial of a BigDecimal number.
     * The factorial of a non-negative integer n, denoted by n!, is the product of all positive integers
     * less than or equal to n. For example, 5! = 5 * 4 * 3 * 2 * 1 = 120.
     * This method handles the following cases:
     *   - Negative input: Calculates the factorial of the absolute value and negates the result.
     *   - Non-integer input: Throws an IllegalArgumentException with an error message.
     *   - Input greater than 170: Throws an IllegalArgumentException due to potential overflow.
     *
     * @param number The BigDecimal number for which to calculate the factorial.
     * @return The factorial of the number as a BigDecimal.
     * @throws IllegalArgumentException If the input number is negative, not an integer, or greater than 170.
     */
    public static BigDecimal factorial(BigDecimal number) {
        // Check if the number is greater than 170
        if (number.compareTo(new BigDecimal("170")) > 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage1));
        }

        // Check if the number is negative
        boolean isNegative = number.compareTo(BigDecimal.ZERO) < 0;
        // If the number is negative, convert it to positive
        if (isNegative) {
            number = number.negate();
        }

        // Check if the number is an integer. If not, throw an exception
        if (number.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage6));
        }

        // Initialize the result as 1
        BigDecimal result = BigDecimal.ONE;

        // Calculate the factorial of the number
        while (number.compareTo(BigDecimal.ONE) > 0) {
            result = result.multiply(number);
            number = number.subtract(BigDecimal.ONE);
        }

        // If the original number was negative, return the negative of the result. Otherwise, return the result.
        return isNegative ? result.negate() : result;
    }

    /**
     * Custom method for calculating the square root with higher precision.
     *
     * @param x The BigDecimal value for which the square root is to be calculated.
     * @return The square root of the input value with higher precision.
     */
    public static BigDecimal squareRoot(BigDecimal x) {
        // Initial guess for the square root
        BigDecimal initialGuess = x.divide(BigDecimal.valueOf(2), MathContext.DECIMAL128);
        BigDecimal previousGuess = BigDecimal.ZERO;
        BigDecimal currentGuess = new BigDecimal(initialGuess.toString());

        // Iterative improvement using Newton's method
        while (!previousGuess.equals(currentGuess)) {
            previousGuess = new BigDecimal(currentGuess.toString());
            BigDecimal f = currentGuess.pow(2).subtract(x, MathContext.DECIMAL128);
            BigDecimal fPrime = BigDecimal.valueOf(2).multiply(currentGuess, MathContext.DECIMAL128);
            currentGuess = currentGuess.subtract(f.divide(fPrime, MathContext.DECIMAL128), MathContext.DECIMAL128);
        }

        return currentGuess;
    }

    /**
     * Custom method for calculating the cube root with higher precision.
     *
     * @param x The BigDecimal value for which the cube root is to be calculated.
     * @return The cube root of the input value with higher precision.
     */
    public static BigDecimal thirdRoot(BigDecimal x) {
        BigDecimal initialApproximation  = x.divide(BigDecimal.valueOf(3), MathContext.DECIMAL128);
        BigDecimal previousApproximation  = BigDecimal.ZERO;
        BigDecimal currentApproximation  = new BigDecimal(initialApproximation .toString());

        while (!previousApproximation .equals(currentApproximation )) {
            previousApproximation  = new BigDecimal(currentApproximation .toString());
            BigDecimal f = currentApproximation .pow(3).subtract(x, MathContext.DECIMAL128);
            BigDecimal fPrime = BigDecimal.valueOf(3).multiply(currentApproximation .pow(2), MathContext.DECIMAL128);
            currentApproximation  = currentApproximation .subtract(f.divide(fPrime, MathContext.DECIMAL128), MathContext.DECIMAL128);
        }

        return currentApproximation ;
    }

    /**
     * Calculates n% of the value m.
     *
     * @param n The percentage to calculate (e.g., 25 for 25%).
     * @param m The base value from which to calculate the percentage.
     * @return The result of n% of m as a BigDecimal.
     */
    public static BigDecimal nPercentFromM(BigDecimal n, BigDecimal m) {
        BigDecimal percent = n.divide(new BigDecimal("100"));
        return m.multiply(percent);
    }

    /**
     * Calculates the number of permutations (k-permutations) of n items taken k at a time.
     * <p>
     * A permutation is an arrangement of objects in a specific order. The number of k-permutations of n items,
     * denoted as nPk or P(n, k), is the number of ways to select and order k items from a set of n items.
     * <p>
     * Mathematically, nPk is calculated as:
     *   nPk = n! / (n-k)!
     *
     * @param n The total number of items (n >= 0).
     * @param k The number of items to choose and order (0 <= k <= n).
     * @return The number of k-permutations of n items.
     * @throws IllegalArgumentException If k is greater than n.
     */
    public static BigDecimal permutationBigDecimal(BigDecimal n, BigDecimal k) {
        int nInt = n.intValue();
        int kInt = k.intValue();

        if (kInt > nInt) {
            throw new IllegalArgumentException("k darf nicht größer als n sein");
        }
        BigDecimal nFactorial = factorial(n);
        BigDecimal nMinusKFactorial = factorial(n.subtract(k));
        return nFactorial.divide(nMinusKFactorial, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * @param n The total number of items (n >= 0).
     * @param k The number of items to choose and order (0 <= k <= n).
     * @return The number of k-permutations of n items.
     * @throws IllegalArgumentException If k is greater than n.
     */
    public static BigDecimal permutationInt(int n, int k) {
        if (k > n) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage18));
        }
        BigDecimal nFactorial = factorial(BigDecimal.valueOf(n));
        BigDecimal nMinusKFactorial = factorial(BigDecimal.valueOf(n - k));
        return nFactorial.divide(nMinusKFactorial);
    }

    /**
     * Calculates the binomial coefficient, also known as "n choose k".
     * <p>
     * The binomial coefficient is a mathematical expression that represents the number of ways to choose
     * k items from a set of n items without regard to order. It's denoted as nCk or (n k) and is calculated as:
     * <p>
     *    nCk = n! / (k! * (n-k)!)
     * <p>
     * This method uses an optimized approach to avoid calculating factorials directly, which can lead to overflow
     * for large values of n.
     *
     * @param n1 The total number of items (n >= 0).
     * @param k1 The number of items to choose (0 <= k <= n).
     * @return The binomial coefficient nCk.
     * @throws IllegalArgumentException If k is greater than n (invalid input).
     */
    public static BigDecimal binomialCoefficientBigDecimal(BigDecimal n1, BigDecimal k1) {
        int n = n1.intValue();
        int k = k1.intValue();

        if (k > n) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage18));
        }
        if (k == 0 || k == n) {
            return new BigDecimal(1);
        }

        k = Math.min(k, n - k);
        long c = 1;
        for (int i = 0; i < k; i++) {
            c = c * (n - i) / (i + 1);
        }

        return BigDecimal.valueOf(c);
    }

    /**
     * @param n The total number of items (n >= 0).
     * @param k The number of items to choose (0 <= k <= n).
     * @return The binomial coefficient nCk.
     * @throws IllegalArgumentException If k is greater than n (invalid input).
     */
    public static BigDecimal binomialCoefficientInt(int n, int k) {
        if (k > n) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage18));
        }
        if (k == 0 || k == n) {
            return new BigDecimal(1);
        }

        k = Math.min(k, n - k);
        long c = 1;
        for (int i = 0; i < k; i++) {
            c = c * (n - i) / (i + 1);
        }

        return BigDecimal.valueOf(c);
    }

    /**
     * Converts polar coordinates (r, θ) to Cartesian coordinates (x, y).
     *
     * @param r     The radial distance (magnitude) from the origin.
     * @param theta The angle (in radians) measured counterclockwise from the positive x-axis.
     * @return An array of two BigDecimals representing the x and y coordinates, respectively.
     */
    public static BigDecimal[] polarToCartesian(BigDecimal r, BigDecimal theta) {
        BigDecimal x = r.multiply(cos(theta));
        BigDecimal y = r.multiply(sin(theta));
        return new BigDecimal[]{x, y};
    }
    /**
     * Converts Cartesian coordinates (x, y) to polar coordinates (r, θ).
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return An array of two BigDecimals representing the radial distance (r) and the angle (θ) in degrees.
     */
    public static BigDecimal[] cartesianToPolar(BigDecimal x, BigDecimal y) {
        BigDecimal r = sqrt(x.pow(2).add(y.pow(2)), MC);
        BigDecimal theta = BigDecimal.valueOf(Math.atan2(y.doubleValue(), x.doubleValue()));
        BigDecimal thetaDeg = BigDecimal.valueOf(Math.toDegrees(theta.doubleValue()));

        return new BigDecimal[]{r, thetaDeg};
    }

    /**
     * Calculates the square root of a BigDecimal using the Babylonian method.
     *
     * @param value   The BigDecimal value for which to calculate the square root.
     * @param context The MathContext to use for precision and rounding.
     * @return The square root of the value as a BigDecimal.
     * @throws ArithmeticException If the input value is negative.
     */
    public static BigDecimal sqrt(BigDecimal value, MathContext context) {
        BigDecimal x0 = new BigDecimal(0);
        BigDecimal x1 = new BigDecimal(Math.sqrt(value.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = value.divide(x0, context);
            x1 = x1.add(x0);
            x1 = x1.divide(BigDecimal.valueOf(2), context);
        }
        return x1;
    }

    /**
     * Calculates the sine of an angle.
     *
     * @param operand The angle in radians or degrees (depending on CALCULATION_MODE).
     * @return The sine of the angle as a BigDecimal.
     */
    public static BigDecimal sin(BigDecimal operand) {
        BigDecimal operandBigDecimal = new BigDecimal(String.valueOf(operand), MathContext.DECIMAL128);
        BigDecimal result;

        if (CALCULATION_MODE.equals("Rad")) {
            result = new BigDecimal(Math.sin(operandBigDecimal.doubleValue()), MathContext.DECIMAL128)
                    .setScale(MC.getPrecision(), RoundingMode.DOWN);
        } else { // if mode equals 'Deg'
            double radians = Math.toRadians(operandBigDecimal.doubleValue());
            result = new BigDecimal(Math.sin(radians), MathContext.DECIMAL128)
                    .setScale(MC.getPrecision(), RoundingMode.DOWN);
        }
        return result;
    }

    /**
     * Calculates the arcsine (inverse sine) of a value.
     *
     * @param operand The value to calculate the arcsine of.
     * @return The arcsine in radians or degrees (depending on CALCULATION_MODE).
     * @throws ArithmeticException If the absolute value of the operand is greater than or equal to 1.
     */
    public static BigDecimal asin(BigDecimal operand) {
        if(CALCULATION_MODE.equals("Rad")) {
            return BigDecimalMath.asin(operand, MathContext.DECIMAL128);
        } else {
            return BigDecimalMath.asin(operand, MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(180))
                    .divide(BigDecimalMath.pi(MathContext.DECIMAL128), MathContext.DECIMAL128);
        }
    }

    /**
     * Calculates the hyperbolic sine of a value.
     *
     * @param operand The value to calculate the hyperbolic sine of.
     * @return The hyperbolic sine as a BigDecimal.
     */
    public static BigDecimal sinh(BigDecimal operand) {
        return BigDecimalMath.sinh(operand, MathContext.DECIMAL128);
    }

    /**
     * Calculates the inverse hyperbolic sine of a value.
     *
     * @param operand The value to calculate the inverse hyperbolic sine of.
     * @return The inverse hyperbolic sine as a BigDecimal.
     */
    public static BigDecimal asinh(BigDecimal operand) {
        return BigDecimalMath.asinh(operand, MathContext.DECIMAL128);
    }

    /**
     * Calculates the cosine of an angle.
     *
     * @param operand The angle in radians or degrees (depending on CALCULATION_MODE).
     * @return The cosine of the angle as a BigDecimal.
     */
    public static BigDecimal cos(BigDecimal operand) {
        BigDecimal operandBigDecimal = new BigDecimal(String.valueOf(operand), MathContext.DECIMAL128);
        BigDecimal result;

        if (CALCULATION_MODE.equals("Rad")) {
            result = new BigDecimal(Math.cos(operandBigDecimal.doubleValue()), MathContext.DECIMAL128)
                    .setScale(MC.getPrecision(), RoundingMode.DOWN);
        } else { // if mode equals 'Deg'
            double radians = Math.toRadians(operandBigDecimal.doubleValue());
            result = new BigDecimal(Math.cos(radians), MathContext.DECIMAL128)
                    .setScale(MC.getPrecision(), RoundingMode.DOWN);
        }
        return result;
    }

    /**
     * Calculates the arccosine (inverse cosine) of a value.
     *
     * @param operand The value to calculate the arccosine of.
     * @return The arccosine in radians or degrees (depending on CALCULATION_MODE).
     * @throws ArithmeticException If the absolute value of the operand is greater than or equal to 1.
     */
    public static BigDecimal acos(BigDecimal operand) {
        if (operand.compareTo(BigDecimal.valueOf(-1)) <= 0 || operand.compareTo(BigDecimal.valueOf(1)) >= 0) {
            throw new ArithmeticException(mainActivity.getString(R.string.errorMessage9));
        }

        if(CALCULATION_MODE.equals("Rad")) {
            return BigDecimalMath.acos(operand, MathContext.DECIMAL128);
        } else {
            return BigDecimalMath.acos(operand, MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(180))
                    .divide(BigDecimalMath.pi(MathContext.DECIMAL128), MathContext.DECIMAL128);
        }
    }

    /**
     * Calculates the hyperbolic cosine of a value.
     *
     * @param operand The value to calculate the hyperbolic cosine of.
     * @return The hyperbolic cosine as a BigDecimal.
     */
    public static BigDecimal cosh(BigDecimal operand) {
        return BigDecimalMath.cosh(operand, MathContext.DECIMAL128);
    }

    /**
     * Calculates the inverse hyperbolic cosine of a value.
     *
     * @param operand The value to calculate the inverse hyperbolic cosine of.
     * @return The inverse hyperbolic cosine as a BigDecimal.
     * @throws ArithmeticException If the operand is less than or equal to 1.
     */
    public static BigDecimal acosh(BigDecimal operand) {
        return BigDecimalMath.acos(operand, MathContext.DECIMAL128);
    }

    /**
     * Calculates the tangent of an angle.
     *
     * @param operand The angle in radians or degrees (depending on CALCULATION_MODE).
     * @return The tangent of the angle as a BigDecimal.
     * @throws ArithmeticException If the angle is a multiple of 90 degrees.
     */
    public static BigDecimal tan(BigDecimal operand) {
        BigDecimal operandBigDecimal = new BigDecimal(String.valueOf(operand), MathContext.DECIMAL128);
        BigDecimal result;

        if (CALCULATION_MODE.equals("Rad")) {
            result = new BigDecimal(Math.tan(operandBigDecimal.doubleValue()), MathContext.DECIMAL128)
                    .setScale(MC.getPrecision(), RoundingMode.DOWN);
        } else { // if mode equals 'Deg'
            double degrees = operand.doubleValue();
            if (isMultipleOf90(degrees)) {
                // Check if the tangent of multiples of 90 degrees is being calculated
                throw new ArithmeticException(mainActivity.getString(R.string.errorMessage9));
            }

            double radians = Math.toRadians(operandBigDecimal.doubleValue());
            result = new BigDecimal(Math.tan(radians), MathContext.DECIMAL128)
                    .setScale(MC.getPrecision(), RoundingMode.DOWN);
        }
        return result;
    }

    /**
     * Calculates the arctangent (inverse tangent) of a value.
     *
     * @param operand The value to calculate the arctangent of.
     * @return The arctangent in radians or degrees (depending on CALCULATION_MODE).
     */
    public static BigDecimal atan(BigDecimal operand) {
        if(CALCULATION_MODE.equals("Rad")) {
            return BigDecimalMath.atan(operand, MathContext.DECIMAL128);
        } else {
            return BigDecimalMath.atan(operand, MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(180))
                    .divide(BigDecimalMath.pi(MathContext.DECIMAL128), MathContext.DECIMAL128);
        }
    }

    /**
     * Calculates the hyperbolic tangent of a value.
     *
     * @param operand The value to calculate the hyperbolic tangent of.
     * @return The hyperbolic tangent as a BigDecimal.
     */
    public static BigDecimal tanh(BigDecimal operand) {
        return BigDecimalMath.tanh(operand, MathContext.DECIMAL128);
    }

    /**
     * Calculates the inverse hyperbolic tangent of a value.
     *
     * @param operand The value to calculate the inverse hyperbolic tangent of.
     * @return The inverse hyperbolic tangent as a BigDecimal.
     * @throws ArithmeticException If the absolute value of the operand is greater than or equal to 1.
     */
    public static BigDecimal atanh(BigDecimal operand) {
        if (operand.compareTo(BigDecimal.valueOf(-1)) <= 0 || operand.compareTo(BigDecimal.valueOf(1)) >= 0) {
            throw new ArithmeticException(mainActivity.getString(R.string.errorMessage9));
        }

        return BigDecimalMath.atanh(operand, MathContext.DECIMAL128);
    }

    /**
     * Calculates the logarithm of a value with the specified base.
     *
     * @param operand The value to calculate the logarithm of.
     * @param x       The base of the logarithm.
     * @return The logarithm as a BigDecimal.
     * @throws IllegalArgumentException If the operand is less than or equal to 0.
     */
    public static BigDecimal logX(BigDecimal operand, double x) {
        if (operand.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage9));
        }
        BigDecimal logBase = BigDecimal.valueOf(Math.log(x));
        BigDecimal logValue = BigDecimal.valueOf(Math.log(operand.doubleValue()));
        return logValue.divide(logBase, MC);
    }

    /**
     * Calculates the natural logarithm (base e) of a value.
     *
     * @param operand The value to calculate the natural logarithm of.
     * @return The natural logarithm as a BigDecimal.
     * @throws IllegalArgumentException If the operand is less than or equal to 0.
     */
    public static BigDecimal ln(BigDecimal operand) {
        if (operand.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage9));
        }
        BigDecimal operandBigDecimal = new BigDecimal(String.valueOf(operand), MathContext.DECIMAL128);
        BigDecimal result = new BigDecimal(Math.log(operandBigDecimal.doubleValue()), MathContext.DECIMAL128)
                .setScale(MC.getPrecision(), RoundingMode.DOWN);
        return result;
    }
}