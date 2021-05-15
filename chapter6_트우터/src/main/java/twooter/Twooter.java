package twooter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Twooter {
    private final TwootRepository twootRepository;
    private final UserRepository userRepository;

    public Twooter(final TwootRepository twootRepository, final UserRepository userRepository) {
        this.twootRepository = twootRepository;
        this.userRepository = userRepository;
    }

    /**
     * 로그인 실패시 응답을 어떻게 할 것인가 ?
     * <p>
     * 1. 예외발생 -> 문제해결은 가능하지만 적절한 예외를 사용했는가 ?
     * -> 로그온 실패는 언제든 일어날 수 있다. (아이디 / 패스워드 실패 등..)
     * <p>
     * 2. null 을 반환 -> NPE 발생 가능성
     * -> 컴파일 타임에 체크가 불가능
     * -> 메소드 시그니쳐로 확인이 불가능
     * <p>
     * * Optional 타입을 반환해 이를 해결한다.
     */
    public Optional<ServerEndPoint> onLogon(String userId, String password, ReceiverEndPoint receiver) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(password, "password");

        final Optional<User> authenticatedUser = userRepository.get(userId)
            .filter(userOfSameId -> {
                final byte[] hashedPassword = KeyGenerator.hash(password, userOfSameId.getSalt());
                return Arrays.equals(hashedPassword, userOfSameId.getPassword());
            });

        authenticatedUser.ifPresent(user -> {
            user.onLogon(receiver);
            twootRepository.query(
                new TwootQuery()
                    .inUsers(user.getFollowing())
                    .lastSeenPosition(user.getLastSeenPosition()),
                user::receiveTwoot
            );
            userRepository.update(user);
        });

        return authenticatedUser.map(user -> new ServerEndPoint(user, this));
    }

    public RegistrationStatus onRegisterUser(final String userId, final String password) {
        final byte[] salt = KeyGenerator.newSalt();
        final byte[] hashedPassword = KeyGenerator.hash(password, salt);
        final User user = new User(userId, hashedPassword, salt, Position.INITIAL_POSITION);
        return userRepository.add(user) ? RegistrationStatus.SUCCESS : RegistrationStatus.DUPLICATE;
    }

    FollowStatus onFollow(final User follow, final String userIdToFollow) {
        return userRepository.get(userIdToFollow)
            .map(userToFollow -> userRepository.follow(follow, userToFollow))
            .orElse(FollowStatus.INVALID_USER);
    }

    Position onSendTwoot(final String id, final User user, final String content) {
        final String userId = user.getId();
        final Twoot twoot = twootRepository.add(id, userId, content);

        user.followers()
            .filter(User::isLoggedOn)
            .forEach(follower -> {
                follower.receiveTwoot(twoot);
                userRepository.update(follower);
            });

        return twoot.getPosition();
    }

    DeleteStatus onDeleteTwoot(final String userId, final String id) {
        return twootRepository
            .get(id)
            .map(twoot -> {
                final boolean canDeleteTwoot = twoot.getSenderId().equals(userId);
                if (canDeleteTwoot) {
                    twootRepository.delete(twoot);
                }
                return canDeleteTwoot ? DeleteStatus.SUCCESS : DeleteStatus.NOT_YOUR_TWOOT;
            })
            .orElse(DeleteStatus.UNKNOWN_TWOOT);
    }
}
