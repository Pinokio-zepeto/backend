package com.example.pinokkio.api.item.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.pinokkio.exception.domain.image.ImageBadInputException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageService {

    @Value("${default-image}")
    private String DEFAULT_IMAGE_URL;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${s3.host.name}")
    private String hostName;

    private final AmazonS3 amazonS3;
    private final AmazonS3Client amazonS3Client;
    private static final String IMAGE_FILE_TYPE = "image";

    @Transactional
    public String uploadImage(MultipartFile multipartFile) {

        String URL = "item/" + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        String contentType = multipartFile.getContentType();
        validate(URL, contentType);

        ObjectMetadata obj = new ObjectMetadata();
        obj.setContentLength(multipartFile.getSize());
        obj.setContentType(multipartFile.getContentType());

        try {
            this.amazonS3.putObject(this.bucketName, URL, multipartFile.getInputStream(), obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return amazonS3.getUrl(bucketName, URL).toString();
    }

    @Transactional
    public void deleteImage(String filePath) {

        if (filePath.equals(DEFAULT_IMAGE_URL)) {
            return;
        }

        if (!filePath.startsWith(this.hostName)) {
            throw new RuntimeException();
        } else {
            String decodeURL = this.decodeURL(filePath);
            boolean isObjectExist = this.amazonS3Client.doesObjectExist(this.bucketName, decodeURL);
            log.info("Delete fileUrl={}", decodeURL);
            if (isObjectExist) {
                this.amazonS3Client.deleteObject(this.bucketName, decodeURL);
            } else {
                log.error("doesn't exist in S3 bucket");
                throw new RuntimeException();
            }
        }
    }

    private String decodeURL(String filePath) {
        return URLDecoder
                .decode(filePath.replace(hostName, "")
                        .replaceAll("\\p{Z}", ""), StandardCharsets.UTF_8);
    }

    private void validate(String URL, String contentType) {
        if (contentType == null || !contentType.startsWith(IMAGE_FILE_TYPE)) {
            throw new ImageBadInputException(URL);
        }
    }

}