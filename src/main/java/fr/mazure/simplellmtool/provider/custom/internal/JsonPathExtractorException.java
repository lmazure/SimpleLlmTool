package fr.mazure.simplellmtool.provider.custom.internal;

/**
 * JsonPathExtractorException is thrown when a JSON path cannot be retrieved from a JSON payload
 */
class JsonPathExtractorException extends Exception {

    JsonPathExtractorException(final String message,
                               final Throwable cause) {
        super(message, cause);
    }
}
