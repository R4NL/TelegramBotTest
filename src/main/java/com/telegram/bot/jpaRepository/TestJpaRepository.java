package com.telegram.bot.jpaRepository;

import com.telegram.bot.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestJpaRepository extends JpaRepository<TestEntity, Long> {
}
