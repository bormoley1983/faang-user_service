package school.faang.user_service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class FailedTest {
    @Test
    public void testThatShouldFail() {
        fail("Этот тест должен упасть");
    }
}
