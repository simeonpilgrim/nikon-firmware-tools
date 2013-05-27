package com.nikonhacker.disassembly;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private static final int REG_NONE = -1;
    int address;
    String name;
    String comment;
    private List<String> aliases;
    private List<Parameter> parameterList;
    private String rawText;

    public Symbol(int address, String name) {
        this.address = address;
        this.name = name;
    }

    public Symbol(int address, String name, String comment) {
        this.address = address & CodeStructure.IGNORE_ISA_BIT;
        this.name = name;
        this.comment = comment;
    }

    /**
     * Parses text for the given address in its different elements
     * @param address
     * @param rawText of the form "MOD_int(R4 [IN dividend, OUT remainder], R5 [IN divisor])" with optional comment between the parenthesis
     * @throws com.nikonhacker.disassembly.ParsingException
     */
    public Symbol(Integer address, String rawText, String[][] registerLabels) throws ParsingException {
        this.address = address;
        this.rawText = rawText;
        // First, remove all comments between /* */
        String cleanText = rawText.replaceAll("/\\*.*?\\*/", "");
        if (cleanText.contains("(")) {
            this.name = StringUtils.substringBefore(cleanText, "(").trim();
            this.comment = StringUtils.substringAfter(cleanText, "(");
            if (!comment.contains(")")) {
                throw new ParsingException("Invalid symbol '" + cleanText + "' : no closing parenthesis");
            }
            comment = StringUtils.substringBefore(comment, ")").trim();
            // Comment of the form R4 [IN dividend, OUT remainder], R5 [IN divisor]
            String cleanComment = comment.replaceAll("/\\*.*?\\*/", "");
            String[] paramStrings = StringUtils.split(cleanComment, ',');
            for (String paramString : paramStrings) {
                Parameter parameter = new Parameter(paramString, registerLabels);
                addParameter(parameter);
            }
//            System.out.println("** Name:" + name);
//            System.out.println("   Comment:" + comment);
//            System.out.println("   CleanComment:" + cleanComment);
//            System.out.println("   Parameters:" + parameterList);
        }
        else {
            this.name = cleanText;
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

        private int register = REG_NONE;
        private String inVariableName = null;
        private String outVariableName = null;

        /**
         * Parses a param from a String
         * @param parameterString string of the form "R4 [IN dividend, OUT remainder]"
         */
        public Parameter(String parameterString, String[][] registerLabels) throws ParsingException {
            String registerText = StringUtils.substringBefore(parameterString, "[").trim();
            for (int list = 0; list < 2; list++) {
                for (int reg = 0; reg < registerLabels[list].length; reg++) {
                    if (registerLabels[list][reg].equalsIgnoreCase(registerText)) {
                        register = reg;
                        break;
                    }
                }
                if (register != REG_NONE) break;
            }
            if (register == REG_NONE) throw new ParsingException("Invalid register name '" + registerText + "' in function parameter string '" + parameterString + "'");

            String details = StringUtils.substringAfter(parameterString, "[");
            if (!details.contains("]")) {
                throw new ParsingException("Invalid function parameter details '" + details + "' : no closing bracket");
            }
            details = StringUtils.substringBefore(details, "]").trim();
            for (String detail : StringUtils.split(details, ";")) {
                parseDetail(detail.trim(), registerLabels);
            }
        }

        /**
         * Parses a parameter detail
         * @param detail string of the form "IN variablename" or "OUT variablename"
         * @param registerLabels
         */
        private void parseDetail(String detail, String[][] registerLabels) throws ParsingException {
            String type = StringUtils.substringBefore(detail, " ").toUpperCase().trim();
            String variableName = StringUtils.replace(StringUtils.substringAfter(detail, " ").trim(), " ", "_");
            if (StringUtils.isEmpty(variableName)) {
                variableName = registerLabels[0][register];
            }
            if ("IN".equals(type)) {
                inVariableName = variableName;
            }
            else if ("OUT".equals(type)) {
                outVariableName = variableName;
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

        public String getInVariableName() {
            return inVariableName;
        }

        public void setInVariableName(String inVariableName) {
            this.inVariableName = inVariableName;
        }

        public String getOutVariableName() {
            return outVariableName;
        }

        public void setOutVariableName(String outVariableName) {
            this.outVariableName = outVariableName;
        }

        @Override
        public String toString() {
            return "Parameter{" +
                    "register=" + register +
                    ", inVariable='" + inVariableName + '\'' +
                    ", outVariable='" + outVariableName + '\'' +
                    '}';
        }
    }
}
