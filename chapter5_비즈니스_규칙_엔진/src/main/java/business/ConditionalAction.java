package business;

/**
 * 조건을 평가하는 기능을 가진 Action 인터페이스
 */
public interface ConditionalAction {
    boolean evaluate(Facts facts);
    void perform(Facts facts);
}
