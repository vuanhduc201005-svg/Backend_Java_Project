package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("""
                SELECT u
                FROM User u
                LEFT JOIN FETCH u.roles ur
                LEFT JOIN FETCH ur.role r
                LEFT JOIN FETCH r.permissions rp
                LEFT JOIN FETCH rp.permission
                WHERE u.username = :username
            """)
    User findByUsername(String username);

    @Query("""
                SELECT u.username,u.email
                FROM User u
                WHERE u.username = :username
                OR u.email=:email
            """)
    User findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameOrEmail(String username, String email);

    User findByEmail(String email);

    boolean existsByEmail(String email);
}
//    //Distinct
/// /    @Query(value = "select distinct u from User u where u.firstName=:firstName and u.lastName=:lastName")
//    List<User> findByUsername(String firstName, String lastName);
//
//    //single field
////    @Query(value = "select * from User u where u.email=?1")
//    List<User> findByEmail(String email);
//
//    //or
////    @Query(value = "select u from User u where u.firstName=?1 or u.lastName=?1")
//    List<User> findByFirstNameOrLastName(String name);
//
//    //is,equals
////    @Query(value = "select * from User u where u.firstName=:firstName")
//    List<User> findByFirstNameIs(String firstName);
//
//    List<User> findByFirstNameEquals(String firstName);
//
//    List<User> findByFirstName(String firstName);
//
//    //between
////    @Query(value = "select u from User u where u.createdAt between ?1 and ?2")
//    List<User> findByCreatedAtBetween(Date startDate, Date endDate);
//
//    // lessthan
////    @Query(value = "select u from User u where u.id<:Id")
//    List<User> findByIdLessThan(int Id);
//
//    List<User> findByIdLessThanEqual(int Id);
//
//    List<User> findByIdGreaterThan(int Id);
//
//    List<User> findByIdGreaterThanEqual(int Id);
//
//    //before,after
////    @Query(value = "select u from User u where u.createdAt<:date")
//    List<User> findByCreatedAtBefore(Date date);
//
//    //    @Query(value = "select u from User u where u.createdAt>:date")
//    List<User> findByCreatedAtAfter(Date date);
//
//    //IsNull,Null
//    //    @Query(value = "select u from User u where u.email is null")
//
//    List<User> findByEmailIsNull();
//
//    //NotNull,IsNotNull
//    //    @Query(value = "select u from User u where u.lastName is not null")
//
//    List<User> findByLastNameNotNull();
//
//    //like
//    //    @Query(value = "select u from User u where u.lastName like %:lastName%")
//    List<User> findByLastNameLike(String lastName);
//
//    //    @Query(value = "select u from User u where u.lastName not like %:lastName%")
//    List<User> findByLastNameNotLike(String lastName);
//
//    //StartingWith
////    @Query(value = "select u from User u where u.lastName like :lastName%")
//    List<User> findByLastNameStartingWith(String lastName);
//
//    //EndingWith
////    @Query(value = "select u from User u where u.lastName like :%lastName")
//    List<User> findByLastNameEndingWith(String lastName);
//
//    //Containing
//    //    @Query(value = "select u from User u where u.lastName like :%lastName%")
//    List<User> findByLastNameContaining(String lastName);
//
//    //Not
//    //    @Query(value = "select u from User u where u.lastName <> :lastName")
//    List<User> findByLastNameNot(String lastName);
//
//    //In
//    //    @Query(value = "select u from User u where u.id in (1,2,3)")
//    List<User> findByIdIn(Collection<?> ids);
//
//    //Not in
//    //    @Query(value = "select u from User u where u.id not in (1,2,3)")
//    List<User> findByIdNotIn(Collection<?> ids);
//
//    //True/False
//    //    @Query(value = "select u from User u where u.activated =true")
////    List<User> findByActivatedTrue();
//
//    //IgnoreCase:like ko phân biệt hoa thường
//    //    @Query(value = "select u from User u where LOWER(u.firstName) =LOWER(:firstName)")
//    List<User> findByFirstNameIgnoreCase(String firstName);
//
//    //sort
//    List<User>findByFirstNameOrderByCreatedAtDesc(String firstName);
//
//    //kết hợp
//    List<User>findByFirstNameAndLastNameAllIgnoreCase(String firstName, String lastName);
//
////join
//    @Query(value = "select u from User u inner join u.addresses a where a.city=:city")
//    List<User>findAllUser(String city);