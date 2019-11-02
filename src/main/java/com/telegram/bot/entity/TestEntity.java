package com.telegram.bot.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TestEntity {
    @GeneratedValue
    @Id
    private Long id;
}