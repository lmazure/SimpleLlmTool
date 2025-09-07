package fr.mazure.simplellmtool.provider.base;

public class MissingModelParameter extends Exception {
    public MissingModelParameter(final String parameterName) {
        super("Missing model parameter: " + parameterName);
    }
}
