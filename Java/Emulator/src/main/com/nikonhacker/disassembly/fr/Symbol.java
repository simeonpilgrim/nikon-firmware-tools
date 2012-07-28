package com.nikonhacker.disassembly.fr;

import com.nikonhacker.disassembly.ParsingException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    int address;
    String name;
    String comment;
    List<String> aliases;
    private List<Parameter> parameterList;
    private String rawText;

    public Symbol(int address, String name, String comment) {
        this.address = address;
        this.name = name;
        this.comment = comment;
    }

    /**
     * Parses text for the given address in its different elements
     * @param address
     * @param rawText of the form "MOD_int(R4 [IN dividend, OUT remainder], R5 [IN divisor])" with optional comment between the parenthesis
     * @throws com.nikonhacker.disassembly.ParsingException
     */
    public Symbol(Integer address, String rawText) throws ParsingException {
        this.address = address;
        this.rawText = rawText;
        if (rawText.contains("(")) {
            this.name = StringUtils.substringBefore(rawText, "(").trim();
            this.comment = StringUtils.substringAfter(rawText, "(");
            if (!comment.contains(")")) {
                throw new ParsingException("Invalid symbol '" + rawText + "' : no closing parenthesis");
            }
            comment = StringUtils.substringBefore(comment, ")").trim();
            // Comment of the form R4 [IN dividend, OUT remainder], R5 [IN divisor]
            String cleanComment = comment.replaceAll("/\\*.*\\*/", "");
            String[] paramStrings = StringUtils.split(cleanComment, ',');
            for (String paramString : paramStrings) {
                Parameter parameter = new Parameter(paramString);
                addParameter(parameter);
            }
//            System.out.println("** Name:" + name);
//            System.out.println("   Comment:" + comment);
//            System.out.println("   CleanComment:" + cleanComment);
//            System.out.println("   Parameters:" + parameterList);
        }
        else {
            this.name = rawText;
        }
    }

    public int getAddress() {
        return address;
    }

    public String getRawText() {
        return rawText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void addAlias(String alias) {
        if (aliases == null) {
            aliases = new ArrayList<String>();
        }
        aliases.add(alias);
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    private void addParameter(Parameter parameter) {
        if (parameterList == null) {
            parameterList = new ArrayList<Parameter>();
        }
        parameterList.add(parameter);
    }


    @Override
    public String toString() {
        String out = name;
        if (StringUtils.isNotBlank(comment)) {
            out += " (" + comment +")";
        }
        return out;
    }

    public class Parameter {

        private int register = -1;
        private String inVariable = null;
        private String outVariable = null;

        /**
         * Parses a param from a String
         * @param parameterString string of the form "R4 [IN dividend, OUT remainder]"
         */
        public Parameter(String parameterString) throws ParsingException {
            String registerText = StringUtils.substringBefore(parameterString, "[").trim().toUpperCase();
            for (int i = 0; i < CPUState.REG_LABEL.length; i++) {
                if (CPUState.REG_LABEL[i].equals(registerText)) {
                    register = i;
                    break;
                }
            }
            if (register == -1) {
                // try both std and alt names
                if ("AC".equals(registerText) || "R13".equals(registerText)) register = 13;
                else if ("FP".equals(registerText) || "R14".equals(registerText)) register = 14;
                else if ("SP".equals(registerText) || "R15".equals(registerText)) register = 15;
                else throw new ParsingException("Invalid register in function parameter '" + registerText + "'");
            }
            String details = StringUtils.substringAfter(parameterString, "[");
            if (!details.contains("]")) {
                throw new ParsingException("Invalid function parameter details '" + details + "' : no closing bracket");
            }
            details = StringUtils.substringBefore(details, "]").trim();
            for (String detail : StringUtils.split(details, ";")) {
                parseDetail(detail.trim());
            }
        }

        /**
         * Parses a parameter detail
         * @param detail string of the form "IN variablename" or "OUT variable name
         */
        private void parseDetail(String detail) throws ParsingException {
            String type = StringUtils.substringBefore(detail, " ").toUpperCase().trim();
            String variable = StringUtils.replace(StringUtils.substringAfter(detail, " ").trim(), " ", "_");
            if (StringUtils.isEmpty(variable)) {
                variable = CPUState.REG_LABEL[register];
            }
            if ("IN".equals(type)) {
                inVariable = variable;
            }
            else if ("OUT".equals(type)) {
                outVariable = variable;
            }
            else {
                throw new ParsingException("Unknown variable type in function parameter: '" + type + "'");
            }
        }

        public int getRegister() {
            return register;
        }

        public void setRegister(int register) {
            this.register = register;
        }

        public String getInVariable() {
            return inVariable;
        }

        public void setInVariable(String inVariable) {
            this.inVariable = inVariable;
        }

        public String getOutVariable() {
            return outVariable;
        }

        public void setOutVariable(String outVariable) {
            this.outVariable = outVariable;
        }

        @Override
        public String toString() {
            return "Parameter{" +
                    "register=" + register +
                    ", inVariable='" + inVariable + '\'' +
                    ", outVariable='" + outVariable + '\'' +
                    '}';
        }
    }
}
