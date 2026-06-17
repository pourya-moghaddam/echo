package io.github.pourya_moghaddam.echo.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pourya_moghaddam.echo.TestcontainersConfiguration;
import io.github.pourya_moghaddam.echo.auth.dto.LoginRequest;
import io.github.pourya_moghaddam.echo.community.CommunityCategory;
import io.github.pourya_moghaddam.echo.community.dto.CreateCommunityRequest;
import io.github.pourya_moghaddam.echo.post.dto.PostCreateRequest;
import io.github.pourya_moghaddam.echo.user.User;
import io.github.pourya_moghaddam.echo.user.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({TestcontainersConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommunitySearchRepository communitySearchRepository;

    @Autowired
    private PostSearchRepository postSearchRepository;

    private ObjectMapper objectMapper;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        jdbcTemplate.execute("TRUNCATE TABLE posts, user_communities, communities, users RESTART IDENTITY CASCADE");
        communitySearchRepository.deleteAll();
        postSearchRepository.deleteAll();

        // Create main user
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user1);

        userToken = getJwtToken("user1", "password");

        // Create a community
        CreateCommunityRequest communityReq = new CreateCommunityRequest("elasticsearch", "We talk about elasticsearch search engine", CommunityCategory.PROGRAMMING);
        mockMvc.perform(post("/api/communities")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(communityReq)))
            .andExpect(status().isCreated());

        // Create a post
        PostCreateRequest postReq = new PostCreateRequest();
        postReq.setTitle("Elasticsearch query string");
        postReq.setContent("How to do wildcard searching in ES?");
        mockMvc.perform(post("/api/communities/elasticsearch/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postReq)))
            .andExpect(status().isCreated());

        // ES operations might take a tiny bit to become searchable
        Thread.sleep(1000);
    }

    private String getJwtToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void searchCommunities_returnsMatch() throws Exception {
        mockMvc.perform(get("/api/search/communities?q=search engine")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("elasticsearch"));
    }

    @Test
    void searchPosts_returnsMatch() throws Exception {
        mockMvc.perform(get("/api/search/posts?q=wildcard")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Elasticsearch query string"));
    }
}
