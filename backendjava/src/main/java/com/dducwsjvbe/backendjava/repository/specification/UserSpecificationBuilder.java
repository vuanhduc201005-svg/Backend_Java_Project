package com.dducwsjvbe.backendjava.repository.specification;

import com.dducwsjvbe.backendjava.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.dducwsjvbe.backendjava.repository.specification.SearchOperation.ZERO_OR_MORE_REGEX;

public class UserSpecificationBuilder {
    private final List<SpecSearchCriteria> param;

    public UserSpecificationBuilder() {
        this.param = new ArrayList<>();
    }

    public UserSpecificationBuilder with(String key, String operation, Object value, String prefix, String suffix) {
      return with(null, key, operation, value, prefix, suffix);

    }

    public UserSpecificationBuilder with(String orPredicate, String key, String operation, Object value, String prefix, String suffix) {
        SearchOperation oper = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (oper == SearchOperation.EQUALITY) {
            boolean starWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
            boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);
            if (starWithAsterisk && endWithAsterisk) {
                oper = SearchOperation.CONTAINS;
            } else if (starWithAsterisk) {
                oper = SearchOperation.ENDS_WITH;
            } else if (endWithAsterisk) {
                oper = SearchOperation.STARTS_WITH;
            }
        }
        param.add(new SpecSearchCriteria(orPredicate, key, oper, value));
        return this;
    }
public Specification<User>build() {
        if (param.isEmpty()) {
            return null;
        }
        Specification<User>specification=new UserSpecification(param.get(0));
        for (int i=1;i<param.size();i++) {
            specification=param.get(i).getOrPredicate()
                    ?Specification.where(specification).or(new UserSpecification(param.get(i)))
                    :Specification.where(specification).and(new UserSpecification(param.get(i)));
        }
        return specification;
}
}
/*
prefix:tiền tố
subfix:hậu tố
orPredicate=operation+xử lý logic
 */