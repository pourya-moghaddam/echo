package io.github.pourya_moghaddam.echo.community;

import io.github.pourya_moghaddam.echo.community.dto.CommunityResponse;
import io.github.pourya_moghaddam.echo.community.dto.CreateCommunityRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CreateCommunityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommunityResponse response = communityService.createCommunity(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<CommunityResponse> getCommunity(@PathVariable String name) {
        CommunityResponse response = communityService.getCommunityByName(name);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{name}/join")
    public ResponseEntity<Void> joinCommunity(
            @PathVariable String name,
            @AuthenticationPrincipal UserDetails userDetails) {
        communityService.joinCommunity(name, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{name}/leave")
    public ResponseEntity<Void> leaveCommunity(
            @PathVariable String name,
            @AuthenticationPrincipal UserDetails userDetails) {
        communityService.leaveCommunity(name, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
