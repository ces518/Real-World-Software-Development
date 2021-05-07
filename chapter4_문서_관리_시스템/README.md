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

