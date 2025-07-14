package fr.mazure.aitestcasegeneration;

public enum ExitCode {

    SUCCESS(0, "success"),
    INVALID_COMMAND_LINE(1, "invalid command line"),
    FILE_ERROR(2, "reading/writing file error"),
    INVALID_PROVIDER(3, "invalid provider"),
    MODEL_ERROR(3, "model error");

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
