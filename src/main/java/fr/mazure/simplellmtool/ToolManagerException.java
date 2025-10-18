package fr.mazure.simplellmtool;

/*
 * ToolManagerException is thrown when a tool cannot be managed
 */
public class ToolManagerException extends Exception {
    public ToolManagerException(final String message) {
        super(message);
    }

    public ToolManagerException(final String message,
                                final Throwable e) {
        super(message, e);
    }
}
