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

    @Test
    void shouldExecuteAndActionWithFactsV2() throws Exception {
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
}