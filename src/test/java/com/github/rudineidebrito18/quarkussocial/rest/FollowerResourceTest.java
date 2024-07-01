package com.github.rudineidebrito18.quarkussocial.rest;

import com.github.rudineidebrito18.quarkussocial.domain.model.Follower;
import com.github.rudineidebrito18.quarkussocial.domain.model.User;
import com.github.rudineidebrito18.quarkussocial.domain.repository.FollowerRepository;
import com.github.rudineidebrito18.quarkussocial.domain.repository.UserRepository;
import com.github.rudineidebrito18.quarkussocial.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {
    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        var user = new User();
        user.setAge(24);
        user.setName("test");
        userRepository.persist(user);
        userId = user.getId();

        var follower = new User();
        follower.setAge(28);
        follower.setName("test2");
        userRepository.persist(follower);
        followerId = follower.getId();

        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("should return 409 when Follower Id is equal to User id")
    public void sameUserAsFollowerTest() {
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .pathParams("userId", userId)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.CONFLICT.getStatusCode())
                    .body(Matchers.is("You can't follow yourself"));
    }

    @Test
    @DisplayName("should return 404 on follow a user when User id don't exit")
    public void userNotFoundWhenTryingToFollowTest() {
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        var nonexistentUserId = 200;

        given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .pathParams("userId", nonexistentUserId)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should follow a user")
    public void followUserTest() {
        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .pathParams("userId", userId)
                .when()
                    .put()
                .then()
                    .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("should return 404 on list user followers and User id don't exit")
    public void userNotFoundWhenListingFollowersTest() {
        var nonexistentUserId = 200;

        given()
                    .contentType(ContentType.JSON)
                    .pathParams("userId", nonexistentUserId)
                .when()
                    .get()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should list a user's followers")
    public void listFollowersTest() {
        var response = given()
                    .contentType(ContentType.JSON)
                    .pathParams("userId", userId)
                .when()
                    .get()
                .then()
                    .extract().response();

        Object followerCount = response.jsonPath().get("followerCount");
        List<Object> followersContent = response.jsonPath().getList("content");

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followerCount);
        assertEquals(1, followersContent.size());
    }

    @Test
    @DisplayName("should return 404 on unfollow user and user id don't exist")
    public void userNotFoundWhenUnfollowAUserTest() {
        var nonexistentUserId = 200;

        given()
                    .pathParams("userId", nonexistentUserId)
                .queryParam("followerId", followerId)
                .when()
                    .delete()
                .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should unfollow an user")
    public void unfollowUserTest() {
        given()
                .pathParams("userId", userId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}