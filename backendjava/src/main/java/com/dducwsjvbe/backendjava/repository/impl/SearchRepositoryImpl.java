package com.dducwsjvbe.backendjava.repository.impl;

import com.dducwsjvbe.backendjava.dto.response.PageResponse;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import com.dducwsjvbe.backendjava.model.Product;
import com.dducwsjvbe.backendjava.model.User;
import com.dducwsjvbe.backendjava.repository.interfaces.SearchRepository;
import com.dducwsjvbe.backendjava.repository.specification.SpecSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
@Slf4j
public class SearchRepositoryImpl implements SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> searchProduct(Pageable pageable, String[] product, String[] user,UserStatus userStatus) {
        log.info("search product request={}",userStatus);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates  = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+?)([:<>~!])(\\p{Punct}?)(.*?)(\\p{Punct}?)$");
        if(product!=null){
            for (String p : product) {
                Matcher matcher = pattern.matcher(p);
                if (matcher.find()) {
                    SpecSearchCriteria criteria = new SpecSearchCriteria(
                            matcher.group(1),
                            matcher.group(2),
                            matcher.group(4),
                            matcher.group(3),
                            matcher.group(5)
                    );
                    predicates.add(toProductPredicate(root, criteriaBuilder, criteria)) ;
                }
            }
        }
        predicates.add(
                criteriaBuilder.equal(root.get("type"), userStatus)
        );
        if (user != null) {
            Join<User, Product> join = root.join("user");
            List<Predicate> userPre = new ArrayList<>();
            for (String u : user) {
                Matcher matcher = pattern.matcher(u);
                if (matcher.find()) {
                    SpecSearchCriteria criteria = new SpecSearchCriteria(
                            matcher.group(1),
                            matcher.group(2),
                            matcher.group(4),
                            matcher.group(3),
                            matcher.group(5)
                    );
                    predicates.add(toUserPredicate(join, criteriaBuilder, criteria));
                }
            }
        }
        if (!predicates.isEmpty()) {
            query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }
              List<Product>resultList=entityManager
                    .createQuery(query)
                    .setFirstResult(pageable.getPageNumber())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        // ===== COUNT QUERY =====
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(criteriaBuilder.count(countRoot));

        if (!predicates.isEmpty()) {
            List<Predicate> countPredicates = new ArrayList<>();
            if (product != null) {
                for (String p : product) {
                    Matcher matcher = pattern.matcher(p);
                    if (matcher.find()) {
                        SpecSearchCriteria criteria = new SpecSearchCriteria(
                                matcher.group(1),
                                matcher.group(2),
                                matcher.group(4),
                                matcher.group(3),
                                matcher.group(5));
                        countPredicates.add(toProductPredicate(countRoot, criteriaBuilder, criteria));
                    }
                }
            }
//                countPredicates.add(criteriaBuilder.equal(countRoot.get("type"), userStatus));
            if (user != null) {
                Join<User, Product> countJoin = countRoot.join("user");
                for (String u : user) {
                    Matcher matcher = pattern.matcher(u);
                    if (matcher.find()) {
                        SpecSearchCriteria criteria = new SpecSearchCriteria(
                                matcher.group(1),
                                matcher.group(2),
                                matcher.group(4),
                                matcher.group(3),
                                matcher.group(5));
                        countPredicates.add(toUserPredicate(countJoin, criteriaBuilder, criteria));
                    }
                }
            }
            countQuery.where(criteriaBuilder.and(countPredicates.toArray(new Predicate[0])));
        }

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);

        }
    // Thêm hàm này vào class
    private Object convertValue(Path<?> path, Object value) {
        Class<?> fieldType = path.getJavaType();
        String strValue = value.toString();
//        System.out.println(">>> convertValue: field=" + path + " | type=" + fieldType + " | value=" + strValue);
        if (fieldType == Date.class|| fieldType == Timestamp.class) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strValue);
            } catch (ParseException e) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(strValue);
                } catch (ParseException ex) {
                    throw new RuntimeException("Invalid date format: " + strValue);
                }
            }
        }
        if (fieldType == Integer.class || fieldType == int.class) return Integer.parseInt(strValue);
        if (fieldType == Long.class || fieldType == long.class) return Long.parseLong(strValue);
        if (fieldType == Double.class || fieldType == double.class) return Double.parseDouble(strValue);
        if (fieldType == Boolean.class || fieldType == boolean.class) return Boolean.parseBoolean(strValue);
        // Enum
        if (fieldType.isEnum()) {
            return Enum.valueOf((Class<Enum>) fieldType, strValue);
        }
        return value; // String giữ nguyên
    }
    public Predicate toProductPredicate(Root<Product> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        Path<?> path = root.get(criteria.getKey());
        Object converted = convertValue(path, criteria.getValue());
//        System.out.println(">>> key=" + criteria.getKey() + " | op=" + criteria.getOperation() + " | value=" + criteria.getValue());
        return switch (criteria.getOperation()) {
            //equal
            case EQUALITY   -> builder.equal(path, converted);
            case NEGATION   -> builder.notEqual(path, converted);
            case GREATER_THAN -> builder.greaterThan((Path<Comparable>) path, (Comparable) converted);
            case LESS_THAN  -> builder.lessThan((Path<Comparable>) path, (Comparable) converted);
            case LIKE       -> builder.like((Path<String>) path, "%" + criteria.getValue() + "%");
            case STARTS_WITH -> builder.like((Path<String>) path, criteria.getValue() + "%");
            case ENDS_WITH  -> builder.like((Path<String>) path, "%" + criteria.getValue());
            case CONTAINS   -> builder.like((Path<String>) path, "%" + criteria.getValue() + "%");
        };
    }
    public Predicate toUserPredicate(Join<User, Product> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        Path<?> path = root.get(criteria.getKey());
        Object converted = convertValue(path, criteria.getValue());
//        System.out.println(">>> key=" + criteria.getKey() + " | op=" + criteria.getOperation() + " | value=" + criteria.getValue());

        return switch (criteria.getOperation()) {
            case EQUALITY   -> builder.equal(path, converted);
            case NEGATION   -> builder.notEqual(path, converted);
            case GREATER_THAN -> builder.greaterThan((Path<Comparable>) path, (Comparable) converted);
            case LESS_THAN  -> builder.lessThan((Path<Comparable>) path, (Comparable) converted);
            case LIKE       -> builder.like((Path<String>) path, "%" + criteria.getValue() + "%");
            case STARTS_WITH -> builder.like((Path<String>) path, criteria.getValue() + "%");
            case ENDS_WITH  -> builder.like((Path<String>) path, "%" + criteria.getValue());
            case CONTAINS   -> builder.like((Path<String>) path, "%" + criteria.getValue() + "%");
        };
    }
}