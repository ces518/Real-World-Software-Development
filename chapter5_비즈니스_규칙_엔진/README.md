# Chapter 5 비즈니스 규칙 엔진

## 요구사항
개발자가 아닌 사람도 자신의 워크 플로우에 비즈니스 로직을 추가하거나 바꿀 수 있는 기능 제작

비즈니스 규칙 엔진은 간단한 맞춤 언어를 사용해 한개 이상의 비즈니스 규칙을 실행하는 소프트웨어로 다양한 컴포넌트를 동시에 지원

- 팩트
    - 규칙이 확인할 정보
- 액션 
    - 수행 하려는 동작
- 조건
    - 액션을 언제 발생시킬것인가
- 규칙
    - 실행하려는 비즈니스 규칙 지정
    - 팩트, 액션, 조건을 하나로 묶어 규칙이 생성된다.
    
> 비즈니스 규칙 엔진의 핵심 기능을 포함하는 **최소 기능 제품 (Minimal Viable Product) 를 개발

## 테스트 주도 개발
- 사용자가 사용할 기본 기능
    - 액션 추가
    - 액션 실행
    - 기본 보고

```java
public class BusinessRuleEngine {

    public void addAction(final Action action) {
        throw new UnsupportedOperationException();
    }

    public int count() {
        throw new UnsupportedOperationException();
    }

    public void run() {
        throw new UnsupportedOperationException();
    }
}

@FunctionalInterface
public interface Action {
    void execute();
}
```
- Action 이라는 함수형 인터페이스를 만들어 비즈니스 규칙 엔진과 액션의 결합도를 제거한다.

### TDD 를 하는 이유
- 테스트를 별도로 구현 하므로 테스트에 대응하는 요구사항을 구현할때 마다 요구사항에 집중하여 개선 할 수 있음
- 올바른 설계가 가능하다. 어떤 공개 인터페이스를 만들지 신중해 짐
- TDD 사이클에 따라 반복적인 테스트 스위트를 만들 수 있으며 버그 발생 범위도 줄어 듦
- 테스트를 통과하기 위한 구현을 하기 때문에 오버 엔지니어링을 줄일 수 있다.

### TDD 사이클
1. 실패하는 테스트 구현
2. 테스트 실행
3. 기능 구현
4. 테스트 실행
5. 리팩토링
6. 테스트 실행
7. 5 ~ 반복

`비즈니스 규칙 엔진 테스트`

```java
class BusinessRuleEngineTest {

    @Test
    void shouldHaveNoRulesInitially() throws Exception {
        final BusinessRuleEngine businessRuleEngine = new BusinessRuleEngine();

        assertEquals(0, businessRuleEngine.count());
    }


    @Test
    void shouldAddTwoActions() throws Exception {
        final BusinessRuleEngine businessRuleEngine = new BusinessRuleEngine();

        businessRuleEngine.addAction(() -> {});
        businessRuleEngine.addAction(() -> {});

        assertEquals(2, businessRuleEngine.count());
    }
}
```
- 위 테스트는 실패한다.
    - 현재 UnsupportedOperation 예외를 발생시키기 때문

```java
public class BusinessRuleEngine {
    private final List<Action> actions;
    private final Facts facts;

    public BusinessRuleEngine(final Facts facts) {
        this.actions = new ArrayList<>();
        this.facts = facts;
    }

    public void addAction(final Action action) {
        this.actions.add(action);
    }

    public int count() {
        return this.actions.size();
    }

    public void run() {
        throw new UnsupportedOperationException();
    }
}
```
- void 메소드인 run 에 대한 테스트를 구현하기 위해서는 **모킹** 이 필요하다.

## 모킹
- **모킹 (mocking)** 은 void 메소드가 실행 되었을때 이를 확인하는 기법
- 대체로 모키토를 이용 한다.

```java
class BusinessRuleEngineTest {

    @Test
    void shouldExecuteOneAction() throws Exception {
        // given
        final BusinessRuleEngine businessRuleEngine = new BusinessRuleEngine();
        final Action mockAction = mock(Action.class);

        // when
        businessRuleEngine.addAction(mockAction);
        businessRuleEngine.run();

        // then
        verify(mockAction).execute();
    }
}

public class BusinessRuleEngine {
    private final List<Action> actions;
    private final Facts facts;

    public BusinessRuleEngine(final Facts facts) {
        this.actions = new ArrayList<>();
        this.facts = facts;
    }

    public void addAction(final Action action) {
        this.actions.add(action);
    }

    public int count() {
        return this.actions.size();
    }

    public void run() {
        this.actions.forEach(action::execute);
    }
}
```
- Action 객체를 Mocking 해서 Action 의 execute 메소드가 호출되었는지 검증 한다.

## 조건 추가
- 비즈니스 규칙 엔진으로 특정 조건을 만족하면 액션을 수행하도록 설정이 가능해야 한다.
- 이런 조건은 "팩트" 에 의존한다.

```java
final Customer customer = new Customer("Mark", "CEO");

businessRuleEngine.addAction(new Action() {
    
    @Override
    public void execute() {
        if ("CEO".equals(customer.getJobTitle())) {
            // send Mail...    
        }
    }
});
```
- CEO 직함이면 메일을 보낸다는 액션을 정의 했다.
- 하지만 이런 경우 Customer 객체가 하드코딩된 의존성을 가지기 때문에 **독립적** 이지 않다.
- 액션과 그룹화 되어 있지 않아 여러 곳에 상태가 공유 된다.

### Facts

```java
class BusinessRuleEngineTest {

    @Test
    void shouldExecuteAndActionWithFacts() throws Exception {
        // given
        final Action mockAction = mock(Action.class);
        final Facts mockFacts = mock(Facts.class);
        final BusinessRuleEngine businessRuleEngine = new BusinessRuleEngine(mockFacts);

        // when
        businessRuleEngine.addAction(mockAction);
        businessRuleEngine.run();

        // then
        verify(mockAction).execute(mockFacts);
    }
}

public class Facts {
    private final Map<String, String> facts = new HashMap<>();

    public String getFact(final String name) {
        return this.facts.get(name);
    }

    public void addFact(final String name, final String value) {
        this.facts.put(name, value);
    }
}

@FunctionalInterface
public interface Action {
    void execute(Facts facts);
}

public class BusinessRuleEngine {
    private final List<Action> actions;
    private final Facts facts;

    public BusinessRuleEngine(final Facts facts) {
        this.actions = new ArrayList<>();
        this.facts = facts;
    }

    public void addAction(final Action action) {
        this.actions.add(action);
    }

    public int count() {
        return this.actions.size();
    }

    public void run() {
        this.actions.forEach(action -> action.execute(facts));
    }
}

```
- Facts 를 이용해 비즈니스 규칙의 액션 내에서 사용가능한 상태로 캡슐화한다.
    - Action 은 Facts 에 의존해 동작하도록 변경
    - BusinessRuleEngine 도 Facts 를 사용하도록 변경

`기존 문제를 Facts 를 사용한 개선 로직`

```java
businessRuleEngine.addAction(facts -> {
    final String jobTitle = facts.getFact("jobTitle");
    if ("CEO".equals(jobTitle)) {
        final String name = facts.getFact("name");
        // sendMail..
    }
});
```

## 고객 관계 관리 CRM
- CRM 시스템에 다양한 거래 상태와 특정 금액을 갖는 여러 거래를 저장하고자 하는 니즈
- 거래 상태는 enum 으로 정의

```java
public enum Stage {
    LEAD,
    INTERESTED,
    EVALUATING,
    CLOSED,
    ;
}

// 특정 거래의 예상치를 계산하는 규칙
businessRuleEngine.addAction(facts -> {
    var forecastedAmount = 0.0;
    var dealStage = Stage.valueOf(facts.getFact("stage"));
    var amount = Double.parseDouble(facts.getFact("amount"));
    if (dealStage == Stage.LEAD) {
        forecastedAmount = amount * 0.2;
    } else if (dealStage == Stage.EVALUATING) {
        forecastedAmount = amount * 0.5;
    } else if (dealStage == Stage.INTERESTED) {
        forecastedAmount = amount * 0.8;
    } else if (dealStage == Stage.CLOSED) {
        forecastedAmount = amount;
    }
    facts.addFact("forecastedAmount", String.valueOf(forecastedAmount));
});
```

`위 로직을 switch 문을 사용해 개선`

```java
businessRuleEngine.addAction(facts -> {
    var dealStage = Stage.valueOf(facts.getFact("stage"));
    var amount = Double.parseDouble(facts.getFact("amount"));

    var forecastedAmount = amount * switch (dealStage) {
        case LEAD -> 0.2;
        case EVALUATING -> 0.5;
        case INTERESTED -> 0.8;
        case CLOSED -> 1;
    }
    facts.addFact("forecastedAmount", String.valueOf(forecastedAmount));
});
```
- 일반적인 스위치문은 break; 가 누락되면 폴-스루 모드로 동작한다.
    - 다음 블록이 동작하면서 버그가 발생할 수 있음
- 자바 12 에서는 switch expression 을 지원해 break 를 사용하지 않아도 폴-스루를 방지할 수 있다.

## 인터페이스 분리 원칙
- 비즈니스 규칙 엔진 사용자가 사용가능한 액션과 조건을 검사 하도록 **인스펙터** 를 개발해야 한다.
- 실제 액션을 수행하지 않아도 액션가 관련된 조건을 기록해야 함
- 조건과 액션을 분리하기 위해 조건을 평가하는 기능을 내장한 새로운 인터페이스 정의

```java
public interface ConditionalAction {
    boolean evaluate(Facts facts);
    void perform(Facts facts);
}

public class Inspector {
    private final List<ConditionalAction> conditionalActions;

    public Inspector(ConditionalAction... conditionalActions) {
        this.conditionalActions = Arrays.asList(conditionalActions);
    }

    public List<Report> inspect(final Facts facts) {
        final List<Report> reports = new ArrayList<>();
        for (final ConditionalAction conditionalAction : conditionalActions) {
            final boolean conditionResult = conditionalAction.evaluate(facts);
            reports.add(new Report(facts, conditionalAction, conditionResult));
        }
        return reports;
    }
}

public class Report {
    private final ConditionalAction conditionalAction;
    private final Facts facts;
    private final boolean isPositive;

    public Report(final Facts facts, final ConditionalAction conditionalAction, final boolean isPositive) {
        this.facts = facts;
        this.conditionalAction = conditionalAction;
        this.isPositive = isPositive;
    }

    public ConditionalAction getConditionalAction() {
        return conditionalAction;
    }

    public Facts getFacts() {
        return facts;
    }

    public boolean isPositive() {
        return isPositive;
    }

    @Override
    public String toString() {
        return "Report{" +
            "conditionalAction=" + conditionalAction +
            ", facts=" + facts +
            ", isPositive=" + isPositive +
            '}';
    }
}
```
- Inspector 는 ConditionalAction 객체 목록을 받아 팩트로 이를 평가하고, 팩트 / 조건부 액션 / 결과를 포함하는 리포트 목록을 반환한다.

### ISP 위반
- ConditionalAction 인터페이스는 **인터페이스 분리 원칙 (ISP)** 을 위반한다.

```java
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
}
```
- ConditionalAction 의 구현체인 JobTitleCondition 의 구현을 보면 이는 ISP 위반이다.
- perform 메소드의 구현은 비어 있고 UnsupportedOperationException 예외를 발생시킨다.
- 필요 이사으이 기능을 제공하면 인터페이스와 결합되어 있다.
- ISP 는 **어떤 클래스도 사용하지 않는 메소드에 의존성을 가져선 안된다는 원칙** 이다.
    - SRP 와 비슷해 보이지만 ISP 는 설계가 아닌 사용자 인터페이스에 초점을 둔다는 것이 차이점
    - 인터페이스가 커질수록 사용자는 사용하지 않는 기능을 갖게 되어 불필요한 결합도가 생겨난다.

> 

## 플루언트 API 설계
- 플루언트 API 란 특정 문제를 직관적으로 해결이 가능하도록 특정 도메인에 맞춰진 API
- 이는 메서드 체이닝을 이용하면 더 복잡한 연산도 지정이 가능하다.
- 대표적인 API
    - Java Stream API
    - Spring Integration
    - JOOQ

### 도메인 모델링
- '어떤 조건이 주어졌을때 (when)' '이런 작업을 한다 (then)' 과 같이 간단한 조합으로 규칙으로 지정한다.
- 이 도메인에 등장하는 세가지 개념
    - 조건
        - 어떤 팩트에 적용할 조건
    - 액션
        - 실행할 연산이나 코드 집합
    - 규칙
        - 조건과 액션을 합친것, 참일 경우에만 액션을 실행

`조건에 해당하는 Condition 인터페이스 정의`

```java
@FunctionalInterface
public interface Condition {
    boolean evaluate(Facts facts);
}
```
- 좋은 네이밍은 코드가 어떤 문제를 해결하는지 이해하는데 도움을 준다.
    - java.util.Predicate 인터페이스를 사용할 수 도 있지만 현재 도메인에 어울리는 인터페이스를 별도로 정의

`perform 연산을 수행하는 Rule (규칙) 인터페이스 정의`

```java
@FunctionalInterface
public interface Rule {
    void perform(Facts facts);
}

public class DefaultRule implements Rule {
    private final Condition condition;
    private final Action action;

    public DefaultRule(final Condition condition, final Action action) {
        this.condition = condition;
        this.action = action;
    }

    @Override
    public void perform(Facts facts) {
        if (condition.evaluate(facts)) {
            action.execute(facts);
        }
    }
}
```

`ISP 를 준수하도록 개선`

```java
final Condition condition = (Facts facts) -> "CEO".equals(facts.getFact("jobTitle"));

final Action action = (Facts facts) -> {
    var name = facts.getFact("name");
    // send Mail...
};

final Rule rule = new DefaultRule(condition, action);
```
- ISP 를 준수하도록 각 인터페이스로 분리 하였으나 코드가 분산되어 있다.
- 사용자가 각 인스턴스를 만들고 조합해야 하는 문제가 있다..

### 빌더 패턴
- 빌더 패턴은 단순하게 객체를 만드는 방법을 제공한다.
- 생성자의 파라미터를 분해해서 각각의 파라미터를 받는 여러 메소드로 분리한다.
- 그로 인해 각 메소드는 도메인이 다루는 문제와 비슷한 이름을 갖는다.

```java
public class RuleBuilder {
    private Condition condition;
    private Action action;

    private RuleBuilder(final Condition condition) {
        this.condition = condition;
    }

    public static RuleBuilder when(final Condition condition) {
        return new RuleBuilder(condition);
    }

    public RuleBuilder then(final Action action) {
        this.action = action;
        return this;
    }

    public Rule createRule() {
        return new DefaultRule(condition, action);
    }
}
```
- 의도치 않은 인스턴스 생성을 막기 위해 생성자를 private 으로 제한한다.

```java
Rule rule = RuleBuilder
    .when(facts -> "CEO".equals(facts.getFact("jobTitle")))
    .then(facts -> {
        var name = facts.getFact("name");
        // sendMail..
    }).createRule();
```

`리팩터링 한 비즈니스 규칙 엔진`

```java
public class BusinessRuleEngine {
    private final List<Rule> rules;
    private final Facts facts;

    public BusinessRuleEngine(final Facts facts) {
        this.rules = new ArrayList<>();
        this.facts = facts;
    }

    public void addRule(final Rule rule) {
        this.rules.add(rule);
    }

    public int count() {
        return this.rules.size();
    }

    public void run() {
        this.rules.forEach(rule -> rule.perform(facts));
    }
}
```

## 정리
- TDD 사이클에 따르면 테스트를 먼저 구현하고 이를 가이드 삼아 기능을 구현한다.
- 인터페이스 분리 원칙은 불필요한 메소드 의존성을 낮춰 높은 응집도를 촉진한다.
- 플루언트 API 는 메소드 체이닝이 핵심
