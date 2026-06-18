package io.github.pourya_moghaddam.echo.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"content\"], \"type\": \"phrase_prefix\"}}")
    List<PostDocument> search(String query);
}
