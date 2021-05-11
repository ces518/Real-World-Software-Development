package business;

@FunctionalInterface
public interface Rule {
    void perform(Facts facts);
}
