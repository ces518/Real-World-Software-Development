package business;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InspectorTest {

    @Test
    void inspectOneConditionEvaluatesTrue() throws Exception {
        final Facts facts = new Facts();
        facts.addFact("jobTitle", "CEO");
        final ConditionalAction conditionalAction = new JobTitleCondition();
        final Inspector inspector = new Inspector(conditionalAction);

        final List<Report> reports = inspector.inspect(facts);
        assertEquals(1, reports.size());
    }

    // ISP 위반이다.
    // perform 메소드의 구현은 비어있고, UnsupportedOperationException 예외를 발생시킨다.
    // 필요 이상의 기능을 제공하는 ConditionalAction 과 결합되어 있다!!
    // ISP 는 어떤 클래스도 사용하지 않는 메소드에 의존성을 갖지 않아야 한다는 원칙
    // > 이는 불필요한 결합을 만든다..
    // SRP 는 클래스가 하나의 기능만 가져야 하며 클래스가 변경되어야 하는 이유도 하나여야 한다는 원칙이다.
    // ISP 와 비슷해 보이지만 ISP 는 설계가 아닌 사용자 인터페이스에 초점을 둔다는 것이 차이점이다.
    // > 인터페이스가 커질수록 사용자는 사용하지 않는 기능을 갖게 되어 불필요한 결합도가 생긴다..
    private static class JobTitleCondition implements ConditionalAction {

        @Override
        public boolean evaluate(Facts facts) {
            return "CEO".equals(facts.getFact("jobTitle"));
        }

        @Override
        public void perform(final Facts facts) {
            throw new UnsupportedOperationException();
        }
    }

    /*

    ISP 를 준수하도록 각 인터페이스로 분리 하였으나 코드가 분산되어 있다.
    사용자가 각 인스턴스를 만들고 조합해야 한다...
    > 빌더패턴으로 해결해보자

     final Condition condition = (Facts facts) -> "CEO".equals(facts.getFact("jobTitle"));
        final Action action = (Facts facts) -> {
            var name = facts.getFact("name");
            // send Mail...
        };

        final Rule rule = new DefaultRule(condition, action);


    빌더 패턴으로 개선
    - 플루언트 API 설계의 핵심은 메서드 체이닝이다.
      Rule rule = new RuleBuilder()
                .when(facts -> "CEO".equals(facts.getFact("jobTitle")))
                .then(facts -> {
                    var name = facts.getFact("name");
                    // sendMail..
                }).createRule();


      빌더 패턴 개선
        - 의도치 않은 인스턴스 생성을 방지하기 위해 생성자를 private 으로 제한
      Rule rule = RuleBuilder
            .when(facts -> "CEO".equals(facts.getFact("jobTitle")))
            .then(facts -> {
                var name = facts.getFact("name");
                // sendMail..
            }).createRule();

     */
}