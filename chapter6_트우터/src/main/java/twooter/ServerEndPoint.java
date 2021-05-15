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

        return twooter.onFollow(user, userIdToFollow);
    }

    public Position onSendTwoot(final String id, final String content) {
        Objects.requireNonNull(content, "content");

        return twooter.onSendTwoot(id, user, content);
    }

    public void onLogoff() {
        user.onLogOff();
    }

    public DeleteStatus onDeleteTwoot(final String id) {
        return twooter.onDeleteTwoot(user.getId(), id);
    }
}
