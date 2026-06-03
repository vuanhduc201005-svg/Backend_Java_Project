package com.dducwsjvbe.backendjava.controller;

import com.dducwsjvbe.backendjava.dto.Dto.ProductDto;
import com.dducwsjvbe.backendjava.dto.response.PageResponse;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.model.File;
import com.dducwsjvbe.backendjava.model.Product;
import com.dducwsjvbe.backendjava.repository.interfaces.FileRepository;
import com.dducwsjvbe.backendjava.repository.interfaces.ProductRepository;
import com.dducwsjvbe.backendjava.service.interfaces.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/files")
@Slf4j(topic = "File-Controller")
@CrossOrigin(origins = "*")
@Tag(name="File-Controller")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProductRepository productRepository;
    private final FileRepository fileRepository;

    //ADMIN-POST

    @Operation(method = "POST", summary = "upload", description = "create new products")
    @PostMapping("/upload-chunk")
    public ResponseData<?> uploadChunk(
            @RequestParam(value = "fileId") String fileId,
            @RequestParam("fileName") String fileName,
            @RequestParam("productName") String productName,
            @RequestParam("productTopic") String productTopic,
            @RequestParam("username") String username,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("file") MultipartFile file) throws IOException, RuntimeException {
        log.info("upload chunk");
        return fileService.saveChunk(fileId, fileName, chunkIndex, totalChunks, file, productName, productTopic, username, null);
    }

    @Operation(method = "GET", summary = "search file upload", description = "View my products")
    @GetMapping("/file-upload")
    public PageResponse<?> searchFileOfUserAdminPost(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "20", required = false) @Min(10) int pageSize,
            @RequestParam(required = false) String[] product,
            @RequestParam("user") String usernameAdminPost,
            @RequestParam(required = false) Long productId,
            @RequestParam String status) {
        log.info("search file upload");
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedAt").descending());
        String[] user = {"username:" + usernameAdminPost};
        if (status.equals(UserStatus.ACTIVE.name())) {
            return fileService.searchProduct(pageable, product, user, UserStatus.ACTIVE);
        }
        return fileService.searchProduct(pageable, product, user, UserStatus.INACTIVE);
    }

    @Operation(method = "PATH", summary = "update file upload", description = "Edit my product")
    @PatchMapping("/update-product")
    public ResponseData<?> updateProduct(
            @RequestParam(value = "fileId") String fileId,
            @RequestParam(value = "fileName") String fileName,
            @RequestParam(value = "productId") Long productId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "productTopic", required = false) String productTopic,
            @RequestParam("username") String username,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("file") MultipartFile file
    ) throws IOException, RuntimeException {
        log.info("update file upload");
        return fileService.saveChunk(fileId, fileName, chunkIndex, totalChunks, file, productName, productTopic, username, productId);
    }

    //ADMIN-UPDATE

    @Operation(method = "GET", summary = "search inactive", description = "Search for the product you want to browse")
    @GetMapping("/product-inactive")
    public PageResponse<?> inactiveProduct(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                           @RequestParam(defaultValue = "20", required = false) @Min(10) int pageSize,
                                           @RequestParam(required = false) String[] product) {
        log.info("search inactive product");
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedAt").descending());
        return fileService.searchProduct(pageable, product, null, UserStatus.INACTIVE);
    }

    @Operation(method = "PATH", summary = "approve product", description = "browse products")
    @PatchMapping("/approve-product/{productId}")
    public void approveProduct(@PathVariable @Min(1) Long productId,
                               @RequestParam(value = "message", required = false) String message,
                               @RequestParam String status, @RequestParam String username) {
        log.info("approve product");
        fileService.approveProduct(productId, message, status, username);
    }

    //USER

    @Operation(method = "GET", summary = "search product", description = "search for products")
    @GetMapping("/search-file")
    public PageResponse<List<ProductDto>> searchFileActive(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "20", required = false) @Min(10) int pageSize,
            @RequestParam(required = false) String[] product,
            @RequestParam(required = false) String[] user
    ) {
        log.info("search product");
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedAt").descending());
        return fileService.searchProduct(pageable, product, user, UserStatus.ACTIVE);
    }

    @Operation(method = "GET", summary = "search top trending", description = "Search for trending products")
    @GetMapping("/product-trending")
    public PageResponse<?> productTrending() {
        log.info("search top trending");
        Pageable pageable = PageRequest.of(0, 3, Sort.by("view").descending());
        Page<Product> topTrending = productRepository.findByTopTrending(pageable, UserStatus.ACTIVE);
        if (topTrending == null) {
            return new PageResponse<>(
                    HttpStatus.CREATED,
                    "product trending unavailable",
                    0,
                    3,
                    0,
                    null
            );
        }
        List<ProductDto> products = topTrending.stream().map(listProduct ->
                ProductDto.builder()
                        .name(listProduct.getName())
                        .topic(listProduct.getTopic())
                        .productId(listProduct.getId())
                        .fileId(listProduct.getFile().getId())
                        .build()
        ).toList();
        return new PageResponse<>(
                HttpStatus.CREATED,
                "data trending",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                topTrending.getTotalPages(),
                products
        );
    }

    //VIEW
    @Operation(method = "GET", summary = "view", description = "Get the link")
    @GetMapping("/view/{fileId}")
    private ResponseData<?> viewProduct(@PathVariable Long fileId) {
        log.info("view product");
        File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("file unavaiable"));
        String path = file.getFilePath();
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "data",
                path
        );
    }

    //UP VIEW
    @Operation(method = "POST", summary = "up view", description = "Update the viewing angle")
    @PostMapping("/up-view/{productId}")
    private void upView(@PathVariable @Min(1) Long productId) {
        log.info("up view product");
        fileService.upView(productId);
    }

    //REPORT=logging ELK
    @Operation(method = "POST", summary = "report", description = "product report")
    @PostMapping("/report/{productId}")
    private void report(@PathVariable @Min(1) Long productId, @RequestParam String messageReport) {
        log.info("Report productId={},message={}", productId, messageReport);
    }

}
