package com.dducwsjvbe.backendjava.service;

import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.exception.UserNotFoundException;
import com.dducwsjvbe.backendjava.model.File;
import com.dducwsjvbe.backendjava.model.Product;
import com.dducwsjvbe.backendjava.model.User;
import com.dducwsjvbe.backendjava.repository.interfaces.FileRepository;
import com.dducwsjvbe.backendjava.repository.interfaces.ProductRepository;
import com.dducwsjvbe.backendjava.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j(topic = "AsyncKafka-Service")
@RequiredArgsConstructor
public class AsyncKafkaService {
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.temp.path:./temp}")
    private String tempPath;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final ProductSearchCacheService cacheService;


    @KafkaListener(topics = "async-upload-topic", groupId = "async-upload-group")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void asyncUpload(String message) throws IOException, RuntimeException {
        log.info("async-upload-message={}",message);
        String[] arr = message.split(",");
        String fileId = arr[0].substring(arr[0].indexOf('=') + 1);
        String fileName = arr[1].substring(arr[1].indexOf('=') + 1);
        String productName = arr[2].substring(arr[2].indexOf('=') + 1);
        String productTopic = arr[3].substring(arr[3].indexOf('=') + 1);
        String username = arr[4].substring(arr[4].indexOf('=') + 1);
        String productId = arr[5].substring(arr[5].indexOf('=') + 1);
        String finalPath = mergeChunks(fileId, fileName);
        long fileSize = getFileSize(finalPath);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("user unavailable");
        }
        // Lưu vào database
        com.dducwsjvbe.backendjava.model.File uploadedFiles;
        Optional<Product> product = Optional.empty();
        if (productId != null && productId.matches("\\d+")) {
            product = productRepository.findById(Long.parseLong(productId));
        }
        if (product.isEmpty()) {
            uploadedFiles = com.dducwsjvbe.backendjava.model.File.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .filePath(finalPath)
                    .fileSize(fileSize)
                    .status(UserStatus.INACTIVE)
                    .build();
            fileRepository.save(uploadedFiles);
            productRepository.save(Product.builder()
                    .name(productName)
                    .topic(productTopic)
                    .user(user)
                    .file(uploadedFiles)
                    .type(UserStatus.INACTIVE)
                    .build());

        } else {
//            uploadedFiles =File.builder()
//                    .fileId(fileId)
//                    .fileName(fileName)
//                    .filePath(finalPath)
//                    .fileSize(fileSize)
//                    .status(UserStatus.INACTIVE)
//                    .build();
            Product p = product.get();
//            p.setFile(uploadedFiles);
            File existingFile = p.getFile();
            //xóa file cũ
            Path oldFilePath = Paths.get(existingFile.getFilePath());
            if (Files.exists(oldFilePath)) {
                Files.delete(oldFilePath);
                log.info("Deleted old file: {}", oldFilePath);
            }
            existingFile.setFileId(fileId);
            existingFile.setFilePath(finalPath);
            existingFile.setFileSize(fileSize);
            existingFile.setFileName(fileName);
            existingFile.setStatus(UserStatus.INACTIVE);
            fileRepository.save(existingFile);
            p.setName(productName);
            p.setTopic(productTopic);
            productRepository.save(product.get());
        }
        log.info("File uploaded and saved to DB: {}", fileName);
        // =========================
        // CACHE EVICT
        // ONLY INACTIVE CACHE
        // AFTER TRANSACTION COMMIT
        // =========================
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        cacheService.evictByStatus(
                                UserStatus.INACTIVE
                        );

                        log.info(
                                "Evicted cache status={}",
                                UserStatus.INACTIVE
                        );
                    }
                }
        );
        Path tempDir = Paths.get(tempPath, fileId);
        deleteDirectory(tempDir);
    }

    private String mergeChunks(String fileId, String fileName) throws IOException {
        log.info("Merging chunks for file: {}", fileId);
        //tempDir=./temp/fileId
        Path tempDir = Paths.get(tempPath, fileId);
        //uploadDir=./uploads
        Path uploadDir = Paths.get(uploadPath);
        //tạo director=uploadDir
        Files.createDirectories(uploadDir);

        // Tạo tên file final với timestamp
        String finalFileName = System.currentTimeMillis() + "_" + fileName;
        //finalPath=./uploads/System.currentTimeMillis()_fileName
        Path finalPath = uploadDir.resolve(finalFileName);

        // Ghép file
        //mở luồng ghi đến file đích
        try (OutputStream os = new FileOutputStream(finalPath.toFile())) {
            //lấy all in file trong temp có name=chunk_
            java.io.File[] chunks = tempDir.toFile().listFiles((dir, name) -> name.startsWith("chunk_"));

            if (chunks != null) {
                // Sắp xếp chunks theo thứ tự
                Arrays.sort(chunks, (a, b) -> {
                    int numA = Integer.parseInt(a.getName().split("_")[1]);
                    int numB = Integer.parseInt(b.getName().split("_")[1]);
                    return Integer.compare(numA, numB);
                });
//đọc từng chunk đưa vào FileInputStream rồi copy vào file OutputStream
                for (java.io.File chunk : chunks) {
                    try (FileInputStream fis = new FileInputStream(chunk)) {
                        IOUtils.copy(fis, os);
                    }
                }
            }
        }

        log.info("File merged successfully: {}", finalPath);
        return finalPath.toString();
    }

    private long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Error getting file size: {}", e.getMessage());
            return 0;
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Error deleting: {}", e.getMessage());
                        }
                    });
        }
    }

    @KafkaListener(topics = "up-view-topic", groupId = "up-view-group")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void asyncUpView(String productId) throws RuntimeException {
        log.info("async-up-view-message={}",productId);
        Optional<Product> product = productRepository.findById(Long.parseLong(productId));
        if (product.isEmpty()) {
            throw new RuntimeException("product not found");
        }
        product.get().setView(product.get().getView() == null ? 1L : product.get().getView() + 1L);
        productRepository.save(product.get());
    }

}
