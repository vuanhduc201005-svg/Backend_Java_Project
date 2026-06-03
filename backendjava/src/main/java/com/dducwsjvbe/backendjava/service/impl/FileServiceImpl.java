package com.dducwsjvbe.backendjava.service.impl;

import com.dducwsjvbe.backendjava.dto.Dto.ProductDto;
import com.dducwsjvbe.backendjava.dto.response.FileUploadResponse;
import com.dducwsjvbe.backendjava.dto.response.PageResponse;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.model.Product;
import com.dducwsjvbe.backendjava.repository.interfaces.FileRepository;
import com.dducwsjvbe.backendjava.repository.interfaces.ProductRepository;
import com.dducwsjvbe.backendjava.repository.interfaces.SearchRepository;
import com.dducwsjvbe.backendjava.repository.interfaces.UserRepository;
import com.dducwsjvbe.backendjava.service.ProductSearchCacheService;
import com.dducwsjvbe.backendjava.service.interfaces.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j(topic = "File-Service-Impl")
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.temp.path:./temp}")
    private String tempPath;

    private final FileRepository fileRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProductSearchCacheService cacheService;
    private final Map<String, UploadProgressTracker> uploadProgress = new ConcurrentHashMap<>();

    @Override
    public ResponseData<?> saveChunk(String fileId, String fileName, int chunkIndex, int totalChunks, MultipartFile file, String productName, String productTopic, String username, Long productId) throws IOException, RuntimeException {
        log.info("In FileServiceImpl.saveChunk={},{},{}",chunkIndex ,totalChunks,productName);
        if (!fileId.matches("^[a-zA-Z0-9_-]+$") || !fileName.matches("^[a-zA-Z0-9_-]+\\.[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("Invalid file id format: " + fileId);
        }
        log.info("Saving chunk {}/{} for file: {}", chunkIndex + 1, totalChunks, fileName);
        //put fileId nếu chưa tồn tại=putIfAbsent tránh việc cả 3 chunk vào 1 lúc và tại ra 3 fileId
        uploadProgress.putIfAbsent(fileId, new UploadProgressTracker(fileId, fileName, totalChunks));
        UploadProgressTracker tracker = uploadProgress.get(fileId);
        // create object ./temp/fileId
        Path tempDir = Paths.get(tempPath, fileId);
        //create file ./temp/fileId
        Files.createDirectories(tempDir);
        // create object temp/fileid/chunk_0123
        Path chunkPath = tempDir.resolve("chunk_" + chunkIndex);
        //ghi dl của chunk(mảnh nhỏ chia ra từ file) và lưu vào cái đg dẫn chunkPath
        Files.write(chunkPath, file.getBytes());
        //đánh dấu chunk đã upload thành công
        tracker.markChunkCompleted(chunkIndex);
        if (!tracker.isComplete()) {
            FileUploadResponse fileUploadResponse = FileUploadResponse.builder()
                    .fileName(fileName)
                    .chunkIndex(chunkIndex)
                    .uploadProgress(tracker.getProgress())
                    .completed(false)
                    .build();
            return new ResponseData<>(
                    HttpStatus.OK.value(),
                    "Chunk " + (chunkIndex + 1) + " uploaded successfully",
                    fileUploadResponse
            );
        }
        //khi nhận đủ chunk thì dùng kafka để ghép,save db,delete temp
        String message = String.format("fileId=%s,fileName=%s,productName=%s,productTopic=%s,username=%s,productId=%s", fileId, fileName, productName, productTopic, username, productId);
        kafkaTemplate.send("async-upload-topic", message);
        // Xóa tracker
        uploadProgress.remove(fileId);
        FileUploadResponse finalResponse = FileUploadResponse.builder()
                .fileName(fileName)
                .chunkIndex(chunkIndex)
                .uploadProgress(100.0)
                .completed(true)
                .build();
        return new ResponseData<>(
                HttpStatus.CREATED.value(),
                "File uploaded successfully",
                finalResponse);
    }

    //search
    @Override
    public PageResponse<List<ProductDto>> searchProduct(Pageable pageable, String[] product, String[] user, UserStatus userStatus) {
        log.info("In FileServiceImpl.searchProduct={}",userStatus);
        // CACHE GET
        PageResponse<List<ProductDto>> cached =
                cacheService.get(pageable, product, user, userStatus);
        if (cached != null) {
            log.info("cache successfully");
            return cached;
        }
        List<ProductDto> fullDataProduct;
        PageResponse<List<ProductDto>> response;
        if (user == null && product == null) {
            Page<Long> productIds = productRepository.findAllProductIds(pageable, userStatus);
            List<Product> products = productRepository.findAllByIds(productIds.getContent(), userStatus);
            fullDataProduct = products.stream().map(listProduct ->
                    ProductDto.builder()
                            .name(listProduct.getName())
                            .topic(listProduct.getTopic())
                            .productId(listProduct.getId())
                            .fileId(listProduct.getFile().getId())
                            .build()
            ).toList();
            response= new PageResponse<>(
                    HttpStatus.OK,
                    "data search",
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    productIds.getTotalPages(),
                    fullDataProduct
            );
        }
        else {
            Page<Product> products = searchRepository.searchProduct(pageable, product, user, userStatus);
            fullDataProduct = products.stream().map(listProduct ->
                    ProductDto.builder()
                            .name(listProduct.getName())
                            .topic(listProduct.getTopic())
                            .productId(listProduct.getId())
                            .fileId(listProduct.getFile().getId())
                            .build()).toList();

            response = new PageResponse<>(
                    HttpStatus.OK,
                    "data search",
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    products.getTotalPages(),
                    fullDataProduct
            );
        }
        // CACHE SET
        cacheService.set(
                pageable,
                product,
                user,
                userStatus,
                response
        );
        return response;
    }

    @Override
    public void upView(Long productId) {
        log.info("In FileServiceImpl.upView={}",productId);
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
        kafkaTemplate.send("up-view-topic", productId.toString());
    }

    @Override
    public void approveProduct(Long productId, String message, String status, String username) {
        log.info("In FileServiceImpl.approveProduct={}",productId);
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty() || !userRepository.existsByUsername(username)) {
            throw new RuntimeException("Product or username admin not found");
        } else {
            if (status != null) {
                if (status.equals("ACTIVE") || status.equals("active")) {
                    product.get().setType(UserStatus.ACTIVE);
                    productRepository.save(product.get());
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
                                            UserStatus.ACTIVE
                                    );

                                    log.info(
                                            "Evicted cache status={}",
                                            UserStatus.ACTIVE
                                    );
                                }
                            }
                    );
                } else if (status.equals("INACTIVE") || status.equals("inactive")) {
                    product.get().setType(UserStatus.INACTIVE);
                    String subject = "INVALID PRODUCT-" + "PRODUCTID=" + productId + "-" + "USERNAME ADMIN=" + username;
                    String messageMail = String.format("recipients=%s,subject=%s,content=%s", product.get().getUser().getEmail(), subject, message);
                    kafkaTemplate.send("up-status-product-topic", messageMail);
                }
            }

        }
    }


    private static class UploadProgressTracker {
        private final String fileId;
        private final String fileName;
        private final int totalChunks;
        private final boolean[] completedChunks;

        public UploadProgressTracker(String fileId, String fileName, int totalChunks) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.totalChunks = totalChunks;
             /*
             vd totalChunk=3
             =>3=boolean[0,1,2]
             * */
            this.completedChunks = new boolean[totalChunks];
        }

        public void markChunkCompleted(int index) {
            if (index < 0 || index >= totalChunks) {
                log.error("invalid chunk index:{},totalChunks:{}", index, totalChunks - 1);
                throw new IllegalArgumentException("invalid chunk index:" + index + "totalChunks:" + (totalChunks - 1));
            }
            //index=0 =>boolean[true false false]
            completedChunks[index] = true;
        }

        //isComplete nếu ko if mặc định là true
        public boolean isComplete() {
            for (boolean completed : completedChunks) {
                //cứ còn false là false
                if (!completed) return false;
            }
            return true;
        }

        public double getProgress() {
            int completed = 0;
            for (boolean c : completedChunks) {
                if (c) completed++;
            }
            return (double) completed / totalChunks * 100;
        }
    }

}