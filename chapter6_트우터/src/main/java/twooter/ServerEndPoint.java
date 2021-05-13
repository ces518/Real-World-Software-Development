package twooter;

import java.util.Objects;

public class ServerEndPoint {
    private final User user;
    private final Twooter twooter;

    ServerEndPoint(final User user, final Twooter twooter) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(twooter, "twooter");

        this.user = user;
        this.twooter = twooter;
    }

    public FollowStatus onFollow(final String userIdToFollow) {
        Objects.requireNonNull(userIdToFollow, "userIdToFollow");

        return FollowStatus.SUCCESS;
    }
}
