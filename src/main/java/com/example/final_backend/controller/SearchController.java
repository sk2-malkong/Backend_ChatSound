package com.example.final_backend.controller;

import com.example.final_backend.dto.PostDto;
import com.example.final_backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/search로 시작하는 검색 관련 REST API 제공하는 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final PostService postService;

    @Operation(
            summary = "게시글 검색",
            description = "키워드를 기준으로 제목 또는 내용에 해당하는 게시글을 검색합니다."
    )
    @ApiResponse(responseCode = "200", description = "검색된 게시글 목록이 성공적으로 반환됩니다.")
    @GetMapping
    public ResponseEntity<Page<PostDto.CheckPostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PostDto.CheckPostResponse> results = postService.searchPosts(keyword, pageable);

        return ResponseEntity.ok(results);
    }
}

