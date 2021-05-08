# Chapter 4 문서 관리 시스템

## 요구사항
- 기존 환자 정보 파일을 읽어 색인 및 검색 기능을 제공해야 한다.

`문서의 종류`
- 리포트
    - 환자 수술과 관련된 상담 내용을 기록한 문서
- 우편물
    - 특정 주소로 발송되는 텍스트 문서
- 이미지
    - 치아와 잇몸 엑스레이 사진

## 설계 작업
- 문서 관리 시스템은 필요에 따라 문서를 임포트해 내부 문서 저장소에 추가한다.

`DocumentManagementSystem`

```java
public class DocumentManagementSystem {
    private final List<Document> documents = new ArrayList<>();

    // 파일 경로를 받아 해당 파일을 임포트한다.
    public void importFile(final String path) throws IOException {

    }

    // 문서관리 시스템에 저장된 모든 문서를 반환한다.
    public List<Document> contents() {
        return documents;
    }
}
```

## 임포터
- 다양한 종류의 문서를 임포트 하는 것이 문서 관리 시스템의 핵심 기능
- 파일의 확장자로 어떻게 임포트 할지 결정할 수 있다.

`문서의 종류 및 확장자`
- 우편물 : .letter
- 리포트 : .report
- 이미지 : .jpg

`switch 문을 활용한 확장자 구분`
```java
switch (extension) {
    case "letter" :
        break;
    case "report" :
        break;
    case "jpg" :
        break;
    default :
        throw new UnknownFileTypeExcetion();
}
```
- 위와 같은 코드는 구현은 간단하지만 확장성이 떨어진다.
- 다른 종류의 파일이 추가 될때 마다 switch 문이 늘어나게 된다.
  
`Importer`

```java
public interface Importer {
    Document importFile(File file) throws IOException;
}
```
- 위 문제를 해결하기위해 Importer 인터페이스를 추가하고, 다양한 파일 별로 구현체를 생성하는 방법

## Document 클래스
- 각 문서는 검색 가능한 다양한 속성을 가지고 있다.
- 문서의 종류마다 포함하는 속성이 달라진다.
- 가장 간단한 방법을 Map<String, String> 타입을 사용 하는 것이다.
- 직접 컬렉션을 사용하기 보다는 도메인 클래스를 정의해서 사용하는 것이 좋다.
- 좋은 개발자는 **유비쿼터스 언어** 를 사용한다.
- 고객과 대화할때 사용하는 용어를 프로그램의 코드와 같은 의미로 사용하면 유지보수가 쉬워진다.
- 이때 사용한 어휘를 코드와 매핑하면 코드의 어떤 부분을 바꾸어야 하는지 쉽게 알 수 있는데 이를 **발견성 (discoverability)** 이라고 한다.

`유비 쿼터스 언어`
- 개발자와 사용자 모두가 사용할 수 있도록 설계, 공유된 공통 언어

### 강한 형식의 원칙
- 강한 형식을 이용하면 데이터의 사용방법을 규제 가능
- Document 클래스는 불변 클래스로 구현한다.
- 클래스를 생성한 다음에는 클래스의 속성을 변경할 수 없다.
- Importer 에 의해 Document 가 생성된다.
- 불변성을 유지하면 Document 속성에서 오류가 나면 Importer 의 구현을 살펴보아야 하는 것으로 오류의 범위가 좁혀진다.
- 또한 안전하게 색인 및 캐싱을 할 수 있다.
- 만약 Map 을 상속받는 형태로 Document 를 구현하게 되면 불변성이 깨지게 된다.
- 또한 Document 클래스에서 제공하고 싶지 않은 인터페이스도 노출 되는 문제가 있다.
    - 상속보다는 합성을 하는 형태로 구현해야 한다.
    
> 인터페이스 설계시 가장 중요한 점은 불필요한 기능은 제한 해야 한다.

`Document`

```java
public class Document {
    private final Map<String, String> attributes;

    Document(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(final String attributeName) {
        return attributes.get(attributeName);
    }
}
```
- 일반적으로 public 접근 제어자를 사용하지만 Document 클래스의 경우 package 레벨의 접근 제어자를 사용 했다.
- 이는 오직 문서관리 시스템에서만 Document 를 생성할 수 있도록 제한함으로 인해 시스템 안전성이 올라간다.

### 속성 및 계층
- Document 클래스의 속성에 String 타입을 사용했다.
- 속성을 텍스트로 저장하면 텍스트로 속성을 검색할 수 있고, Importer 의 종류와 상관없이 모든 속성이 일반적인 형식을 갖도록 만드는 의도
- 일반적으로 String 으로 정보를 전달하는 것은 좋지 않은 방법으로 알려져 있다.
> 이를 강한 형식에 빗대어 문자화 형식 이라고 한다.

- Importer 의 계층 그대로 Document 에 적용이 가능하다.
- Report 가 Document 를 상속하도록 구현할 수 있다.
- 이런 방식을 사용하면 상속으로 인해 기본적인 무결성 검사가 가능하다.
> 만약 클래스 계층으로 인해 얻는 효과가 없다면, 계층을 추가하지 않는 것이 좋다. \n
> KISS 원칙 으로 설명이 가능하다.

## 임포터 구현

`Attributes`

```java
public abstract class Attributes {
    public static final String PATH = "path";
    public static final String PATIENT = "patient";
    public static final String ADDRESS = "address";
    public static final String BODY = "body";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String TYPE = "type";
    public static final String AMOUNT = "amount";
}
```
- 상수를 (문자열/숫자) 코드에 직접 삽입 하는 것을 매직 넘버라고 한다.
- 이는 오타로 인한 휴먼 에러가 발생할 수 있다.
- 상수를 변수로 선언해서 사용하면 이를 방지할 수 있다.

`ImageImporter`

```java
public class ImageImporter implements Importer {

    @Override
    public Document importFile(final File file) throws IOException {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(PATH, file.getPath());

        final BufferedImage image = ImageIO.read(file);
        attributes.put(WIDTH, String.valueOf(image.getWidth()));
        attributes.put(HEIGHT, String.valueOf(image.getHeight()));
        attributes.put(TYPE, "IMAGE");

        return new Document(attributes);
    }
}
```


## 임포터 등록

```java
public class DocumentManagementSystem {
    private final Map<String, Importer> importers = new HashMap<>();
    private final List<Document> documents = new ArrayList<>();

    public DocumentManagementSystem() {
        importers.put("letter", new LetterImporter());
        importers.put("report", new ReportImporter());
        importers.put("jpg", new ImageImporter());
        importers.put("invoice", new InvoiceImporter());
    }
}
```

## 검색 기능
- 구글 검색엔진 처럼 최적화된 검색까진 아니더라도 다양한 정보를 검색 할 수 있어야 한다.
- 환자의 이름은 Joe 이고, 본문에 Diet Coke 를 포함하는 문서를 검색한다면 속성명과 문자열을 조합한 단순 쿼리 언어를 만들어 이를 처리한다.
- patient:Joe,body:Diet Coke 

`Query`

```java
public class Query implements Predicate<Document> {
    private final Map<String, String> caluses;

    // 커스텀한 쿼리 형식 사용
    // 쿼리 형식 -> patient:Joe,body:Diet Coke
    public static Query parse(final String query) {
        return new Query(Arrays.stream(query.split(","))
            .map(str -> str.split(":"))
            .collect(Collectors.toMap(x -> x[0], x -> x[1]))
        );
    }

    private Query(final Map<String, String> caluses) {
        this.caluses = caluses;
    }

    // 쿼리 검색
    @Override
    public boolean test(final Document document) {
        return caluses.entrySet()
            .stream()
            .allMatch(entry -> {
                final String documentValue = document.getAttribute(entry.getKey());
                final String queryValue = entry.getValue();
                return documentValue != null && documentValue.contains(queryValue);
            });
    }
}
```

`DocumentManagementSystem`

```java
public class DocumentManagementSystem {

    // 문서내 검색을 수행한다.
    public List<Document> search(final String query) {
        return documents.stream()
            .filter(Query.parse(query))
            .collect(Collectors.toList());
    }
}
```

> DocumentManagementSystem 의 search 메소드로 전달된 쿼리 ex) patient:Joe,body:Diet Coke 는 Query 객체로 변환되어 검색을 수행한다.


## 리스코프 치환 원칙 (LSP)
- Importer 를 구현한 방법, Document 클래스에 계층을 추가하지 않은 이유와 HashMap 을 상속받지 않은 이유는 리스코프 치환 원칙을 따른다.
- **리스코프 치환 원칙** 이란 클래스 상속과 인터페이스 구현을 올바르게 할 수 있도록 도움을 주는 원칙이다.

### 1. 하위 타입에서 선행 조건을 더할 수 없다.
- 선행 조건이란 어떤 코드가 동작하는 조건을 결정한다.
- Importer 라는 구현은 임포트할 파일이 존재하고, 읽을 수 있을것이라는 **선행조건** 을 가진다.
- Importer 를 수행하기 전에 검증을 수행하는 importFile 메소드가 필요하다.

```java
public class DocumentManagementSystem {
    private final Map<String, Importer> importers = new HashMap<>();
    private final List<Document> documents = new ArrayList<>();

    // 파일 경로를 받아 해당 파일을 임포트한다.
    public void importFile(final String path) throws IOException {
        final File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException(path);
        }

        final int separatorIndex = path.lastIndexOf(".");
        if (separatorIndex != -1) {
            if (separatorIndex == path.length()) {
                throw new UnknownFileTypeException("No Extension found For file: " + path);
            }
            final String extension = path.substring(separatorIndex + 1);
            final Importer importer = importers.get(extension);
            if (importer == null) {
                throw new UnknownFileTypeException("For file: " + path);
            }

            final Document document = importer.importFile(file);
            documents.add(document);
        } else {
            throw new UnknownFileTypeException("No Extension found For file: " + path);
        }
    }

}
```
- LSP 는 **부모가 지정한 것 보다 더 많은 선행조건을 요구할 수 없다.**
- 부모가 문서의 크기를 제한하지 않았다면, 문서 크기가 100KB 보다 작아야 한다고 요구할 수 없다.

### 2. 하위 타입에서 후행 조건을 약화 시킬 수 없다.
- 후행 조건이란 코드를 실행한 뒤 만족해야 하는 규칙이다.
- importFile 을 실행했다면 contents 가 반환하는 문서 목록에 그 파일이 포함되어야 한다.
- **부모가 부작용을 포함하거나 어떤 값을 반환한다면 자식도 반드시 그래야 한다.**

### 3. 부모 타입의 불변자는 하위 타입에서도 보존되어야 한다.
- 상속 관계의 부모와 자식 클래스인 경우 부모 클래스에서 유지되는 모든 불변자는 자식 클래스에서도 유지되어야 한다.

### 4. 히스토리 규칙
- 자식 클래스는 부모가 허용하지 않은 상태 변화를 허용하지 않아야 한다.
- Document 가 불변 클래스라면, 자식 클래스 또한 불변이어야 한다.
- 즉 어떤 속성도 추가 / 삭제 / 변경 할 수 없어야 한다.

## 대안

### 임포터를 클래스로 만들기
- 임포터의 클래스 계층을 인터페이스 대신 최상위에 Importer 클래스를 만드는 방법
- 인터페이스와 클래스는 서로 다른 기능을 제공한다.
  - 인터페이스는 여러 개를 한 번에 구현 가능
  - 클래스는 인스턴스 필드와 메소드를 가짐
> 다중 상속을 목적으로 인터페이스를 사용해서는 안된다. \n
> 또한 상수 정의를 목적으로 인터페이스를 사용해서도 안된다. \n
> 인터페이스는 **타입 정의 목적** 으로만 사용해야 한다.

- 상속 기반의 클래스 보단 인터페이스를 이용하는 것이 좋은 선택
- 모든 상황에서 인터페이스가 좋은 것은 아니다. 강력한 **is a 관계** 를 모델링 해야 한다면 상속이 옳은 선택이다.

### 영역 / 캡슐화 선택
- 접근 제어자를 이용해 일종의 은폐 장치로 사용할 수 있다.
  - 특정 클래스는 특정 패키지 내에서만 생성 하게끔 제한 하는 등..
  - 이런 방식을 이용해 일종의 캡슐화 구현도 가능하다.
- 접근 제어자는 패키지가 기본 영역이지만 실제 개발시 패키지 영역 보다 public 영역을 더 많이 사용한다.
- 공개 영역을 Public 으로 지정하는 것이 더 좋은 선택 일 수 있다.

## 기존 코드 확장 및 재사용
- 소프트웨어는 항상 변한다.
- 제품에 기능 추가 / 요구사항 변경 등 소프트웨어는 항상 변할 수 있다.
- 청구서 문서를 관리하고 싶다는 요구사항이 추가되었다.
  - 청구서 문서는 본문 및 금액을 포함하고, .invoice 라는 확장자를 가진다.

`청구서 예제`

```text
Dear Joe Bloggs

Here is your invoice for the dental treatment that you received.

Amount: $100

regards,

  Dr Avaj
  Awesome Dentist
```

`우편물 예제`

```text
Dear Joe Bloggs

123 Fake Street
Westminster
London
United Kingdom

We are writing to you to confirm the re-scheduling of your appointment
with Dr. Avaj from 29th December 2016 to 5th January 2017.

regards,

  Dr Avaj
  Awesome Dentist
```

`리포트 예제`

```text
Patient: Joe Bloggs

On 5th January 2017 I examined Joe's teeth.
We discussed his switch from drinking Coke to Diet Coke.
No new problems were noted with his teeth.
```

- 각 예제들을 살펴보면 Dear, Amount 와 같은 접두어 뒤에 나오는 내용을 속성으로 추출 할 수 있다.
- 청구서 / 우편물 / 리포트 모두 접두어 추출을 활용할 수 있다.
- 이런 코드를 재사용 하려면 특정 클래스에 구현을 해야한다.

### 유틸리티 클래스
- 가장 강단한 방법이지만 이는 좋은 방법은 아니다.
- 결국 여러 정적 메소드들을 포함하게 된다.
- 유틸리티 클래스는 보통 어떤 의무나 개념과 상관없이 다양한 코드의 모음을 귀결된다.
- 시간이 흐를 수록 하나의 **갓 클래스** 가 될 가능성이 높다.

### 상속 사용
- 동작과 개념을 연결하는데 상속을 이용 하는 방법
- 각각의 임포터가 TextImporter 클래스를 상속받는 방법
- 모든 공통기능을 구현하고 서브클래스에서 이를 재사용 한다.
- TextImporter 는 Importer 이고 LSP 를 따르지만 실제 관계를 제대로 반영하지 않은 상속은 쉽게 깨질 수 있다.
- 시간이 흐르고 프로그램이 변경될 때 이를 바꾸는 것 보단 변화를 추상화 하는 것이 좋다.
> 일반적으로 상속을 코드 재사용 목적으로 사용하는 것은 좋은 방법이 아니다.

### 도메인 클래스
- 도메인 클래스로 텍스트 파일을 모델링 하는 방법
- 기본 개념을 모델링 한 뒤 기본 개념이 제공하는 메서드를 호출해 다양한 임포터를 만든다.

`TextFile`

```java
public class TextFile {
    private final Map<String, String> attributes;
    private final List<String> lines;

    public TextFile(final File file) throws IOException {
        this.attributes = new HashMap<>();
        attributes.put(Attributes.PATH, file.getPath());
        this.lines = Files.lines(file.toPath()).collect(Collectors.toList());
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public int addLines(
        final int start,
        final Predicate<String> isEnd,
        final String attributeName
    ) {
        final StringBuilder accumulator = new StringBuilder();
        int lineNumber;

        for (lineNumber = start; lineNumber < lines.size(); lineNumber++) {
            final String line = lines.get(lineNumber);
            if (isEnd.test(line)) {
                break;
            }

            accumulator.append(line);
            accumulator.append("\n");
        }
        attributes.put(attributeName, accumulator.toString().trim());
        return lineNumber;
    }

    public void addLineSuffix(final String prefix, final String attributeName) {
        for (final String line : lines) {
            if (line.startsWith(prefix)) {
                attributes.put(attributeName, line.substring(prefix.length()));
                break;
            }
        }
    }
}
```
- TextFile 은 Document 의 서브 타입이 아니고, 텍스트 파일에서 데이터를 추출하는 메소드를 가진다.
> 도메인 클래스를 이용하면 유연성을 개선할 수 있다. \n
> 도메인 클래스 활용시 상속 처럼 쉽게 깨질 수 있는 계층을 만들지 않으면서 코드 재사용이 가능하다.

`각 임포터 구현`

```java
public class InvoiceImporter implements Importer {
    private static final String NAME_PREFIX = "Dear ";
    private static final String AMOUNT_PREFIX = "Amount: ";

    @Override
    public Document importFile(final File file) throws IOException {
        final TextFile textFile = new TextFile(file);

        textFile.addLineSuffix(NAME_PREFIX, PATIENT);
        textFile.addLineSuffix(AMOUNT_PREFIX, AMOUNT);

        final Map<String, String> attributes = textFile.getAttributes();
        attributes.put(Attributes.TYPE, "INVOICE");

        return new Document(attributes);
    }
}

public class LetterImporter implements Importer {
  private static final String NAME_PREFIX = "Dear ";

  @Override
  public Document importFile(final File file) throws IOException {
    final TextFile textFile = new TextFile(file);

    textFile.addLineSuffix(NAME_PREFIX, PATIENT);

    final int lineNumber = textFile.addLines(2, String::isBlank, ADDRESS);
    textFile.addLines(lineNumber + 1, line -> line.startsWith("regards,"), BODY);

    final Map<String, String> attributes = textFile.getAttributes();
    attributes.put(TYPE, "LETTER");

    return new Document(attributes);
  }
}

public class ReportImporter implements Importer {
  private static final String NAME_PREFIX = "Patient: ";

  @Override
  public Document importFile(final File file) throws IOException {
    final TextFile textFile = new TextFile(file);
    textFile.addLineSuffix(NAME_PREFIX, PATIENT);
    textFile.addLines(2, line -> false, BODY);

    final Map<String, String> attributes = textFile.getAttributes();
    attributes.put(TYPE, "REPORT");

    return new Document(attributes);
  }
}
```

## 테스트 위생
- 자동화된 테스트를 구현하면 소프트웨어 유지보수에 큰 도움이 된다.
- 어떤 동작이 문제를 일으켰는지 이해할 수 있고, 자신있게 리팩터링이 가능하다.
- 또한 잘 작성된 테스트 코드는 **문서의 역할** 도 한다.
- 테스트 유지보수 문제를 해결하려면 **테스트 위생** 을 지켜야 한다.
  - 코드베이스 뿐이 아닌 테스트 코드도 깔끔하고 개선 해야 한다.

### 테스트 이름 짓기
- 테스트 이름은 가독성 / 유지보수성을 고려 해야한다.
- test1 과 같은 테스트 명은 최악의 안티패턴이고 file, document 와 같이 개념이나 명사로 테스트 명을 짓는 것이다.

`좋은 테스트 이름 짓는 3가지`
- 도메인 용어 사용
- 자연어 사용
- 서술적

### 구현이 아닌 동작
- 클래스 / 컴포넌트 / 시슽메 테스트 구현시에는 대상의 **공개 동작 (public behavior)** 만 테스트 해야한다.
- 객체의 내부 상태나 설계는 고려하지 않고 오직 공개 API 메소드만 이용해 테스트를 수행해야 한다.
- 세부 구현에 의존한 테스트는 구현이 변경되면 테스트도 깨지게 된다.

### 중복 배제
- 개발자들은 코드베이스에서는 중복 제거를 잘 지키지만, 테스트 코드에서는 크게 신경 쓰지 않는다.
- 테스트 코드에서도 반복되는 중복 코드를 제거해야 한다.

### 좋은 진단
- 테스트는 실패하지 않는다면 소용 없다.
- 실패에 최적화된 테스트를 구현해야 한다.
- 이는 테스트가 실패한 이유를 쉽게 이해 가능하도록 만들어야 한다는 의미이다.

### 오류 상황 테스트
- 테스트 개발시 가장 흔한 실수는 가장 바람직한 성공 케이스만 테스트 한다는 점이다.
- 문서관리 시스템에서 파일이 없거나, 읽지 못하는 파일을 읽으려 하는등 실패 케이스에 대한 테스트도 작성이 되어야 한다.

### 상수
- 상수는 변하지 않는 값이다.
- 테스트에 활용되는 용도에 따라 적절한 네이밍을 하는것이 좋다.

## 정리
- 개발자와 사용자 모두가 사용할 수 있도록 유비 쿼터스 언어를 활용 해야 한다.
- 강한 형식의 원칙을 적용하면 데이터 사용 방법을 규제할 수 있다.
- 인터페이스 설계시 불필요한 기능은 제거 해야 한다.
- 매직넘버 사용은 지양해야 한다.
- 상속 관계에 적용하는 리스코프 치환 원칙을 준수해야 한다.
- 리스코프 치환 원칙을 준수하더라도 실제 관계를 반영하지 않는 상속관계라면 깨지기 쉽고 불안정하다.
- 코드 재사용을 위한 상속은 사용해서는 안된다.
  - 도메인 클래스를 활용해 추상화 하는 방법도 존재한다.
- 상속 보다는 합성을 사용해야 한다.
  










