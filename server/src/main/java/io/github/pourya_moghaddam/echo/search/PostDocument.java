package io.github.pourya_moghaddam.echo.search;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@Document(indexName = "posts")
public class PostDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "english")
    private String content;

    @Field(type = FieldType.Keyword)
    private String communityName;

    @Field(type = FieldType.Keyword)
    private String authorUsername;
}
