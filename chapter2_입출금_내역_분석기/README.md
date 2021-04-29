# Chapter 2 입출금 내역 분석기

## 요구사항
- 은행 입출금 내역의 총 수입과 총 지출은 각각 얼마인가 ? 결과가 양수인가 / 음수인가
- 특정 달엔 몇 건의 입출금 내역이 발생했는가 ?
- 지출이 가장 높은 상위 10건은 무엇인가 ?
- 돈을 갖아 많이 소비하는 항목을 무엇인가 ?

## KISS 원칙
- **KISS (Keep It Short and Simple)** 원칙을 이용해 하나의 클래스로 구현

```java
public class BankTransactionAnalyzerSimple {
    private static final String RESOURCES = "src/main/resources";

    public static void main(final String[] args) throws IOException {

        final Path path = Paths.get(RESOURCES + args[0]);
        final List<String> lines = Files.readAllLines(path);
        double total = 0d;

        for (final String line : lines) {
            final String[] columns = line.split(",");
            final double amount = Double.parseDouble(columns[1]);
            total += amount;
        }

        System.out.println("The total for all transactions is" + total);
    }
}
```

- 특정달엔 몇건의 입출금 내역이 발생하는가 ?
    - 이를 대응하기 위해 코드를 복사 / 붙여넣기로 구현을 해야한다.

```java
public class BankTransactionAnalyzerSimple {
    private static final String RESOURCES = "src/main/resources";

    // 1월 입출금 내역 합계 계산 (기존 소스를 복사하는 방식)
    public void calculate(final String[] args) throws IOException {
        final Path path = Paths.get(RESOURCES + args[0]);
        final List<String> lines = Files.readAllLines(path);
        double total = 0d;
        final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (final String line : lines) {
            final String[] columns = line.split(",");
            final LocalDate date = LocalDate.parse(columns[0], DATE_PATTERN);

            // 1월 일 경우 합계에 포함
            if (date.getMonth() == Month.JANUARY) {
                final double amount = Double.parseDouble(columns[1]);
                total += amount;
            }

        }

        System.out.println("The total for all transactions in January is" + total);
    }
}
```

### final 변수에 대한 논란 ? 논쟁 ?
- final 변수를 사용하면 값을 재 할당할 수 없다.
- 이는 사용에 따른 장단점이 존재하기 때문에 사용여부는 조직과 프로젝트에 따라 달라진다.
- 코드에서 가능하면 많은 변수를 final 로 표현한다면 어떤 객체의 상태가 바뀔 수 있는지 명확하게 구분할 수 있다.
- 하지만 final 키워드를 사용한다고 완전한 **불변** 이 되는것은 아님을 명심해야 한다.
- 또한 인터페이스 메소드 파라미터에 final 은 사용해도 구현부가 없기 때문에 무용지물이 된다.

## 유지보수와 안티패턴
- 코드 유지보수성을 높히기
    - 특정 기능을 담당하는 코드를 쉽게 찾을 수 있어야 한다.
    - 코드가 어떤 일을 수행하는지 쉽게 이해할 수 있어야 한다.
    - 새로운 기능을 추가하거나 기존 기능을 쉽게 제거할 수 있어야 한다.
    - 캡슐화가 잘되어 있어야 한다.
> 이를 평가하기 가장 좋은 방법은 코드를 구현후 6개월뒤 다른 동료가 해당 코드를 사용해야 하는 상황이 생긴 경우이다.

- 요구사항이 생길때마다 복사/붙여넣기로 해결하는 방식은 효과적인 방법이 아니며 **안티 패턴** 이라고 부른다.
    - 하나의 **갓 클래스** 때문에 코드 이해도가 떨어진다.
    - 코드 중복 때문에 코드가 불안정하고 변화에 쉽게 망가진다.

> DRY 원칙 (중복 배제) 을 준수해야 한다

### 갓 클래스
- 하나의 클래스에 모든 구현을 하면 결국에는 하나의 거대한 클래스가 탄생하며 이해하기 힘들어진다.
- 이런 문제를 **갓 클래스 안티패턴** 이라고 부른다.

## 단일 책임 원칙
- **단일 책임 원칙 (SRP)** 는 쉽게 관리하고 유지보수하는 코드를 구현하는데 도움을 주는 소프트웨어 개발 지침이다.
- 한 클래스는 한 기능만 책임 진다.
- 클래스가 변경되어야 하는 이유는 오직 하나여야 한다.
> SRP 는 일반적으로 클래스와 메소드 레벨에 적용하며 **특정 동작이나 개념, 카테고리와 관련** 된다.

`단일 책임 원칙에 따라 파싱 로직을 담당하는 클래스를 추출`

```java
public class BankStatementCSVParser {
    private final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    public BankTransaction parseFrom(final String line) {
        final String[] columns = line.split(",");

        final LocalDate date = LocalDate.parse(columns[0], DATE_PATTERN);
        final double amount = Double.parseDouble(columns[1]);
        final String description = columns[2];

        return new BankTransaction(date, amount, description);
    }
    
    public List<BankTransaction> parseLinesFrom(final List<String> lines) {
        List<BankTransaction> bankTransactions = List.of();
        for (final String line : lines) {
            bankTransactions.add(parseFrom(line));
        }
        return bankTransactions;
    }
}
```

`입출금 도메인 클래스`

```java
public class BankTransaction {
    private final LocalDate date;
    private final double amount;
    private final String description;

    public BankTransaction(LocalDate date, double amount, String description) {
        this.date = date;
        this.amount = amount;
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "BankTransaction{" +
            "date=" + date +
            ", amount=" + amount +
            ", description='" + description + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankTransaction that = (BankTransaction) o;
        return Double.compare(that.amount, amount) == 0 && Objects.equals(date, that.date) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, amount, description);
    }
}
```

> 도메인 클래스는 프로그램의 다른 부분에서 해당 도메인이라는 의미를 공유할 수 있어 매우 유용하다.

`BankStatementCSVParser 를 활용한 리팩토링`

```java
public class BankTransactionAnalyzerSimple {
    private static final String RESOURCES = "src/main/resources";
    
    public void refactorSimple(final String[] args) throws IOException {
        final BankStatementCSVParser bankStatementParser = new BankStatementCSVParser();

        final Path path = Paths.get(RESOURCES + args[0]);
        final List<String> lines = Files.readAllLines(path);
        final List<BankTransaction> bankTransactions = bankStatementParser.parseLinesFrom(lines);

        System.out.println("The total for all transactions is " + calculateTotalAmount(bankTransactions));
        System.out.println("Transactions in January " + selectInMonth(bankTransactions, Month.JANUARY));
    }

    // 총 가격 합산
    public static double calculateTotalAmount(final List<BankTransaction> bankTransactions) {
        double total = 0d;
        for (BankTransaction bankTransaction : bankTransactions) {
            total += bankTransaction.getAmount();
        }
        return total;
    }

    // 월별 목록 추출
    public static List<BankTransaction> selectInMonth(final List<BankTransaction> bankTransactions, final Month month) {
        final List<BankTransaction> bankTransactionsInMonth = List.of();
        for (BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDate().getMonth() == month) {
                bankTransactionsInMonth.add(bankTransaction);
            }
        }
        return bankTransactionsInMonth;
    }
}
```

> 메소드 구현시에는 **놀람 최소화 원칙 (principle of least surprise)** 를 따라야 한다. \n
> 코드를 보고 무슨일이 일어나는지 명확하게 이해할 수 있도록 구현해야 한다.

## 응집도
- 소프트웨어 엔지니어링과 **응집도** 는 코드 구현에서 중요한 특성이다.
- 응집도란 서로 **어떻게 관련되어 있는지** 를 가리킨다.
- 즉 클래스나 메소드의 책임이 얼마나 강하게 연결되어있는지 측정한다.

> 높은 응집도는 누구나 쉽게 코드를 찾고 / 이해하고 / 사용할 수 있도록 한다.

- 현재 프로그램의 진입점인 BankTransactionAnalyzerSimple 클래스를 보면, 계산 관련 작업은 파싱 / 결과 전송과 연관이 없는 응집도가 떨어지는 사례이다.

`계산 연산을 담당하는 BankStatementProcessor`

```java
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
        double total = 0;
        for (final BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDate().getMonth() == month) {
                total += bankTransaction.getAmount();
            }
        }
        return total;
    }

    public double calculateTotalForCategory(final String category) {
        double total = 0;
        for (BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDescription().equals(category)) {
                total += bankTransaction.getAmount();
            }
        }
        return total;
    }
}
```

> 메소드 시그니쳐도 단순해지고 응집도도 높아져 이해하기 쉽게 된다.

`BankStatementProcessor 를 활용한 리팩토링`

```java

public class BankTransactionAnalyzerSimple {
    private static final String RESOURCES = "src/main/resources";

    public void refactorSimpleV2(final String[] args) throws IOException {
        final BankStatementCSVParser bankStatementParser = new BankStatementCSVParser();

        final Path path = Paths.get(RESOURCES + args[0]);
        final List<String> lines = Files.readAllLines(path);
        final List<BankTransaction> bankTransactions = bankStatementParser.parseLinesFrom(lines);
        final BankStatementProcessor bankStatementProcessor = new BankStatementProcessor(bankTransactions);

        collectSummary(bankStatementProcessor);
    }

    // 결과 출력
    private static void collectSummary(final BankStatementProcessor bankStatementProcessor) {
        System.out.println("The total for all transactions is " + bankStatementProcessor.calculateTotalAmount());
        System.out.println("The total for transactions in January is " + bankStatementProcessor.calculateTotalInMonth(Month.JANUARY));
        System.out.println("The total salary received is " + bankStatementProcessor.calculateTotalForCategory("Salary"));
    }
}
```

### 클래스 수준의 응집도
- 일반적으로 여섯가지 방법으로 그루핑한다.
    - 기능
    - 정보
    - 유틸리티
    - 논리
    - 순차
    - 시간

`기능`
- BankStatementCSVParser 를 구현할때 기능이 비슷한 메소드끼리 그루핑
- 이런식으로 함께 사용하는 메소드들 끼리 그룹화 하면 이해하기도 쉽고 응집도를 높인다.
> 기능 응집은 한개의 메소드를 같은 클래스를 너무 과도하게 만들 수 있다는 약점이 있다.

`정보`
- 같은 데이터 / 도메인 객체를 처리하는 메소드를 그루핑
- BankTransaction 객체를 만들고 CRUD 를 구행하는 연산을 제공하는 클래스

```java
public class BankTransactionDAO {

    public BankTransaction create(final LocalDate date, final double amount, final String description) {
        throw new UnsupportedOperationException();
    }

    public BankTransaction read(final long id) {
        throw new UnsupportedOperationException();
    }

    public BankTransaction update(final long id) {
        throw new UnsupportedOperationException();
    }

    public void delete(final BankTransaction bankTransaction) {
        throw new UnsupportedOperationException();
    }
}
```

> 이런 패턴은 일반적으로 DAO (데이터 접근 객체) 라고 부르며, 데이터베이스와 상호작용할 때 흔히 볼 수 있다. \n
> 정보 응집은 여러 기능을 그룹화 하지만 필요한 일부 기능을 사용하기 위해 클래스 전체를 디펜던시로 추가해야 한다는 약점이 있다.

`유틸리티`
- 유틸리티는 위 2가지 와는 성격이 다르다.
- 특정 메소드가 어디에 속해야할지 어려운 경우 유틸리티 클래스에 추가하는 방식을 택한다.
> 이는 낮은 응집도로 이어지므로 될수 있으면 자제하는것이 좋다.

`논리`
- CSV, JSON, XML 의 자료를 파싱하는 논리적인 그루핑

```java
import bank.BankTransaction;

public class BankTransactionParser {
    public BankTransaction parseFromCSV(final String line) {
        // ..
    }

    public BankTransaction parseFromJSON(final String line) {
        // ..
    }

    public BankTransaction parseFromXML(final String line) {
        // ..
    }
    
    // ...
}
```

> '파싱' 이라는 논리로 그룹화 되었지만 서로 관련이 없으며 하나의 클래스가 4가지 책임을 갖게 되므로 SRP 를 위배한다.

`순차`
- 입출력이 순차적으로 흐르는 것을 응집하여 순차 응집이라 부른다.
- 여러 동작이 어떻게 함께 수행되는지 쉽게 이해할 수 있다.
> 순차 응집을 적용하면 한 클래스를 변경해야 할 이유가 여러개가 되므로 SRP 를 위배한다.

`시간`
- 시간과 관련된 연산을 그룹화 한다.
- 리소스 초기화 / 뒷정리 작업 등을 포함하는 클래스가 그 예이다.

### 응집도 수준과 장단점

| 응집도 수준 | 장점 | 단점 |
| --- | --- | --- |
| 기능 (높음) | 이해하기 쉬움 | 너무 단순한 클래스 생성 |
| 정보 (중간) | 유지보수 쉬움 | 불필요한 디펜던시 |
| 순차 (중간) | 관련 동작 찾기 쉬움 | SRP 위반 |
| 논리 (중간) | 높은 수준의 카테고리화 제공 | SRP 위반 |
| 유틸리티 (낮음) | 간단히 추가 가능 | 클래스 책임 파악 힘듦 |
| 시간 (낮음) | 판단 불가 | 이해 및 사용이 힘듦 |

### 메소드 레벨 응집도
- 클래스 뿐 아니라 메소드에도 적용이 가능하다.
- 메소드가 다양한 기능을 수행한다면 이해하기 힘들어 진다.
- 일반적으로 클래스 나 메소드 파라미터의 여러 필드를 바꾸는 조건문이 여러개 존재한다면 이는 더 작은 조각으로 분리해야 한다.

## 결합도
- **결합도** 는 한 기능이 다른 크래스에 얼마나 의존하고 있는지를 판단한다.
- 어떤 클래스를 구현하는데 얼마나 많은 다른 클래스를 참조 했는가
- 시계에 비유하면 이해하기 쉽다.

> 시계의 내부 동작을 몰라도 사람은 시간을 읽는데 문제가 없다. \n
> 사람은 시계 내부 구조에 의존하지 않기 때문이다.

- 결합도는 코드가 서로 **어떻게 의존하는지** 와 관련이 있는 척도 이다.

`인터페이스를 활용해 결합도를 낮추기`

```java
public interface BankStatementParser {
    BankTransaction parseFrom(String line);
    List<BankTransaction> parseLinesFrom(List<String> lines);
}

public class BankStatementCSVParser implements BankStatementParser {
    private final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public BankTransaction parseFrom(final String line) {
        final String[] columns = line.split(",");

        final LocalDate date = LocalDate.parse(columns[0], DATE_PATTERN);
        final double amount = Double.parseDouble(columns[1]);
        final String description = columns[2];

        return new BankTransaction(date, amount, description);
    }

    @Override
    public List<BankTransaction> parseLinesFrom(final List<String> lines) {
        List<BankTransaction> bankTransactions = List.of();
        for (final String line : lines) {
            bankTransactions.add(parseFrom(line));
        }
        return bankTransactions;
    }
}


public class BankTransactionAnalyzerSimple {
    private static final String RESOURCES = "src/main/resources";
    
    // 결과 출력
    private static void collectSummary(final BankStatementProcessor bankStatementProcessor) {
        System.out.println("The total for all transactions is " + bankStatementProcessor.calculateTotalAmount());
        System.out.println("The total for transactions in January is " + bankStatementProcessor.calculateTotalInMonth(Month.JANUARY));
        System.out.println("The total salary received is " + bankStatementProcessor.calculateTotalForCategory("Salary"));
    }

    // 특정 파서와의 결합 제거
    public void analyze(final String fileName, final BankStatementParser bankStatementParser) throws IOException {
        final Path path = Paths.get(RESOURCES + fileName);
        final List<String> lines = Files.readAllLines(path);
        final List<BankTransaction> bankTransactions = bankStatementParser.parseLinesFrom(lines);
        final BankStatementProcessor bankStatementProcessor = new BankStatementProcessor(bankTransactions);

        collectSummary(bankStatementProcessor);
    }
}
```

> BankTransactionAnalyzerSimple 는 더이상 특정 파서에 의존하지 않음 \n 
> 코드를 구현할때는 **결합도를 낮춰야 한다.**

## 정리
- 갓 클래스와 코드 중복은 추론 및 유지보수를 어렵게 만든다.
- 단일 책임 원칙은 유지보수 하기 쉬운 코드를 구현하는데 도움을 준다.
- 응집도는 클래스나 메소드의 책임이 얼마나 강하게 연관되어 있는지를 가리킨다.
- 결합도는 클래스가 다른 코드 부분에 얼마나 의존하고 있는지를 가리킨다.
- 높은 응집도와 낮은 결합도를 유지해야 한다.
