package flab.Linkedlog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final S3Service s3Service;

    @Value("${profile.default-image-url}")
    private String defaultProfileImage;

    @Value("${profile.default-folder-path}")
    private String defaultProfilepath;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${post-image.default-folder-path}")
    private String postImagePath;

    @Async
    public CompletableFuture<String> uploadProfileImageAsync(MultipartFile profileImage) throws IOException {
        long startTime = System.currentTimeMillis();

        String imageUrl = s3Service.uploadFile(profileImage, bucketName, defaultProfilepath);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Finished uploading profile image. Duration: {} ms, Thread: {}",
                duration, Thread.currentThread().getName());

        return CompletableFuture.completedFuture(imageUrl);
    }

    @Async("imageUploadExecutor")
    public CompletableFuture<String> uploadPostImageAsync(MultipartFile image) throws IOException {
        long startTime = System.currentTimeMillis();

        String imageUrl = s3Service.uploadFile(image, bucketName, postImagePath);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Finished uploading profile image. Duration: {} ms, Thread: {}",
                duration, Thread.currentThread().getName());

        return CompletableFuture.completedFuture(imageUrl);
    }

}
