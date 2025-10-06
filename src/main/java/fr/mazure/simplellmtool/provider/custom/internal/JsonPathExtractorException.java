package fr.mazure.simplellmtool.provider.custom.internal;

/*
 * JsonPathExtractorException is thrown when a JSON path cannot be retrieved from a JSON payload
 */
public class JsonPathExtractorException extends Exception {

    public JsonPathExtractorException(final String message,
                                      final Throwable cause) {
        super(message, cause);
    }
}
