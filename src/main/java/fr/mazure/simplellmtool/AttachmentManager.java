package fr.mazure.simplellmtool;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.VideoContent;

/*
 * AttachmentManager is a class that manages attachments
 */
public class AttachmentManager {
    private enum AttachementType {
        IMAGE,
        AUDIO,
        VIDEO,
        PDF
    }
    
    /*
     * getFileContent returns the content of a file
     */
    public static Content getFileContent(final Path filePath) throws AttachmentManagerException {
        final String fileExtension = getExtension(filePath.toString() );
        final AttachementType type = getAttachmentType(fileExtension);

        return switch (type) {
            case AttachementType.IMAGE -> getImageFileContent(filePath);
            case AttachementType.AUDIO -> getAudioFileContent(filePath);
            case AttachementType.VIDEO -> getVideoFileContent(filePath);
            case AttachementType.PDF -> getPdfFileContent(filePath);
        };
    }

    /*
     * getUriContent returns the content of a URI
     */
    public static Content getUriContent(final URI uri) throws AttachmentManagerException {
        final String fileExtension = getExtension(uri.getPath());
        final AttachementType type = getAttachmentType(fileExtension);

        return switch (type) {
            case AttachementType.IMAGE -> ImageContent.from(uri);
            case AttachementType.AUDIO -> AudioContent.from(uri);
            case AttachementType.VIDEO -> VideoContent.from(uri);
            case AttachementType.PDF -> PdfFileContent.from(uri);
        };
    }

    private static AttachementType getAttachmentType(final String extension) throws AttachmentManagerException {
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> AttachementType.IMAGE;
            case "mp3" -> AttachementType.AUDIO;
            case "mp4" -> AttachementType.VIDEO;
            case "pdf" -> AttachementType.PDF;
            default -> throw new AttachmentManagerException("Unsupported filename extension: " + extension);
        };
    }

    private static Content getImageFileContent(final Path filePath) throws AttachmentManagerException {
        String fileExtension = getExtension(filePath.toString());
        if (fileExtension.equals("jpg")) {
            fileExtension = "jpeg";
        }
        final String base64Data = getFileContentAsBase64(filePath);
        return ImageContent.from(base64Data, "image/" + fileExtension);
    }

    private static Content getAudioFileContent(final Path filePath) throws AttachmentManagerException {
        final String fileExtension = getExtension(filePath.toString());
        final String base64Data = getFileContentAsBase64(filePath);
        return AudioContent.from(base64Data, "audio/" + fileExtension);
    }

    private static Content getVideoFileContent(final Path filePath) throws AttachmentManagerException {
        final String fileExtension = getExtension(filePath.toString());
        final String base64Data = getFileContentAsBase64(filePath);
        return VideoContent.from(base64Data, "video/" + fileExtension);
    }

    private static Content getPdfFileContent(final Path filePath) throws AttachmentManagerException {
        final String base64Data = getFileContentAsBase64(filePath);
        return PdfFileContent.from(base64Data, "application/pdf");
    }

    private static String getExtension(final String filename) {
        return filename.toString().substring(filename.toString().lastIndexOf('.') + 1).toLowerCase();
    }

    private static String getFileContentAsBase64(final Path filePath) throws AttachmentManagerException {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(filePath);
        } catch (final IOException e) {
            throw new AttachmentManagerException("Error reading file: " + e.getMessage());
        }
        return Base64.getEncoder().encodeToString(bytes);
    }
}
