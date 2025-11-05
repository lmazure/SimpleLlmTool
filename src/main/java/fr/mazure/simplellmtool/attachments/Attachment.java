package fr.mazure.simplellmtool.attachments;

public record Attachment(AttachmentSource source, String path) {
    public enum AttachmentSource {
        FILE,
        URL;
    }
}