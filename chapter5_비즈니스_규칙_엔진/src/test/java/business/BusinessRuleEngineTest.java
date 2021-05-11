package business;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BusinessRuleEngineTest {

/*
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
*/

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

    // 위 테스트가 통과 하므로 Facts 를 이용한 액션 로직 사용 가능
    /*
     직함이 CEO 인 경우 메일 발송

     businessRuleEngine.addAction(facts -> {
         final String jobTitle = facts.getFact("jobTitle");
         if ("CEO".equals(jobTitle)) {
             final String name = facts.getFact("name");
             // sendMail..
         }
     });

     */

    /*
    특정 거래 예상치를 계산하는 규칙

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


      switch 문은 break; 를 빼먹으면 폴-스루 모드로 동작할 수 있다. (의도치 않은 버그 발생)
      자바 12 에서는 switch expression 을 제공하여 폴-스루 모드를 방지할 수 있음 (가독성도 향상)

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

     */
}