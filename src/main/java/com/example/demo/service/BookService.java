package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Book;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

//서비스는 기능 정의 가능, 트랙잭션 관리 가능
@RequiredArgsConstructor //final 키워드를 달았을 때 자동으로 의존성주입(DI)을 해주는 롬복
@Service
public class BookService {
	
	private final BookRepository bookRepostiory;
	
	@Transactional
	public Book saveBook(Book book) {
		return bookRepostiory.save(book);
	}
	
	@Transactional(readOnly=true)//JPA 변경감지라는 내부 기능 활성화X, update시 정합성 유지가능 but, insert의 유령 데이터 현상 못 막음
	public Book findById(Long id) {
//		return bookRepostiory.findById(id)
//				.orElseThrow(new Supplier<IllegalArgumentException>() {
//					@Override
//					public IllegalArgumentException get() {
//						return new IllegalArgumentException("ID를 입력해줏tpdy");
//					}
//				});
		//람다식으로 변형 시 에러의 타입을 몰라도 된다. 왜??
		return bookRepostiory.findById(id)
				.orElseThrow(()-> new IllegalArgumentException("ID를 입력해주세요."));
	}
	
	@Transactional(readOnly=true)
	public List<Book> findAll(){
		return bookRepostiory.findAll();
	}
	
	@Transactional
	public Book editBook (Long id, Book book) {
		//더티체킹 update치기
		Book bookEntity = bookRepostiory.findById(id)
				.orElseThrow(()-> new IllegalArgumentException("ID를 입력해주세요.")); //영속화(메모리로 들고 있다)->영속성 컨텍스트에 보관
		
		bookEntity.setTitle(book.getTitle());
		bookEntity.setAuthor(book.getAuthor());
		
		return bookEntity;
	} //함수종료=> 트랜잭션종료=>영속회 되어있는 데이터를 db로 갱신(flush)=>commit ==> 더티체킹
	
	@Transactional
	public String deleteBook (Long id) {
		bookRepostiory.deleteById(id);
		return "OK";
	}
}
