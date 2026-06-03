package com.calculator.app.core;

public class CalculatorEngine {

    private String count = "0";
    private String oldType = "num";  
    private String memory = "0";
    private String memoryDisplay = "";

    private static final String NUMS = "0123456789)";

    public String getCount() { return count; }
    public String getMemoryDisplay() { return memoryDisplay; }
    public String getMemory() { return memory; }

    private boolean lastCharIsNum() {
        if (count.isEmpty()) return false;
        char last = count.charAt(count.length() - 1);
        return NUMS.indexOf(last) >= 0;
    }

    
    public void inputNum(String digit) {
        if (count.equals("0")) {
            count = digit;
            oldType = "num";
        } else if (count.equals("√0")) {
            count = "√" + digit;
            oldType = "num";
        } else if (oldType.equals("num")) {
            count += digit;
            oldType = "num";
        } else {
            count += (" " + digit);
            oldType = "num";
        }
    }

    
    public void inputOperation(String op) {
        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
                if (oldType.equals("num")) {
                    
                    String realOp = op.equals("×") ? "*" : op.equals("÷") ? "/" : op;
                    count += " " + realOp;
                    oldType = "opr";
                }
                break;
            case ".":
                if (oldType.equals("num")) {
                    String[] parts = count.split(" ");
                    String last = parts[parts.length - 1];
                    if (!last.contains(".")) {
                        count += ".";
                    }
                }
                break;
            case "del":
                inputDel();
                break;
            case "Clear":
                count = "0";
                oldType = "num";
                break;
            case "ClrEntr":
                inputCE();
                break;
            case "PlsMns":
                inputPlusMinus();
                break;
            case "Root":
                inputRoot();
                break;
            case "Perc":
                inputPercent();
                break;
            case "1/inp":
                inputInverse();
                break;
        }
    }

    private void inputDel() {
        int len = count.length();
        if (len == 1) {
            count = "0";
        } else if (len >= 2 && count.charAt(len - 2) == ' ') {
            count = count.substring(0, len - 2);
        } else {
            count = count.substring(0, len - 1);
        }
        if (!count.isEmpty()) {
            char last = count.charAt(count.length() - 1);
            oldType = (NUMS.indexOf(last) >= 0) ? "num" : "opr";
        } else {
            count = "0";
            oldType = "num";
        }
    }

    private void inputCE() {
        if (oldType.equals("num")) {
            if (count.length() == 1) {
                if (!count.equals("0")) count = "0";
            } else {
                String[] parts = count.split(" ");
                if (parts.length == 1) {
                    count = "0";
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (i > 0) sb.append(" ");
                        sb.append(parts[i]);
                    }
                    count = sb.toString();
                    char last = count.charAt(count.length() - 1);
                    oldType = (NUMS.indexOf(last) >= 0) ? "num" : "opr";
                }
            }
        }
    }

    private void inputPlusMinus() {
        if (!oldType.equals("num")) return;
        try {
            String[] parts = count.split(" ");
            String lastPart = parts[parts.length - 1];
            double val = evalSimple(lastPart);
            val = val * -1;
            String valStr = formatNumber(val);
            if (parts.length == 1) {
                count = valStr;
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    sb.append(parts[i]).append(" ");
                }
                sb.append(valStr);
                count = sb.toString();
            }
        } catch (Exception ignored) {}
    }

    private void inputRoot() {
        if (oldType.equals("num")) {
            if (count.equals("0")) {
                count = "√0";
            } else {
                String[] parts = count.split(" ");
                if (parts.length == 1) {
                    count = "√" + count;
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        sb.append(parts[i]).append(" ");
                    }
                    sb.append("√").append(parts[parts.length - 1]);
                    count = sb.toString();
                }
            }
        } else {
            count += " √";
            oldType = "num";
        }
    }

    private void inputPercent() {
        if (!oldType.equals("num")) return;
        try {
            String[] parts = count.split(" ");
            String lastPart = parts[parts.length - 1];
            double val = evalSimple(lastPart) / 100.0;
            String valStr = formatNumber(val);
            if (parts.length == 1) {
                count = valStr;
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    sb.append(parts[i]).append(" ");
                }
                sb.append(valStr);
                count = sb.toString();
            }
        } catch (Exception ignored) {}
    }

    private void inputInverse() {
        if (!oldType.equals("num")) return;
        try {
            String[] parts = count.split(" ");
            String lastPart = parts[parts.length - 1];
            double val = 1.0 / evalSimple(lastPart);
            String valStr = formatNumber(val);
            if (parts.length == 1) {
                count = valStr;
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    sb.append(parts[i]).append(" ");
                }
                sb.append(valStr);
                count = sb.toString();
            }
        } catch (Exception ignored) {}
    }

    
    public String calculate() {
        if (count == null || count.isEmpty()) {
            count = "0";
            return "0";
        }
        try {
            
            String expr = buildExpression(count);
            double result = evaluate(expr);
            count = formatNumber(result);
            oldType = "num";
            return count;
        } catch (Exception e) {
            return null; 
        }
    }

    
    public String calculateForMemory() {
        String saved = count;
        String oldOldType = oldType;
        String result = calculate();
        if (result != null) {
            String res = count;
            count = saved;
            oldType = oldOldType;
            return res;
        }
        return null;
    }

    
    public void memoryClear() {
        memory = "0";
        memoryDisplay = "";
    }

    public void memoryRead() {
        count = memory;
        oldType = "num";
    }

    public void memoryStore() {
        String res = calculateForMemory();
        if (res != null) {
            memory = formatNumber(Double.parseDouble(res));
            memoryDisplay = "M " + memory;
        }
    }

    public void memoryPlus() {
        if (count.equals("0")) return;
        String res = calculateForMemory();
        if (res != null) {
            double val = Double.parseDouble(memory) + Double.parseDouble(res);
            memory = formatNumber(val);
            memoryDisplay = "M " + memory;
        }
    }

    public void memoryMinus() {
        if (count.equals("0")) return;
        String res = calculateForMemory();
        if (res != null) {
            double val = Double.parseDouble(memory) - Double.parseDouble(res);
            memory = formatNumber(val);
            memoryDisplay = "M " + memory;
        }
    }

    

    private String buildExpression(String expr) {
        
        
        StringBuilder sb = new StringBuilder();
        String[] tokens = expr.trim().split(" ");
        for (String token : tokens) {
            if (sb.length() > 0) sb.append(" ");
            if (token.startsWith("√")) {
                sb.append("sqrt(").append(token.substring(1)).append(")");
            } else if (token.equals("√")) {
                sb.append("sqrt(0)");
            } else {
                sb.append(token);
            }
        }
        return sb.toString();
    }

    
    private double evaluate(String expr) {
        return new ExprParser(expr.replaceAll("\\s+", "")).parse();
    }

    private double evalSimple(String s) {
        if (s.startsWith("√")) {
            double inner = Double.parseDouble(s.substring(1));
            return Math.sqrt(inner);
        }
        return Double.parseDouble(s);
    }

    public static String formatNumber(double val) {
        if (val % 1 == 0 && !Double.isInfinite(val)) {
            return String.valueOf((long) val);
        } else {
            
            String s = String.format("%.10f", val);
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
            return s;
        }
    }

    
    private static class ExprParser {
        private final String src;
        private int pos = 0;

        ExprParser(String src) { this.src = src; }

        double parse() {
            double result = parseExpr();
            if (pos != src.length()) throw new RuntimeException("Unexpected char at " + pos);
            return result;
        }

        double parseExpr() {
            double left = parseTerm();
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '+') { pos++; left += parseTerm(); }
                else if (c == '-') { pos++; left -= parseTerm(); }
                else break;
            }
            return left;
        }

        double parseTerm() {
            double left = parseFactor();
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '*') { pos++; left *= parseFactor(); }
                else if (c == '/') {
                    pos++;
                    double divisor = parseFactor();
                    if (divisor == 0) throw new ArithmeticException("Division by zero");
                    left /= divisor;
                }
                else break;
            }
            return left;
        }

        double parseFactor() {
            if (pos < src.length() && src.charAt(pos) == '-') {
                pos++;
                return -parseFactor();
            }
            if (pos < src.length() && src.charAt(pos) == '+') {
                pos++;
                return parseFactor();
            }
            if (pos + 4 < src.length() && src.substring(pos, pos + 5).equals("sqrt(")) {
                pos += 5;
                double inner = parseExpr();
                if (pos < src.length() && src.charAt(pos) == ')') pos++;
                if (inner < 0) throw new ArithmeticException("sqrt of negative");
                return Math.sqrt(inner);
            }
            if (pos < src.length() && src.charAt(pos) == '(') {
                pos++;
                double val = parseExpr();
                if (pos < src.length() && src.charAt(pos) == ')') pos++;
                return val;
            }
            return parseNumber();
        }

        double parseNumber() {
            int start = pos;
            if (pos < src.length() && src.charAt(pos) == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
            if (start == pos) throw new RuntimeException("Expected number at " + pos);
            return Double.parseDouble(src.substring(start, pos));
        }
    }
}
