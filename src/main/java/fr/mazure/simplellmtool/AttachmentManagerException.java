package fr.mazure.simplellmtool;

/*
 * AttachmentManagerException is thrown when an attachment cannot be managed
 */
public class AttachmentManagerException extends Exception {
    public AttachmentManagerException(final String message) {
        super(message);
    }
}
