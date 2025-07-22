package fr.mazure.aitestcasegeneration.provider.base;

public class InvalidModelParameter extends Exception {

    public InvalidModelParameter(final String parameterName,
                                 final String type,
                                 final String parameterValue) {
        super("Invalid model parameter (should be of type " + type + "): " + parameterName + " has value \"" + parameterValue + "\"");
    }
}
