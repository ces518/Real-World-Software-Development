package twooter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import twooter.in_memory.InMemoryTwootRepository;
import twooter.in_memory.InMemoryUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static twooter.TestData.twootAt;

class TwooterTest {

    private static final Position POSITION1 = new Position(0);

    private final ReceiverEndPoint receiverEndPoint = mock(ReceiverEndPoint.class);

    private final TwootRepository twootRepository = spy(new InMemoryTwootRepository());
    private final UserRepository userRepository = spy(new InMemoryUserRepository());

    private Twooter twooter;
    private ServerEndPoint endPoint;

    @BeforeEach
    void setUp() {
        twooter = new Twooter(twootRepository, userRepository);

        assertEquals(RegistrationStatus.SUCCESS, twooter.onRegisterUser(TestData.USER_ID, TestData.PASSWORD));
        assertEquals(RegistrationStatus.SUCCESS, twooter.onRegisterUser(TestData.OTHER_USER_ID, TestData.PASSWORD));
    }


    @Test
    void shouldNotRegisterDuplicateUsers() throws Exception {
        assertEquals(RegistrationStatus.DUPLICATE, twooter.onRegisterUser(TestData.USER_ID, TestData.PASSWORD));
    }

    @Test
    void shouldBeAbleToAuthenticateUser() throws Exception {
        logon();
    }

    @Test
    void shouldNotAuthenticateUserWithWrongPassword() throws Exception {
        Optional<ServerEndPoint> endPoint = twooter.onLogon(TestData.USER_ID, "bad password", receiverEndPoint);

        assertFalse(endPoint.isPresent());
    }

    @Test
    void shouldNotAuthenticateUnknownUser() throws Exception {
        final Optional<ServerEndPoint> endPoint = twooter.onLogon(
            TestData.NOT_A_USER, TestData.PASSWORD, receiverEndPoint);

        assertFalse(endPoint.isPresent());
    }

    /**
     * Follow 메소드의 반환값을 void, boolean 으로 처리하는 것은 좋지 않다.
     * -> void 인 경우 Follow 에 실패했을때 예외로 이를 알려한다. 하지만 예외를 던지는 것은 정말 예외적인 상황에서만 사용하는 것이 좋다.
     * <p>
     * -> boolean 인 경우 true, false 만 반환할 수 있을뿐 "어떤 이유로 실패했는지" 알릴 수 없다.
     * <p>
     * -> 또 다른방법인 int 로 오류 코드를 반환하는 방법은 이전에 살펴본 대료 좋지 않은 방법이다.
     * <p>
     * * enum 상수롤 이용해 이를 해결한다.
     */
    @Test
    void shouldFollowValidUser() throws Exception {
        logon();

        final FollowStatus followStatus = endPoint.onFollow(TestData.OTHER_USER_ID);

        assertEquals(FollowStatus.SUCCESS, followStatus);
    }

    @Test
    void shouldNotDuplicateFollowValidUser() throws Exception {
        logon();

        endPoint.onFollow(TestData.OTHER_USER_ID);

        final FollowStatus followStatus = endPoint.onFollow(TestData.OTHER_USER_ID);
        assertEquals(FollowStatus.ALREADY_FOLLOWING, followStatus);
    }

    @Test
    void shouldReceiveTwootsFromFollowedUser() throws Exception {
        final String id = "1";

        logon();

        endPoint.onFollow(TestData.OTHER_USER_ID);

        final ServerEndPoint otherEndPoint = otherLogon();
        otherEndPoint.onSendTwoot(id, TestData.TWOOT);

        verify(twootRepository).add(id, TestData.OTHER_USER_ID, TestData.TWOOT);
        verify(receiverEndPoint).onTwoot(new Twoot(id, TestData.OTHER_USER_ID, TestData.TWOOT, POSITION1));
    }

    @Test
    void shouldReceiveReplayOfTwootsAfterLogoff() throws Exception {
        final String id = "1";

        userFollowsOtherUser();

        final ServerEndPoint otherEndPoint = otherLogon();
        otherEndPoint.onSendTwoot(id, TestData.TWOOT);

        logon();

        verify(receiverEndPoint).onTwoot(twootAt(id, POSITION1));
    }

    @Test
    void shouldDeleteTwoots() throws Exception {
        final String id = "1";

        userFollowsOtherUser();

        final ServerEndPoint otherEndPoint = otherLogon();
        otherEndPoint.onSendTwoot(id, TestData.TWOOT);
        final DeleteStatus status = otherEndPoint.onDeleteTwoot(id);

        logon();

        assertEquals(DeleteStatus.SUCCESS, status);
        verify(receiverEndPoint, never()).onTwoot(twootAt(id, POSITION1));
    }

    @Test
    void shouldNotDeleteFuturePoistionTwoots() throws Exception {
        logon();

        final DeleteStatus status = endPoint.onDeleteTwoot("DAS");

        assertEquals(DeleteStatus.UNKNOWN_TWOOT, status);
    }

    @Test
    void shouldNotOtherUsersTwoots() throws Exception {
        final String id = "1";

        logon();

        final ServerEndPoint otherEndPoint = otherLogon();
        otherEndPoint.onSendTwoot(id, TestData.TWOOT);

        final DeleteStatus status = endPoint.onDeleteTwoot(id);

        assertNotNull(twootRepository.get(id));
        assertEquals(DeleteStatus.NOT_YOUR_TWOOT, status);
    }

    private ServerEndPoint otherLogon() {
        return logon(TestData.OTHER_USER_ID, mock(ReceiverEndPoint.class));
    }

    private void userFollowsOtherUser() {
        logon();

        endPoint.onFollow(TestData.OTHER_USER_ID);

        endPoint.onLogoff();
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