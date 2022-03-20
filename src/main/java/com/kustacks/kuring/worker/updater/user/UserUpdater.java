package com.kustacks.kuring.worker.updater.user;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.kustacks.kuring.worker.updater.firebase.UpdaterFirebaseClient;
import com.kustacks.kuring.worker.persistence.user.User;
import com.kustacks.kuring.worker.persistence.user.UserRepository;
import com.kustacks.kuring.worker.persistence.user_category.UserCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserUpdater {

    private final UpdaterFirebaseClient updaterFirebaseClient;
    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;

    public UserUpdater(
            UpdaterFirebaseClient updaterFirebaseClient,
            UserRepository userRepository,
            UserCategoryRepository userCategoryRepository) {

        this.updaterFirebaseClient = updaterFirebaseClient;
        this.userRepository = userRepository;
        this.userCategoryRepository = userCategoryRepository;
    }

    public void update() {

        log.info("========== 토큰 유효성 필터링 시작 ==========");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            String token = user.getToken();
            try {
                updaterFirebaseClient.verifyToken(token);
            } catch(FirebaseMessagingException e) {
                userCategoryRepository.deleteAll(user.getUserCategories());
                userRepository.deleteByToken(token);
                log.info("삭제한 토큰 = {}", token);
            }
        }

        log.info("========== 토큰 유효성 필터링 종료 ==========");
    }
}
