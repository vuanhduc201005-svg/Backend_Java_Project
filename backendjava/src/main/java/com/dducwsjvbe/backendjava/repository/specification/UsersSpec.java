package com.dducwsjvbe.backendjava.repository.specification;

import com.dducwsjvbe.backendjava.enums.Gender;
import com.dducwsjvbe.backendjava.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

public class UsersSpec {
    public static Specification<User> hasFirstName(String firstName) {
        return new  Specification<User>() {
            @Override
            public  Predicate toPredicate(
                    Root<User> root,
                    CriteriaQuery<?> query,
                    CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.like(root.get("firstName"), "%"+firstName+"%");
            }
        };
    }
    public static Specification<User> notEqualGender(Gender gender) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("gender"), gender);
    }
}
