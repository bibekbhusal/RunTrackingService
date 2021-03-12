package com.bhusalb.runtrackingservice.libs.jwt;

import com.bhusalb.runtrackingservice.models.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JsonWebTokenHelperTest {

    @Autowired
    private JsonWebTokenHelper helper;

    @Test
    void generateToken () {
        final ObjectId id = new ObjectId();
        final User user = new User("email", "pass");
        user.setId(id);

        final String token = helper.generateToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    void getUserId () {
        final ObjectId id = new ObjectId();
        final User user = new User("email", "pass");
        user.setId(id);

        final String token = helper.generateToken(user);
        assertThat(token).isNotBlank();

        final String userId = helper.getUserId(token);
        assertThat(userId).isEqualTo(id.toString());
    }
}