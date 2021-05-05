package dms;

import dms.constants.Attributes;
import dms.document.Document;
import dms.errors.UnknownFileTypeException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static dms.constants.Attributes.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentManagementSystemTest {
    /**
     * 테스트에 활용되는 다양한 값들은 상수로 정의하는 것이 좋음
     */
    private static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    private static final String LETTER = RESOURCES + "patient.letter";
    private static final String REPORT = RESOURCES + "patient.report";
    private static final String XRAY = RESOURCES + "xray.jpg";
    private static final String INVOICE = RESOURCES + "patient.invoice";
    private static final String JOE_BLOGGS = "Joe Bloggs";

    private DocumentManagementSystem system = new DocumentManagementSystem();

    /**
     * 테스트명 지을시 가독성 / 유지보수성 / 실행가능한 문서의 역할을 고려 해야한다.
     * 메소드 명만으로 어떤 기능이 동작하고 어떤 기능이 동작하지 않았는지 알 수 있어야 한다.
     *
     * anti-pattern
     * - test1, test2 와 같은 네이밍
     * - file, document 와 같은 개념 혹은 명사로 테스트 명을 결정하는 것
     *
     * best-practice
     * - 도메인 용어 사용
     * - 자연어 사용
     * - 서술적
     *
     * 테스트 작성시 public API 만 테스트 해야한다.
     * - 객체 내부 상태 / 설계를 고려하지 않고 public API 만 이용해 테스트를 해야 한다.
     * - 세부 구현에 의존한 테스트는 구현이 변경되면 테스트 결과가 실패로 변경된다.
     */
    @Test
    void shouldImportFile() throws Exception {
        system.importFile(LETTER);

        final Document document = onlyDocument();

        assertAttributeEquals(document, PATH, LETTER);
    }

    /**
     * 우편물을 임포트하는 테스트
     * Importer 의 구현체인 LetterImporter 를 테스트하는 방법도 있다.
     * 이처럼 테스트를 구현 클래스에 맞게 작성하면 테스트가 깨지지 않고 내부 리팩토링이 가능하다.
     *
     * Getter/Setter 를 이용해 테스트를 쉽게 만들었지만 private state 를 외부에 노출하는 안티 패턴도 존재한다.
     */
    @Test
    void shouldImportLetterAttributes() throws Exception {
        system.importFile(LETTER);

        final Document document = onlyDocument();

        assertAttributeEquals(document, PATIENT, JOE_BLOGGS);
        assertAttributeEquals(document, ADDRESS,
            "123 Fake Street\n" +
                "Westminster\n" +
                "London\n" +
                "United Kingdom");
        assertAttributeEquals(document, BODY,
            "We are writing to you to confirm the re-scheduling of your appointment\n" +
                "with Dr. Avaj from 29th December 2016 to 5th January 2017.");
        assertTypeIs("LETTER", document);
    }

    @Test
    void shouldImportReportAttributes() throws Exception {
        system.importFile(REPORT);

        assertIsReport(onlyDocument());
    }

    @Test
    void shouldImportImageAttributes() throws Exception {
        system.importFile(XRAY);

        final Document document = onlyDocument();

        assertAttributeEquals(document, WIDTH, "320");
        assertAttributeEquals(document, HEIGHT, "179");
        assertTypeIs("IMAGE", document);
    }

    @Test
    void shouldImportInvoiceAttributes() throws Exception {
        system.importFile(INVOICE);

        final Document document = onlyDocument();

        assertAttributeEquals(document, PATIENT, JOE_BLOGGS);
        assertAttributeEquals(document, AMOUNT, "$100");
        assertTypeIs("INVOICE", document);
    }

    @Test
    void shouldBeAbleToSearchFilesByAttributes() throws Exception {
        system.importFile(LETTER);
        system.importFile(REPORT);
        system.importFile(XRAY);

        final List<Document> documents = system.search("patient:Joe,body:Diet Coke");
        assertThat(documents.size()).isEqualTo(1);

        assertIsReport(documents.get(0));
    }

    /**
     * 오류 상황 테스트
     *
     * 테스트 작성시 자주하는 실수 중 하나
     * - 잘 동작되는 지 여부만 검증하는것
     * - 실패하는 상황에 대해 테스트가 필요하다.
     */
    @Test
    void shouldNotImportMissingFile() throws Exception {
        assertThrows(FileNotFoundException.class, () -> system.importFile("gobbledygook.txt"));
    }

    @Test
    void shouldNotImportUnknownFile() throws Exception {
        assertThrows(UnknownFileTypeException.class, () -> system.importFile(RESOURCES + "unknown.txt"));
    }

    /**
     * 중복 코드 제거
     *
     * 대다수의 개발자들이 서비스 코드에는 중복코드를 사용하지 않으려하지만
     * 테스트 코드에서는 중복코드를 신경쓰지 않는다.
     */
    private void assertAttributeEquals(
        final Document document,
        final String attributeName,
        final String expectedValue
    ) {
        assertThat(document.getAttribute(attributeName))
            .withFailMessage("Document has the wrong value for " + attributeName)
            .isEqualTo(expectedValue);
    }

    private void assertTypeIs(final String type, final Document document) {
        assertAttributeEquals(document, Attributes.TYPE, type);
    }

    private void assertIsReport(final Document document) {
        assertAttributeEquals(document, PATIENT, JOE_BLOGGS);
        assertAttributeEquals(document, BODY,
            "On 5th January 2017 I examined Joe's teeth.\n" +
                "We discussed his switch from drinking Coke to Diet Coke.\n" +
                "No new problems were noted with his teeth.");
        assertTypeIs("REPORT", document);
    }


    /**
     * 좋은 진단
     *
     * 테스트는 실패하지 않으면 의미가 없다.
     * 실패에 최적화된 테스트를 구현해야 한다.
     *
     * 이는 실행속도가 아닌 테스트가 실패한 이유를 더 쉽게 이해할 수 있도록 만들어야 한다는 의미이다.
     * - 테스트 실패시 출력하는 메시지와 정보를 의미
     */
    private Document onlyDocument() {
        final List<Document> documents = system.contents();
        assertThat(documents.size())
            .withFailMessage("Documents should be contains only 1 elements")
            .isEqualTo(1);
        return documents.get(0);
    }
}
