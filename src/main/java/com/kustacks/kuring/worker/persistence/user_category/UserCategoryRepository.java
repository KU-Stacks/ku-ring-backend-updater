package com.kustacks.kuring.worker.persistence.user_category;

import com.kustacks.kuring.worker.persistence.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {
    List<UserCategory> findAllByUser(User user);
}
