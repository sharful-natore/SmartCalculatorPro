package com.example.util

import kotlin.math.*

object ExpressionEvaluator {
    fun evaluate(expression: String, isDegreeMode: Boolean = true): Double {
        // Pre-process expression string to map human-readable symbols to parser-friendly symbols
        var cleaned = expression
            .replace("−", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("3√", "cbrt")
            .replace("log10", "logten")
            .replace("√", "sqrt")
            .replace("ln", "ln")
            .replace("log", "log")
            
        // Insert multiplication sign between number/constant/bracket implicitly if needed, e.g. 2pi -> 2*pi, (2)(3) -> (2)*(3)
        cleaned = insertImplicitMultiplication(cleaned)

        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < cleaned.length) cleaned[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < cleaned.length) throw RuntimeException("Unexpected character: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) {
                        x += parseTerm(baseValue = x) // addition with percentage context
                    } else if (eat('-'.code)) {
                        x -= parseTerm(baseValue = x) // subtraction with percentage context
                    } else break
                }
                return x
            }

            fun parseTerm(baseValue: Double? = null): Double {
                var x = parseFactor(baseValue = baseValue)
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication (no baseValue context for subsequent factors)
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else break
                }
                return x
            }

            fun parseFactor(baseValue: Double? = null): Double {
                if (eat('+'.code)) return +parseFactor(baseValue) // unary plus
                if (eat('-'.code)) return -parseFactor(baseValue) // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    if (!eat(')'.code)) throw RuntimeException("Missing closing parenthesis")
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = cleaned.substring(startPos, this.pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions or constants
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = cleaned.substring(startPos, this.pos)
                    if (eat('('.code)) {
                        val arg = parseExpression()
                        if (!eat(')'.code)) throw RuntimeException("Missing closing parenthesis after $func")
                        x = when (func) {
                            "sin" -> {
                                val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                                sin(rad)
                            }
                            "cos" -> {
                                val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                                cos(rad)
                            }
                            "tan" -> {
                                val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                                tan(rad)
                            }
                            "asin" -> {
                                val res = asin(arg)
                                if (isDegreeMode) Math.toDegrees(res) else res
                            }
                            "acos" -> {
                                val res = acos(arg)
                                if (isDegreeMode) Math.toDegrees(res) else res
                            }
                            "atan" -> {
                                val res = atan(arg)
                                if (isDegreeMode) Math.toDegrees(res) else res
                            }
                            "log" -> log10(arg)
                            "logten" -> log10(arg)
                            "antilog" -> 10.0.pow(arg)
                            "ln" -> ln(arg)
                            "sqrt" -> {
                                if (arg < 0.0) throw ArithmeticException("Square root of negative number")
                                sqrt(arg)
                            }
                            "cbrt" -> {
                                cbrt(arg)
                            }
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    } else {
                        x = when (func) {
                            "pi" -> Math.PI
                            "e" -> Math.E
                            else -> throw RuntimeException("Unknown constant: $func")
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // power

                if (eat('!'.code)) {
                    x = factorial(x)
                }

                // Handle postfix percentage operator (%)
                while (eat('%'.code)) {
                    if (baseValue != null) {
                        x = baseValue * (x / 100.0)
                    } else {
                        x = x / 100.0
                    }
                }

                return x
            }

            fun factorial(n: Double): Double {
                if (n < 0.0) throw IllegalArgumentException("Negative factorial")
                val i = n.toLong()
                if (i.toDouble() != n) throw IllegalArgumentException("Fractional factorial")
                if (i > 170) return Double.POSITIVE_INFINITY
                var fact = 1.0
                for (j in 1..i) {
                    fact *= j
                }
                return fact
            }
        }.parse()
    }

    private fun insertImplicitMultiplication(expr: String): String {
        val result = StringBuilder()
        for (i in expr.indices) {
            val curr = expr[i]
            result.append(curr)
            if (i < expr.length - 1) {
                val next = expr[i + 1]
                // Conditions to insert implicit '*'
                // 1. Number followed by parenthesis or constant/function
                // 2. Constant 'e' or 'pi' followed by number or constant
                // 3. Parenthesis ')' followed by number, constant or parenthesis '('
                val isCurrDigitOrDot = curr.isDigit() || curr == '.'
                val isNextLetterOrParenthesis = next.isLetter() || next == '('
                
                val isCurrPiOrE = (curr == 'i' && i >= 1 && expr[i-1] == 'p') || (curr == 'e')
                val isNextDigitOrLetter = next.isDigit() || next.isLetter()
                
                val isCurrCloseParenthesis = curr == ')'
                val isNextOpenParenthesisOrDigitOrLetter = next == '(' || next.isDigit() || next.isLetter()

                if ((isCurrDigitOrDot && isNextLetterOrParenthesis) ||
                    (isCurrPiOrE && isNextDigitOrLetter) ||
                    (isCurrCloseParenthesis && isNextOpenParenthesisOrDigitOrLetter)) {
                    result.append("*")
                }
            }
        }
        return result.toString()
    }
}
