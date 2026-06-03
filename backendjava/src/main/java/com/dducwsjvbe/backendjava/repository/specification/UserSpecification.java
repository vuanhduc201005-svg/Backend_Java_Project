package com.dducwsjvbe.backendjava.repository.specification;

import com.dducwsjvbe.backendjava.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import static com.dducwsjvbe.backendjava.repository.specification.SearchOperation.EQUALITY;

public class UserSpecification implements Specification<User> {
    private SpecSearchCriteria criteria;

    public UserSpecification(SpecSearchCriteria specSearchCriteria) {
        this.criteria = specSearchCriteria;
    }

    @Override
    public @Nullable Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return switch (criteria.getOperation()) {
            //equal
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            //not aqual
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            //>
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            //<
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            // like
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            // bắt đầu =
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            // kết thúc =
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            // ==
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }
}
/*
class convert từ -> query
toPredicate được gọi nội bộ jpa khi:
 Specification<User>specification=new UserSpecification(param.get(0)); gọi nội bộ lần đầu
        for (int i=1;i<param.size();i++) {
            specification=param.get(i).getOrPredicate()
                    ?Specification.where(specification).or(new UserSpecification(param.get(i))) gọi nối bộ lần i
                    :Specification.where(specification).and(new UserSpecification(param.get(i))); gọi nối bộ lần i
        }
        return specification;
 */