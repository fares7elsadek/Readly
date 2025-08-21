package com.fares_elsadek.Readly.services.uploadfiles;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadFilesService {

    public String saveFile(MultipartFile file) throws IOException;
    public byte[] loadFile(String filename) throws IOException;

}
