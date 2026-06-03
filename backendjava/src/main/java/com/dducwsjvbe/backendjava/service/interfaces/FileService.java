package com.dducwsjvbe.backendjava.service.interfaces;

import com.dducwsjvbe.backendjava.dto.Dto.ProductDto;
import com.dducwsjvbe.backendjava.dto.response.PageResponse;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;

import com.dducwsjvbe.backendjava.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    @Transactional
    ResponseData<?> saveChunk(String fileId, String fileName, int chunkIndex, int totalChunks, MultipartFile file, String productName,String productTopic,String username,Long productId) throws IOException,RuntimeException;

    PageResponse<List<ProductDto>> searchProduct(Pageable pageable, String[]product, String[]user, UserStatus userStatus) ;
    void upView(Long productId);
    void approveProduct(Long productId,String message,String status,String username) ;

}
