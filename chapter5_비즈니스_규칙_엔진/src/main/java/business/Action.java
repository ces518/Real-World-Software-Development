package business;

@FunctionalInterface
public interface Action {
    void execute(Facts facts);
}
