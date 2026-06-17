package io.github.pourya_moghaddam.echo.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunitySearchRepository extends ElasticsearchRepository<CommunityDocument, String> {
    List<CommunityDocument> findByNameMatchesOrDescriptionMatches(String name, String description);
}
