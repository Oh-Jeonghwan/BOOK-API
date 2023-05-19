package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.Book;

//@Repostory를 적어야 스프링 IOC에 빈으로 등록이 되는데
//JpaRepostory를 extends하면 생략 가능
//JpaRepository는 CRUD함수를 들고 있음
public interface BookRepository extends JpaRepository<Book,Long>{

}
