package com.springjpatest.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 블로그 서비스 진입점.
 *
 * <p>게시글(Post) CRUD API를 제공하는 독립 마이크로서비스.
 * 게이트웨이(8080)를 통해 /api/posts/**, /api/categories/** 요청을 처리한다.
 * </p>
 */
@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
