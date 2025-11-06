package fr.mazure.simplellmtool.attachments;

/**
 * Attachment represents an attachment
 */
public record Attachment(AttachmentSource source, String path) {
    public enum AttachmentSource {
        FILE,
        URL;
    }
}
