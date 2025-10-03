package fr.mazure.simplellmtool;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.VideoContent;
import fr.mazure.simplellmtool.CommandLine.Attachment;
import fr.mazure.simplellmtool.CommandLine.AttachmentSource;

/*
 * AttachmentManager is a class that manages attachments
 */
public class AttachmentManager {

    private enum AttachmentType {
        IMAGE,
        AUDIO,
        VIDEO,
        PDF
    }

    public static List<Content> getAttachmentsContent(final List<Attachment> attachments) throws AttachmentManagerException {
        try {
            return attachments.stream()
                            .map(attachment -> {
                                try {
                                    return ((attachment.source() == AttachmentSource.FILE) ? getFileContent(Paths.get(attachment.path()))
                                                                                           : getUriContent(new URI(attachment.path())));
                                } catch (final AttachmentManagerException|URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());
        } catch (final RuntimeException e) {
            final Throwable exception = e.getCause();
            if (exception instanceof AttachmentManagerException attachmentManagerException) {
                throw attachmentManagerException;
            } else if (exception instanceof URISyntaxException uriSyntaxException) {
                throw new AttachmentManagerException("Invalid URL: " + uriSyntaxException.getMessage());
            } else {
                throw new IllegalStateException(exception);
            }
        }
    }

    /*
     * Returns the content of a file
     * 
     * @param filePath The path of the file
     * 
     * @return The content of the file
     * 
     * @throws AttachmentManagerException If the file is not a supported type or
     * 
     */
    public static Content getFileContent(final Path filePath) throws AttachmentManagerException {
        final String fileExtension = getExtension(filePath.toString() );
        final AttachmentType type = getAttachmentType(fileExtension);

        return switch (type) {
            case AttachmentType.IMAGE -> getImageFileContent(filePath, fileExtension);
            case AttachmentType.AUDIO -> getAudioFileContent(filePath, fileExtension);
            case AttachmentType.VIDEO -> getVideoFileContent(filePath, fileExtension);
            case AttachmentType.PDF -> getPdfFileContent(filePath);
        };
    }

    /*
     * Returns the content of a URI
     * 
     * @param uri The URI of the file
     * 
     * @return The content of the file
     * 
     * @throws AttachmentManagerException If the URL is not a supported type
     */
    public static Content getUriContent(final URI uri) throws AttachmentManagerException {
        final String fileExtension = getExtension(uri.getPath());
        final AttachmentType type = getAttachmentType(fileExtension);

        return switch (type) {
            case AttachmentType.IMAGE -> ImageContent.from(uri);
            case AttachmentType.AUDIO -> AudioContent.from(uri);
            case AttachmentType.VIDEO -> VideoContent.from(uri);
            case AttachmentType.PDF -> PdfFileContent.from(uri);
        };
    }

    /*
     * Returns the type of an attachment
     * 
     * @param extension The extension of the file
     * 
     * @return The type of the attachment
     * 
     * @throws AttachmentManagerException If the file is not a supported type
     */
    private static AttachmentType getAttachmentType(final String extension) throws AttachmentManagerException {
        return switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> AttachmentType.IMAGE;
            case "mp3" -> AttachmentType.AUDIO;
            case "mp4" -> AttachmentType.VIDEO;
            case "pdf" -> AttachmentType.PDF;
            default -> throw new AttachmentManagerException("Unsupported filename extension: " + extension);
        };
    }

    /*
     * Returns the content of an image file
     * 
     * @param filePath The path of the file
     * @param fileExtension The extension of the file
     * 
     * @return The content of the file
     * 
     * @throws AttachmentManagerException If the file cannot be read
     */
    private static Content getImageFileContent(final Path filePath,
                                               String fileExtension) throws AttachmentManagerException {
        if (fileExtension.equals("jpg")) {
            fileExtension = "jpeg";
        }
        final String base64Data = getFileContentAsBase64(filePath);
        return ImageContent.from(base64Data, "image/" + fileExtension);
    }

    /*
     * Returns the content of an audio file
     * 
     * @param filePath The path of the file
     * @param fileExtension The extension of the file
     * 
     * @return The content of the file
     * 
     * @throws AttachmentManagerException If the file cannot be read
     */
    private static Content getAudioFileContent(final Path filePath,
                                               String fileExtension) throws AttachmentManagerException {
        final String base64Data = getFileContentAsBase64(filePath);
        return AudioContent.from(base64Data, "audio/" + fileExtension);
    }


    /*
     * Returns the content of a video file
     * 
     * @param filePath The path of the file
     * @param fileExtension The extension of the file
     * 
     * @return The content of the file
     * 
     * @throws AttachmentManagerException If the file cannot be read
     */
    private static Content getVideoFileContent(final Path filePath,
                                               String fileExtension) throws AttachmentManagerException {
        final String base64Data = getFileContentAsBase64(filePath);
        return VideoContent.from(base64Data, "video/" + fileExtension);
    }

    /*
     * Returns the content of a PDF file
     * 
     * @param filePath The path of the file
     * 
     * @return The content of the file
     * 
     * @throws AttachmentManagerException If the file cannot be read
     */
    private static Content getPdfFileContent(final Path filePath) throws AttachmentManagerException {
        final String base64Data = getFileContentAsBase64(filePath);
        return PdfFileContent.from(base64Data, "application/pdf");
    }

    /*
     * Returns the extension of a filename or URL
     * 
     * @param filename The filename or URL
     * 
     * @return The extension of the filename or URL
     */
    private static String getExtension(final String filename) {
        return filename.toString().substring(filename.toString().lastIndexOf('.') + 1).toLowerCase();
    }

    /*
     * Returns the content of a file as a base64 string
     * 
     * @param filePath The path of the file
     * 
     * @return The content of the file as a base64 string
     * 
     * @throws AttachmentManagerException If the file cannot be read
     */
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
