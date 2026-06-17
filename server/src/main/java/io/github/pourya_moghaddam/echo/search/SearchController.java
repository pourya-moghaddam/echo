package io.github.pourya_moghaddam.echo.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/communities")
    public ResponseEntity<List<CommunityDocument>> searchCommunities(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchCommunities(q));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchPosts(q));
    }
}
