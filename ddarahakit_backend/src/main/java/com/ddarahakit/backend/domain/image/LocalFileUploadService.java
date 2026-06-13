package com.ddarahakit.backend.domain.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.ddarahakit.backend.common.Constants.PROFILE_IMAGE_UPLOAD_PATH;
import static com.ddarahakit.backend.common.Constants.QNA_IMAGE_UPLOAD_PATH;
import static com.ddarahakit.backend.common.Constants.COMMUNITY_IMAGE_UPLOAD_PATH;

@Service
public class LocalFileUploadService implements FileUploadService {


    @Value("${project.upload.path}")
    private String defaultUploadPath;


    public String makeFolder(String uploadPath) {

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String folderPath = date.replace("/", File.separator);

        File uploadPathFolder = new File(defaultUploadPath + uploadPath + File.separator + folderPath);
        if (!uploadPathFolder.exists()) {
            if(uploadPathFolder.mkdirs()) {
                return File.separator + folderPath;
            }
        }

        return File.separator + folderPath;
    }

    public String saveFile(MultipartFile file, String uploadPath) {
        String originalName = file.getOriginalFilename();

        String folderPath = makeFolder(uploadPath);

        String saveFileName = uploadPath + folderPath + File.separator + UUID.randomUUID() + "_" + originalName;
        File saveFile = new File(defaultUploadPath + File.separator + saveFileName);

        try {
            file.transferTo(saveFile);
            return saveFileName.replace(File.separator, "/");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public ResponseEntity<byte[]> display(String fileName) {
        File file = new File(defaultUploadPath + "\\" + fileName);

        ResponseEntity<byte[]> result = null;

        try {

            HttpHeaders header = new HttpHeaders();

        /*
        Files.probeContentType() 해당 파일의 Content 타입을 인식(image, text/plain ...)
        없으면 null 반환

        file.toPath() -> file 객체를 Path객체로 변환

        */
            header.add("Content-type", Files.probeContentType(file.toPath()));

            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public String uploadProfile(MultipartFile file) {
        return saveFile(file, PROFILE_IMAGE_UPLOAD_PATH);
    }

    @Override
    public String uploadQnaImage(MultipartFile file) {
        return saveFile(file, QNA_IMAGE_UPLOAD_PATH);
    }

    @Override
    public String uploadCommunityImage(MultipartFile file) {
        return saveFile(file, COMMUNITY_IMAGE_UPLOAD_PATH);
    }
}
