package fr.mazure.simplellmtool;

/**
 * Enum for the exit codes of the application.
 *
 * The exit codes are as follows:
 * <ul>
 *     <li>{@link #SUCCESS}: the application ran successfully</li>
 *     <li>{@link #INVALID_COMMAND_LINE}: the command line was invalid</li>
 *     <li>{@link #FILE_ERROR}: there was an error reading or writing a file</li>
 *     <li>{@link #INVALID_PROVIDER}: the provider was invalid</li>
 *     <li>{@link #MODEL_ERROR}: there was an error with the model</li>
 *     <li>{@link #ATTACHMENT_ERROR}: there was an error with the attachments</li>
 * </ul>
 */
public enum ExitCode {

    SUCCESS(0, "success"),
    INVALID_COMMAND_LINE(1, "invalid command line"),
    FILE_ERROR(2, "reading/writing file error"),
    INVALID_PROVIDER(3, "invalid provider"),
    MODEL_ERROR(4, "model error"),
    ATTACHMENT_ERROR(5, "attachment error");

    private final int code;
    private final String message;

    private ExitCode(final int code,
                     final String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
