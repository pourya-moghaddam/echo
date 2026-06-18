package io.github.pourya_moghaddam.echo.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;

@Repository
public interface CommunitySearchRepository extends ElasticsearchRepository<CommunityDocument, String> {
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"description\"], \"type\": \"phrase_prefix\"}}")
    List<CommunityDocument> search(String query);
}
