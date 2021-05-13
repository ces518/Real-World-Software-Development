package twooter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TwooterTest {
    private Twooter twooter = new Twooter();
    private ReceiverEndPoint receiverEndPoint = mock(ReceiverEndPoint.class);
    private ServerEndPoint endPoint;

    @Test
    void shouldBeAbleToAuthenticationUser() throws Exception {
        // 유효 사용자의 로그온 메세지 수신

        // 로그온 메서드는 새 엔드포인트 반환

        // 엔드포인트 유효성을 확인하는 어서션
    }

    @Test
    void shouldNotAuthenticateUserWithWrongPassword() throws Exception {
        Optional<ServerEndPoint> endPoint = twooter.onLogon(TestData.USER_ID, "bad password", receiverEndPoint);

        assertFalse(endPoint.isPresent());
    }

    /**
     * Follow 메소드의 반환값을 void, boolean 으로 처리하는 것은 좋지 않다.
     * -> void 인 경우 Follow 에 실패했을때 예외로 이를 알려한다. 하지만 예외를 던지는 것은 정말 예외적인 상황에서만 사용하는 것이 좋다.
     *
     * -> boolean 인 경우 true, false 만 반환할 수 있을뿐 "어떤 이유로 실패했는지" 알릴 수 없다.
     *
     * -> 또 다른방법인 int 로 오류 코드를 반환하는 방법은 이전에 살펴본 대료 좋지 않은 방법이다.
     *
     * * enum 상수롤 이용해 이를 해결한다.
     */
    @Test
    void shouldFollowValidUser() throws Exception {
        logon();

        final FollowStatus followStatus = endPoint.onFollow(TestData.OTHER_USER_ID);

        assertEquals(FollowStatus.SUCCESS, followStatus);
    }

    @Test
    void shouldReceiveTwootsFromFollowedUser() throws Exception {
        // given

        // when

        // then
    }

    private ServerEndPoint otherLogon() {
        return logon(TestData.OTHER_USER_ID, mock(ReceiverEndPoint.class));
    }

    private void logon() {
        this.endPoint = logon(TestData.USER_ID, receiverEndPoint);
    }

    private ServerEndPoint logon(final String userId, final ReceiverEndPoint receiverEndPoint) {
        final Optional<ServerEndPoint> endPoint = twooter.onLogon(userId, TestData.PASSWORD, receiverEndPoint);
        assertTrue(endPoint.isPresent(), "Failed to logon");
        return endPoint.get();
    }
}