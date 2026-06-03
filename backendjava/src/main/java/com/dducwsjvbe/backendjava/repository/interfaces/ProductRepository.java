package com.dducwsjvbe.backendjava.repository.interfaces;


import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Query 1: Lấy IDs — phân trang đúng ở DB
    @Query("""
SELECT p.id 
FROM Product p
WHERE p.type=:status
""")
    Page<Long> findAllProductIds(Pageable pageable,UserStatus status);
    // Query 2: Fetch data theo IDs
    @Query("""
    SELECT p FROM Product p
    LEFT JOIN FETCH p.user
    LEFT JOIN FETCH p.file
    WHERE p.id IN :ids
    AND p.type = :status
    ORDER BY p.updatedAt DESC
""")
    List<Product> findAllByIds(@Param("ids") List<Long> ids,@Param("status") UserStatus status);
    @Query("SELECT p FROM Product p WHERE p.type=:status ORDER BY p.view DESC")
    Page<Product>findByTopTrending(Pageable pageable,UserStatus status);
}
/*
 @Query("""
                SELECT u
                FROM User u
                LEFT JOIN FETCH u.roles ur
                LEFT JOIN FETCH ur.role r
                LEFT JOIN FETCH r.permissions rp
                LEFT JOIN FETCH rp.permission
                WHERE u.username = :username
            """)
 */