package fr.mazure.simplellmtool.provider.custom.internal;

/*
 * JsonPathExtractorException is thrown when a JSON path cannot be retrieved from a JSON payload
 */
public class JsonPathExtractorException extends Exception {
    
    final String path;

    public JsonPathExtractorException(final String message,
                                      final String path) {
        super(message);
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
