package com.github.rudineidebrito18.quarkussocial.rest;

import com.github.rudineidebrito18.quarkussocial.domain.model.Follower;
import com.github.rudineidebrito18.quarkussocial.domain.model.Post;
import com.github.rudineidebrito18.quarkussocial.domain.model.User;
import com.github.rudineidebrito18.quarkussocial.domain.repository.FollowerRepository;
import com.github.rudineidebrito18.quarkussocial.domain.repository.PostRepository;
import com.github.rudineidebrito18.quarkussocial.domain.repository.UserRepository;
import com.github.rudineidebrito18.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {
    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUP() {
        var user = new User();
        user.setAge(24);
        user.setName("test");
        userRepository.persist(user);
        userId = user.getId();

        Post post = new Post();
        post.setText("testing!");
        post.setUser(user);
        postRepository.persist(post);

        var userNotFollower = new User();
        userNotFollower.setAge(25);
        userNotFollower.setName("test2");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        var userFollower = new User();
        userFollower.setAge(29);
        userFollower.setName("test3");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);

        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("some text");

        given()
                    .contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParams("userId", userId)
                .when()
                    .post()
                .then()
                    .statusCode(201);
    }

    @Test
    @DisplayName("should return 404 when trying to make a post for an nonexistent user")
    public void postForAnNonexistentUserTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("some text");

        var userID = 950;

        given()
                    .contentType(ContentType.JSON)
                    .body(postRequest)
                    .pathParams("userId", userID)
                .when()
                    .post()
                .then()
                    .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest() {
        var nonexistentUserId = 100;

        given()
                    .pathParams("userId", nonexistentUserId)
                .when()
                    .get()
                .then()
                    .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest() {
        given()
                    .pathParams("userId", userId)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body(Matchers.is("You forgot the header followerId"));
    }

    @Test
    @DisplayName("should return 404 when follower doesn't exist")
    public void listPostFollowerNotFoundTest() {
        var nonexistentFollowerId = 100;

        given()
                    .pathParams("userId", userId)
                    .header("followerId", nonexistentFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body(Matchers.is("nonexistent followerId"));
    }

    @Test
    @DisplayName("should return 404 when follower isn't follower")
    public void listPostNotAFollowerTest() {
        given()
                    .pathParams("userId", userId)
                    .header("followerId", userNotFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(403)
                    .body(Matchers.is("You can't see these posts"));
    }

    @Test
    @DisplayName("should return posts")
    public void listPostTest() {
        given()
                    .pathParams("userId", userId)
                    .header("followerId", userFollowerId)
                .when()
                    .get()
                .then()
                    .statusCode(200)
                    .body("size()", Matchers.is(1));
    }

}