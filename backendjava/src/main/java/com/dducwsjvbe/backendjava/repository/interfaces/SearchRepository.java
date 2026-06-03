package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.dto.response.PageResponse;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.model.Address;
import com.dducwsjvbe.backendjava.model.Product;
import com.dducwsjvbe.backendjava.model.User;
import com.dducwsjvbe.backendjava.repository.SearchCriteria;
import com.dducwsjvbe.backendjava.repository.specification.SpecSearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchRepository {
    Page<Product> searchProduct(Pageable pageable, String[]product, String[]user, UserStatus status);
 }
