package fr.mazure.simplellmtool.provider.base;

public class MissingEnvironmentVariable extends Exception {
    public MissingEnvironmentVariable(final String environmentVariableName,
                                      final String message) {
        super(environmentVariableName + " environment variable is not set. " + message);
    }
}
