package com.webdev.bloggingsystem.s3Stuff;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final UploadedImgDao uploadedImgDao;
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    public S3Service(S3Client s3Client, UploadedImgDao uploadedImgDao) {
        this.s3Client = s3Client;
        this.uploadedImgDao = uploadedImgDao;
    }

    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.s3.cdn-url}")
    private String cdnUrl;

    public String uploadImage(String folder, MultipartFile file, int postId) {
        String key = "images/" + folder + "/" + file.getOriginalFilename();
        logger.info("Uploading image to S3 bucket {} at {}", bucket, key);

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new BlogEntryException("Error while uploading file");
        }

        String fullUrl = cdnUrl + "/" + key;
        UploadedImg imgRecord = new UploadedImg(postId, fullUrl);

        try {
            uploadedImgDao.insert(imgRecord);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BlogEntryException("Error while persisting url, check log");
        }

        return fullUrl;
    }

    public List<UploadedImg> findAllByPostId(int postId) {
        return uploadedImgDao.findAllByPostId(postId);
    }
}
