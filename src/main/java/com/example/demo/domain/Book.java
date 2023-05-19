package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity //서버 실행시에 Object Relation Mapping이 됨
public class Book {
 @Id //pk를 해당 변수로
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 private String title;
 private String author;
}
