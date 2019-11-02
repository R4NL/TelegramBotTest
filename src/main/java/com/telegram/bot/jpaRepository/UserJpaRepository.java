package com.telegram.bot.jpaRepository;

import com.telegram.bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    User findByChatId(Long ChatId);
}
