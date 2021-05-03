# Chapter 3 입출금 내역 분석기 확장판

## 요구사항
- 특정 입출금 내역을 검색할 수 있는 기능
- 텍스트 / HTML 등의 형식으로 리포팅

## 개방/폐쇄 원칙 
- 개방/폐쇄 원칙 open/closed principle (OCP) 는 **확장에는 열려있고, 변경에는 닫혀있다.**
    - 유연성과 유지보수성이 증가한다.

`특정조건의 입출금 내역을 검색하는 메소드 구현`

```java
public class BankStatementProcessor {
    private final List<BankTransaction> bankTransactions;

    public BankStatementProcessor(final List<BankTransaction> bankTransactions) {
        this.bankTransactions = bankTransactions;
    }

    // 특정 금액 이상의 은행 거래 내역  찾기
    public List<BankTransaction> findTransactionsGreaterThanEqual(final int amount) {
        final List<BankTransaction> result = new ArrayList<>();
        for (final BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getAmount() >= amount) {
                result.add(bankTransaction);
            }
        }
        return result;
    }

    // 특정 월의 입출금 내역 찾기
    public List<BankTransaction> findTransactionsInMonth(final Month month) {
        final List<BankTransaction> result = new ArrayList<>();
        for (final BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDate().getMonth() == month) {
                result.add(bankTransaction);
            }
        }
        return result;
    }

    // 특정 월이나 금액으로 입출금 내역 찾기
    public List<BankTransaction> findTransactionsInMonthAndGreater(final Month month, final int amount) {
        final List<BankTransaction> result = new ArrayList<>();
        for (final BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDate().getMonth() == month && bankTransaction.getAmount() >= amount) {
                result.add(bankTransaction);
            }

        }
        return result;
    }
}
```

> 위 코드들은 모두 잘 동작하지만, 한눈에 알 수 있는 문제는 비슷한 메소드들이 반복된다는 점이다.

**중복된 코드는 소프트웨어를 불안정하게 만든다**, 특히 요구사항이 자주 바뀔수록 영향이 커진다.

개방/폐쇄 원칙은 위와 같은 상황에 적용하기 적합하다. 이를 적용하면 코드를 직접 변경하지 않아도 해당 메소드나 클래스의 동작을 변경할 수 있다.

`BankTransactionFilter`

```java
@FunctionalInterface
public interface BankTransactionFilter {
    boolean test(BankTransaction bankTransaction);
}
```
> 비즈니스 로직을 담당하는 BankTransactionFilter 를 이용하여 OCP 를 구현한다. \n
> 자바 8 에서는 이런 동작을 하는 인터페이스가 이미 존재한다. (java.util.function.Predicate<T>)

`OCP 를 적용한 findTransactions 메소드`

```java
public class BankStatementProcessor {
    private final List<BankTransaction> bankTransactions;

    public BankStatementProcessor(final List<BankTransaction> bankTransactions) {
        this.bankTransactions = bankTransactions;
    }
    // 개방 / 폐쇄의 원칙 (OCP)를 준수..
    // 변경에는 닫혀있고, 확장에는 열려있다.
    public List<BankTransaction> findTransactions(final BankTransactionFilter bankTransactionFilter) {
        final List<BankTransaction> result = new ArrayList<>();
        for (final BankTransaction bankTransaction : bankTransactions) {
            if (bankTransactionFilter.test(bankTransaction)) {
                result.add(bankTransaction);
            }
        }
        return result;
    }
}
```
> 변경되는 부분만 BankTransactionFilter 를 이용해 결합을 제거했다. \n
> 기존 코드를 변경하지 않아도 새로운 기능을 추가할 수 있다.

`함수형 인터페이스 인스턴스와 람다식`

```java
public class BankTransactionIsInFebruaryAndExpensive implements BankTransactionFilter {

    @Override
    public boolean test(BankTransaction bankTransaction) {
        return bankTransaction.getDate().getMonth() == Month.FEBRUARY
            && bankTransaction.getAmount() >= 1_000;
    }
}

final List<BankTransaction> transactions = bankStatementProcessor.findTransactions(new BankTransactionIsInFebruaryAndExpensive());
```
> 함수형 인터페이스의 구현체를 다음과 같이 정의해서 사용할 수도 있지만, 람다 표현식을 사용하면 다음과 같이 간결한 표현이 가능하다.

```java
final List<BankTransaction> transactions = bankStatementProcessor.findTransactions(bankTransaction -> 
    bankTransaction.getDate().getMonth() == Month.FEBRUARY
    && bankTransaction.getAmount() >= 1_000);
```

## 인터페이스 문제
- OCP 를 적용하여 유연한 메소드를 만들었지만, 기존에 다른 메소드들은 어떻게 해야할까 ?
- 한 인터페이스에 모든 기능을 추가하는 **갓 인터페이스** 는 지양해야 한다.

`갓 인터페이스`

```java
import bank.BankTransaction;
import bank.filter.BankTransactionFilter;

interface BankTransactionProcessor {
    double calculateTotalAmount();

    double calculateTotalInMonth(Month month);

    List<BankTransaction> findTransactions(BankTransactionFilter bankTransactionFilter);
    // ...
}
```
> 위 인터페이스는 입출금 내역 분석기가 구현할 모든 기능을 포함한다.\n

- 위 인터페이스의 문제
    - 월 / 카테고리와 같이 도메인 객체에 종속된다.
        - 도메인 객체 세부 구현이 변경되면 인터페이스도 변경되어야 하며 불안정해진다.
    - 인터페이스가 너무 크기 때문에, 인터페이스가 변경되면 이를 구현한 코드도 자주 변경되어야 한다.

> 인터페이스는 가능하면 작게 만들어야 한다, 하지만 지나치게 작아도 유지보수에 방해된다. \n
> 기능이 너무 세밀한 인터페이스로 분산되어 찾기가 어렵고, 복잡도가 높아진다. \n
> 이를 **안티 응집도 anti-cohesion** 문제 라고 한다.

## 명시적 API 와 암묵적 API
- 이전에 구현한 BankTransactionProcessor 의 메소드들 중 findTransactions 와 같이 메소드를 쉽게 정의 가능한 상황에서
- findTransactionsGreaterThanEqual() 과 같이 구체적으로 메소드를 정의해야 하는가? 에 대한 의문이 생긴다.
- 이런 문제를 명시적 API vs 암묵적 API 제공 문제라고 한다.
- findTransactionsGreaterThanEqual 과 같은 메소드를 **명시적 API** 라고 한다.
    - 이는 어떤 동작을 수행하는지 잘 설명되어 있고 사용하기 쉽다.
    - 또한 가독성을 높이고 이해하기 쉽도록 서술되어 있다.
- findTransactions 과 같은 메소드를 암묵적 API 라고 한다.
    - 이런 API 는 처음에는 사용하기가 어렵다.
    - 어떤 동작을 수행하는지 알기가 쉽지 않다.
    - 문서화를 잘 해 두어야한다.

`명시적 API 와 암묵적 API 를 혼용`

```java
@FunctionalInterface
public interface BankTransactionSummarizer {
    double summarize(double accumulator, BankTransaction bankTransaction);
}

public class BankStatementProcessor {
    private final List<BankTransaction> bankTransactions;

    public BankStatementProcessor(final List<BankTransaction> bankTransactions) {
        this.bankTransactions = bankTransactions;
    }

    public double calculateTotalAmount() {
        double total = 0;
        for (final BankTransaction bankTransaction : bankTransactions) {
            total += bankTransaction.getAmount();
        }
        return total;
    }

    public double calculateTotalInMonth(final Month month) {
        return summarizeTransactions(
            (acc, bankTransaction) ->
                bankTransaction.getDate().getMonth() == month ? acc + bankTransaction.getAmount() : acc
        );
    }

    public double calculateTotalForCategory(final String category) {
        return summarizeTransactions(
            (acc, bankTransaction) ->
                bankTransaction.getDescription().equals(category) ? acc + bankTransaction.getAmount() : acc
        );
    }

    // 개방 / 폐쇄의 원칙 (OCP)를 준수..
    // 변경에는 닫혀있고, 확장에는 열려있다.
    public double summarizeTransactions(final BankTransactionSummarizer bankTransactionSummarizer) {
        double result = 0d;
        for (final BankTransaction bankTransaction : bankTransactions) {
            result = bankTransactionSummarizer.summarize(result, bankTransaction);
        }
        return result;
    }


    // 특정 금액 이상의 은행 거래 내역  찾기
    public List<BankTransaction> findTransactionsGreaterThanEqual(final int amount) {
        return findTransactions(bankTransaction -> bankTransaction.getAmount() >= amount);
    }

    // 특정 월의 입출금 내역 찾기
    public List<BankTransaction> findTransactionsInMonth(final Month month) {
        return findTransactions(bankTransaction -> bankTransaction.getDate().getMonth() == month);
    }

    // 특정 월이나 금액으로 입출금 내역 찾기
    public List<BankTransaction> findTransactionsInMonthAndGreater(final Month month, final int amount) {
        return findTransactions(
            bankTransaction ->
                bankTransaction.getDate().getMonth() == month && bankTransaction.getAmount() >= amount
        );
    }

    // 개방 / 폐쇄의 원칙 (OCP)를 준수..
    // 변경에는 닫혀있고, 확장에는 열려있다.
    public List<BankTransaction> findTransactions(final BankTransactionFilter bankTransactionFilter) {
        final List<BankTransaction> result = new ArrayList<>();
        for (final BankTransaction bankTransaction : bankTransactions) {
            if (bankTransactionFilter.test(bankTransaction)) {
                result.add(bankTransaction);
            }
        }
        return result;
    }
}
```

> 명시적 API 냐, 암묵적 API 냐의 선택은 어떤 연산이 주로 사용되는가 등 상황에 따라 적절하게 사용해야 한다. \n
> 무조건 둘중 하나가 옳은것이 아닌 적절한 선택을 하는 것이 중요하다.

## 도메인 클래스와 원시 값
- BankTransactionSummarizer 클래스의 핵심 연산의 반환값은 'double' 이다.
- 이는 일반적으로 좋은 방법이 아니다.
- 원시 값을 반환하면 다양한 결과를 반환할 수 없어 유연성이 떨어지게 된다.
- double 을 감싸는 새로운 도메인 클래스를 만들어 사용하면 이런 문제가 해결된다.

> 위와 같은 기법을 활용하면 도메인 간의 결합을 줄이고 / 요구사항이 변경되어도 영향을 최소화 할 수 있다.

## 다양한 형식으로 내보내기
- OCP 와 인터페이스를 활용하여 추가 요구사항들에 대응했다.
- 이번에는 입출금 목록을 텍스트 / HTML / JSON 등의 형식으로 출력해야 한다.

### 도메인 객체 소개
- 숫자
    - 구현이 가장 단순하여 쉽지만 유연성이 떨어진다.
- 컬렉션
    - Iterable 타입으로 반환한다면 상황에 맞춰 처리가 가능하지만 컬렉션 타입만 취급할 수 있다.
- 도메인 객체
    - 사용자에게 출력할 요약 정보를 의미하는 SummaryStatistics 라는 새로운 도메인 객체를 사용한다.
    - 새로운 요구사항이 생겨 추가 정보를 낼야 한다면 기존 코드를 수정할 필요 없이 유연하게 대처가 가능하다.
- 복잡한 도메인 객체
    - Report 처럼 좀 더 일반적 이고 거래 내역 컬렉션 등 다양한 결과를 저장하는 필드를 가지는 도메인 객체를 만들 수 있다.
    - 요구사항에 따라 사용할 도메인 객체가 변경된다. 
    - 어떤 상황이 되어도 **생산** 과 **소비** 가 결합되지 않는다는 장점이 있다.

`SummaryStatistics`

```java
public class SummaryStatistics {
    private final double sum;
    private final double max;
    private final double min;
    private final double average;

    public SummaryStatistics(final double sum, final double max, final double min, final double average) {
        this.sum = sum;
        this.max = max;
        this.min = min;
        this.average = average;
    }

    public double getSum() {
        return sum;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getAverage() {
        return average;
    }
}
```

### Exporter 인터페이스
- 내보낼 형식에 맞게 이를 구현하는 API 를 Exporter 인터페이스를 정의해 결합도를 낮추어 구현한다.

`잘못된 예`

```java
import bank.result.SummaryStatistics;

public interface Exporter {
    void export(SummaryStatistics summaryStatistics);
}
```
> 위 인터페이스는 안좋은 예이다. \n
> 얼핏 보면 별 문제가 없어 보이지만 void 반환 형식은 아무런 도움이 되지 않는다. (일반 적으로 void 메소드는 부수효과를 일으킨다.) \n
> 인터페이스를 통해 얻을수 있는 정보가 적다. \n
> 반환타입이 void 라면 결과를 테스트하기도 어렵다.

`좋은 예`

```java
import bank.result.SummaryStatistics;

public interface Exporter {
    String export(SummaryStatistics summaryStatistics);
}
```
> String 을 반환하여 이를 출력 / 파일에 저장 / 또는 전송 하는 등 활용이 가능하며 테스트도 쉽게 가능해진다.

`HTMLExporter`
- HTML 형식을 내보내는 Exporter 인터페이스의 구현체

```java
public class HtmlExporter implements Exporter {

    @Override
    public String export(SummaryStatistics summaryStatistics) {
        String result = "<!doctype html>";
        result += "<html lang='en'>";
        result += "<head><title>Bank Transaction Report</title></head>";
        result += "<body>";
        result += "<ul>";
        result += String.format("<li><strong>The Sum is</strong>: %s</li>", summaryStatistics.getSum());
        result += String.format("<li><strong>The Average is</strong>: %s</li>", summaryStatistics.getAverage());
        result += String.format("<li><strong>The Max is</strong>: %s</li>", summaryStatistics.getMax());
        result += String.format("<li><strong>The Min is</strong>: %s</li>", summaryStatistics.getMin());
        result += "</ul>";
        result += "</body>";
        result += "</html>";
        return result;
    }
}
```

## 예외 처리

### 예외를 사용해야 하는 이유
- 예를 들어 CSVParser 가 파싱 문제 처리중 문서가 잘못된 형식으로 구성되어 있을 수 있다.
- C 프로그래밍에서는 수많은 If 문을 이용해 오류 코드를 반환하는 방식을 사용했다.
- 이는 이해하기 어려워져 유지보수 하기 어렵다.
- 또한 실제 반환값인지 오류 값인지 구분하기 힘들고 비즈너스 로직과 제어 흐름이 섞여 유지보수성이 떨어지고 테스트하기 힘들어진다.

- 자바는 예외를 일급 언어 기능으로 추가하여 다음과 같은 장점을 제공한다.
1. 문서화
    - 메소드 시그니처에 예외 지원
2. 형식 안전성
    - 개발자가 예외 흐름을 처리중인지 형식 시스템이 검증한다. 
3. 관심사 분리
    - 비즈니스 로직과 예외 복구 로직이 try/catch 블록으로 인해 구분된다.

### 자바의 예외
- checked exception
    - 복구 대상이 되는 예외
    - throw 하거나 try/catch 블록으로 처리 해야만 한다.
- unchecked exception
    - 예상치 못한 예외
    - throw 하거나 try/catch 블록으로 처리하지 않아도 된다.

### 예외의 패턴과 안티패턴
- 어떤 상황에 어떤 종류의 예외를 사용해야하는지에 대한 정답은 없다.
- 이는 상황에 따라 달라진다.
- 구현하는 API 에서 예외 발생시 복구 하도록 강제할 것인지를 고민해 보면 답이 나온다.
- 일반적으로 비즈니스 로직 검증시 발생한 문제는 try/catch 블록을 줄이기 위해 unchecked exception 을 사용한다.

> 대다수의 예외를 unchecked 로, 반드시 필요한 경우에만 checked 로 선언해야 한다.

`안티패턴 1 - 과도하게 세밀한 예외`

```java
public class OverlySpecificBankStatementValidator {

    private String description;
    private String date;
    private String amount;

    public OverlySpecificBankStatementValidator(final String description, final String date, final String amount) {
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public boolean validate() {
        if (this.description.length() > 100) {
            throw new DescriptionTooLongException();
        }

        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(this.date);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormat();
        }

        if (parsedDate.isAfter(LocalDate.now())) {
            throw new DateInTheFutureException();
        }

        try {
            Double.parseDouble(this.amount);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException();
        }
        return true;
    }
}
```
- 위는 너무 과도하게 세밀한 예외를 사용한 예 이다.
- 이는 각 예외에 적합하고 정확한 복구 처리가 가능하지만 너무 과도한 설정 작업이 필요하고, 많은 예외를 선언 해야 하므로 생산성이 현저하게 떨어진다.

`안티패턴 2 - 과도하게 단순한 예외`

```java
public class OverlySpecificBankStatementValidator {

    private String description;
    private String date;
    private String amount;

    public OverlySpecificBankStatementValidator(final String description, final String date, final String amount) {
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public boolean validate() {
        if (this.description.length() > 100) {
            throw new IllegalStateException();
        }

        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(this.date);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException();
        }

        if (parsedDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException();
        }

        try {
            Double.parseDouble(this.amount);
        } catch (NumberFormatException e) {
            throw new IllegalStateException();
        }
        return true;
    }
}
```
- 이전과 반대로 너무 과도하게 단순한 예외를 사용한 예이다.
- 모두 동일한 예외로 처맇나다면 구체적인 복구 로직처리가 불가능하다.

### 노티피케이션 패턴
- 노티피케이션 패턴은 너무 많은 미확인 예외를 사용하는 상황에 적절한 해결책을 제공한다.
- 이는 도메인 클래스로 오류를 수집한다.

`오류를 수집하는 도메인 클래스`

```java
public class Notification {
    private final List<String> errors = new ArrayList<>();

    public void addError(final String message) {
        errors.add(message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String errorMessage() {
        return errors.toString();
    }

    public List<String> getErrors() {
        return errors;
    }
}
```

`노티피케이션 패턴 적용`

```java
public class NotificationBankStatementValidator {

    private String description;
    private String date;
    private String amount;

    public NotificationBankStatementValidator(final String description, final String date, final String amount) {
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public Notification validate() {
        final Notification notification = new Notification();
        if (this.description.length() > 100) {
            notification.addError("The description is too long");
        }

        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(this.date);

            if (parsedDate.isAfter(LocalDate.now())) {
                notification.addError("date cannot be in the future");
            }
        } catch (DateTimeParseException e) {
            notification.addError("Invalid format for date");
        }


        try {
            Double.parseDouble(this.amount);
        } catch (NumberFormatException e) {
            notification.addError("Invalid format for amount");
        }

        return notification;
    }
}
```

- 도메인 클래스를 활용해 이전에는 불가능 했던 한번에 여러 오류도 수집할 수 있게 된다.

### 예외 가이드
- 예외를 무시하지 말것
    - 원인을 알 수 없다고 예외를 무시해서는 안된다.
    - 최소한 로그정도는 남겨야한다.
    - 처리할 방법이 없다면 unchecked 예외로 변환하여 던져야 한다.
- 일반적인 예외는 잡지 말것
    - 추상화된 상위 예외는 되도록 잡지 말고, 보다 구체적인 예외를 잡아야 한다.
    - 또한 catch 문 작성시 예외의 순서에 유의해야 한다.
- 예외를 문서화 할것
- 특정 구현에 종속된 예외를 주의할 것
    - 특정 구현에 종속된 예외를 던지면 API 의 캡슐화가 깨진다.
    - ex) OracleException
- 예외로 흐름을 제어하지 말것
    - 예외로 흐름을 제어하는 것은 정말 피해야하는 기법 중 하나이다.
    - 이는 GOTO 문으로 흐름을 제어하는 것과 다름 없다.

## 예외 대안

### null
- 이는 되도록 사용하지 않아야 한다.
- 반드시 사용하지 않을 이유는 없다. 때로는 비즈니스 null 이 필요할 때도 존재한다.

### null object pattern
- 객체가 존재하지 않을때 null 참조를 반환하는 대신 비어있음을 의미하는 객체를 반환하는 기법
- 의도치 않은 NPE 와 불필요한 null 체크를 피할수 있다.
- 하지만 나중에 버그를 찾기 더 어려워 질 수 있다.

### Optional<T>
- 자바8 부터 제공하는 java.util.Optional<T> 
- 값이 없는 상태를 명시적으로 처리하는 다양한 메소드를 제공한다.
- 이는 버그를 줄이는데 큰 도움이 된다.

### Try<T>
- 성공 또는 실패를 가리키는 데이터 형식이다.
- 이는 값이 아닌 연산에 적용한다는 점이 다르다.
- JDK 에서는 지원하지 않는다.

## 정리
- OCP 를 이용하면 코드를 변경하지 않고 메소드/클래스의 동작을 바꿀 수 있다.
- OCP 를 이용하면 기존 코드의 재사용성을 높이고 응집도가 높아져 코드 유지보수성이 개선된다.
- 많은 메소드를 포함하는 **갓 인터페이스** 는 복잡도와 결합도를 높인다.
- 너무 과도하게 세밀한 인터페이스를 지양해야 한다.
- API 의 가독성을 높이기 위해 명시적인 API 를 작성해야 한다.
- void 반환은 테스트하기 어렵다.
- 검증 로직은 Validator 클래스를 별도로 만드는 것이 좋다.
    - 재사용성
    - 독립적인 테스트 가능
    - SRP 준수
- 자바의 예외는 문서화 / 형식 안전성 / 관심사 분리를 촉진한다.
- 정말 필요한 경우에만 checked 예외를 사용해야 한다.
- 노티피케이션 패턴을 활용하면 도메인 클래스로 오류를 수집할 수 있다.
- 예외를 무시하거나 일반적인 예외를 잡으면 근본적인 문제를 파악하기 힘들다.