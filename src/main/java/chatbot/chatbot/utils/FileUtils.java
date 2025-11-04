package chatbot.chatbot.utils;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

public final class FileUtils {

    private static final String[] ALLOWED_EXTENSIONS = { "md", "txt" };

    private static Path getRawDataDirectory() {
        return Path.of("raw_data");
    }

    public static boolean isRawDataDirectoryExists() {
        return Files.exists(getRawDataDirectory());
    }

    public static void createRawDataDirectoryIfNotExists() throws IOException {
        Path rawDataDir = getRawDataDirectory();
        if (!Files.exists(rawDataDir)) {
            Files.createDirectories(rawDataDir);
        }
    }

    public static List<String> validateMultipartFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file.isEmpty()) {
            errors.add("Empty file: " + file.getOriginalFilename());
            return errors;
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            errors.add("File has no name");
            return errors;
        }

        // Validate file type (only .md and .txt files)
        if (!isAllowedFileType(originalFileName)) {
            errors.add("Invalid file type for " + originalFileName + ". Only " + String.join(", ", ALLOWED_EXTENSIONS) + " files are allowed.");
            return errors;
        }

        return errors;
    }

    public static boolean saveMultipartFile(MultipartFile file, List<String> errors) {
        try {
            createRawDataDirectoryIfNotExists();
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new IOException("File has no name");
            }

            Path targetLocation = getRawDataDirectory().resolve(originalFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            errors.add("Failed to upload files: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static List<Map<String, Object>> getFileInfos() {
        List<Map<String, Object>> result = new ArrayList<>();

        Path rawDataDir = getRawDataDirectory();

        if (!isRawDataDirectoryExists()) {
            return result;
        }

        try {
            Files.list(rawDataDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getFileName().toString());
                        fileInfo.put("size", Files.size(file));
                        fileInfo.put("lastModified", Files.getLastModifiedTime(file).toString());
                        fileInfo.put("type", getFileExtension(file.getFileName().toString()));
                        result.add(fileInfo);
                    } catch (IOException e) {
                        // Skip files that can't be read
                    }
                });
        } catch (IOException e) {
            return new ArrayList<>();
        }

        return result;
    }

    /**
     * Helper method to extract file extension
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public static boolean isAllowedFileType(String fileName) {
        String extension = getFileExtension(fileName);
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllowedFileType(Path filePath) {
        String extension = getFileExtension(filePath.getFileName().toString());
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean deleteFile(String fileName, List<String> errors) {
        Path filePath = getRawDataDirectory().resolve(fileName);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            } else {
                errors.add("File not found: " + fileName);
                return false;
            }
        } catch (IOException e) {
            errors.add("Failed to delete file " + fileName + ": " + e.getMessage());
            return false;
        }
    }
}
