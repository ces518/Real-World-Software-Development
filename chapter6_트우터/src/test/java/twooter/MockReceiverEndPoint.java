package twooter;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class MockReceiverEndPoint implements ReceiverEndPoint {
    private final List<Twoot> receivedTwoots = new ArrayList<>();

    @Override
    public void onTwoot(final Twoot twoot) {
        receivedTwoots.add(twoot);
    }

    public void verifyOnTwoot(final Twoot twoot) {
        assertThat(
            receivedTwoots,
            contains(twoot)
        );
    }
}
