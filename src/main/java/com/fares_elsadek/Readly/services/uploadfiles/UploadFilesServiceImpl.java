package com.fares_elsadek.Readly.services.uploadfiles;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadFilesServiceImpl implements UploadFilesService{

    private final Path root = Paths.get("uploads");

    public UploadFilesServiceImpl() throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
    }
    @Override
    public String saveFile(MultipartFile file) throws IOException {
        var filename = file.getOriginalFilename() + "-" + UUID.randomUUID();
        Path filePath = root.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    @Override
    public byte[] loadFile(String filename) throws IOException {
        Path filePath = root.resolve(filename);
        return Files.readAllBytes(filePath);
    }
}
