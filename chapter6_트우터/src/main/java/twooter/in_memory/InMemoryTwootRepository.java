package twooter.in_memory;

import twooter.Position;
import twooter.Twoot;
import twooter.TwootQuery;
import twooter.TwootRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class InMemoryTwootRepository implements TwootRepository {
    private final List<Twoot> twoots = new ArrayList<>();

    private Position currentPosition = Position.INITIAL_POSITION;

    @Override
    public Twoot add(String id, String userId, String content) {
        currentPosition = currentPosition.next();

        final Position twootPosition = currentPosition;
        final Twoot twoot = new Twoot(id, userId, content, twootPosition);
        twoots.add(twoot);
        return twoot;
    }

    @Override
    public Optional<Twoot> get(String id) {
        return twoots.stream()
            .filter(twoot -> twoot.getId().equals(id))
            .findFirst();
    }

    @Override
    public void delete(Twoot twoot) {
        twoots.remove(twoot);
    }

    @Override
    public void query(final TwootQuery twootQuery, final Consumer<Twoot> callback) {
        if (!twootQuery.hasUsers()) {
            return;
        }

        final Position lastSeenPosition = twootQuery.getLastSeenPosition();
        final Set<String> inUsers = twootQuery.getInUsers();

        twoots.stream()
            .filter(twoot -> inUsers.contains(twoot.getSenderId()))
            .filter(twoot -> twoot.isAfter(lastSeenPosition))
            .forEach(callback);
    }

    public void queryLoop(final TwootQuery twootQuery, final Consumer<Twoot> callback) {
        if (!twootQuery.hasUsers()) {
            return;
        }

        final Position lastSeenPosition = twootQuery.getLastSeenPosition();
        final Set<String> inUsers = twootQuery.getInUsers();

        for (Twoot twoot : twoots) {
            if (inUsers.contains(twoot.getSenderId()) &&
                twoot.isAfter(lastSeenPosition)) {
                callback.accept(twoot);
            }
        }
    }

    @Override
    public void clear() {
        twoots.clear();
    }

    @Override
    public void close() throws Exception {

    }
}
