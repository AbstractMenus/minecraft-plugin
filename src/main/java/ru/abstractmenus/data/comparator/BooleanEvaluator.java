package ru.abstractmenus.data.comparator;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

import java.util.Iterator;

public class BooleanEvaluator extends AbstractEvaluator<String> {

    private final static Operator MORE = new Operator(">", 2, Operator.Associativity.LEFT, 3);
    private final static Operator LESS = new Operator("<", 2, Operator.Associativity.LEFT, 3);
    private final static Operator MORE_OR_EQUAL = new Operator(">=", 2, Operator.Associativity.LEFT, 3);
    private final static Operator LESS_OR_EQUAL = new Operator("<=", 2, Operator.Associativity.LEFT, 3);
    private final static Operator EQUAL = new Operator("==", 2, Operator.Associativity.LEFT, 2);
    private final static Operator NOT_EQUAL = new Operator("!=", 2, Operator.Associativity.LEFT, 2);
    private final static Operator EQUALS_IGNORE_CASE = new Operator("===", 2, Operator.Associativity.LEFT, 2);
    private final static Operator NOT_EQUALS_IGNORE_CASE = new Operator("!==", 2, Operator.Associativity.LEFT, 2);
    private final static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 1);
    private final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 0);

    private static final Parameters PARAMETERS;

    static {
        PARAMETERS = new Parameters();
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        PARAMETERS.add(MORE);
        PARAMETERS.add(LESS);
        PARAMETERS.add(MORE_OR_EQUAL);
        PARAMETERS.add(LESS_OR_EQUAL);
        PARAMETERS.add(EQUAL);
        PARAMETERS.add(NOT_EQUAL);
        PARAMETERS.add(EQUALS_IGNORE_CASE);
        PARAMETERS.add(NOT_EQUALS_IGNORE_CASE);
        PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
    }

    public BooleanEvaluator() {
        super(PARAMETERS);
    }

    @Override
    protected String toValue(String literal, Object evaluationContext) {
        return literal;
    }

    @Override
    protected String evaluate(Operator operator, Iterator<String> operands, Object ctx) {
        String leftOperand = operands.next();
        String rightOperand = operands.next();
        boolean result = false;

        if (operator == AND) {
            result = getBool(leftOperand) && getBool(rightOperand);
        } else if (operator == OR) {
            result = getBool(leftOperand) || getBool(rightOperand);
        } else if (operator == MORE) {
            result = getNumber(leftOperand) > getNumber(rightOperand);
        } else if (operator == LESS) {
            result = getNumber(leftOperand) < getNumber(rightOperand);
        } else if (operator == MORE_OR_EQUAL) {
            result = getNumber(leftOperand) >= getNumber(rightOperand);
        } else if (operator == LESS_OR_EQUAL) {
            result = getNumber(leftOperand) <= getNumber(rightOperand);
        } else if (operator == EQUAL) {
            result = leftOperand.equals(rightOperand);
        } else if (operator == NOT_EQUAL) {
            result = !leftOperand.equals(rightOperand);
        } else if (operator == EQUALS_IGNORE_CASE) {
            result = leftOperand.equalsIgnoreCase(rightOperand);
        } else if (operator == NOT_EQUALS_IGNORE_CASE) {
            result = !leftOperand.equalsIgnoreCase(rightOperand);
        }

        return String.valueOf(result);
    }

    private boolean getBool(String str) {
        return str.equalsIgnoreCase("true")
                || str.equalsIgnoreCase("yes")
                || str.equals("1");
    }

    private double getNumber(String str) {
        return Double.parseDouble(str);
    }
}
