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

import android.util.Log;

import java.util.Stack;

public class ParenthesesBalancer {
    /**
     * This method takes a mathematical expression as input, checks if parentheses are
     * balanced, and adds missing parentheses as needed to balance the expression.
     *
     * @param input The input mathematical expression.
     * @return The balanced mathematical expression.
     */
    public static String balanceParentheses(String input) {
        // Check for empty input
        if (input.isEmpty()) {
            return "0";
        }

        // Remove spaces and equals sign
        input = input.replace(" ", "");

        // Stack to keep track of positions of unbalanced parentheses
        Stack<Integer> stack = new Stack<>();

        // Check for unbalanced parentheses and add missing parentheses
        StringBuilder inputBuilder = new StringBuilder(input);
        for (int i = 0; i < inputBuilder.length(); i++) {
            char ch = inputBuilder.charAt(i);
            if (ch == '(') {
                stack.push(i);
            } else if (ch == ')') {
                if (!stack.isEmpty()) {
                    stack.pop();
                } else {
                    // Check if there are already opening parentheses at the beginning
                    if (i == 0 || inputBuilder.charAt(i - 1) != '(') {
                        inputBuilder.insert(0, '(');
                        i++; // Update current index due to insertion
                    }
                }
            }
        }

        // Add missing closing parentheses
        while (!stack.isEmpty()) {
            inputBuilder.append(')');
            stack.pop();
        }

        // Check if parentheses are needed and add them if necessary
        if (needsParentheses(inputBuilder.toString())) {
            inputBuilder.insert(0, '(');
            inputBuilder.append(')');
        }

        return inputBuilder.toString();
    }

    // Method to check if parentheses are needed
    private static boolean needsParentheses(String input) {
        if (input.startsWith("(") && input.endsWith(")")) {
            // Already wrapped in parentheses
            return false;
        }

        int openCount = 0;
        int closeCount = 0;

        for (char ch : input.toCharArray()) {
            if (ch == '(') {
                openCount++;
            } else if (ch == ')') {
                closeCount++;
            }
        }

        // Parentheses needed if counts are unequal
        return openCount != closeCount;
    }
}