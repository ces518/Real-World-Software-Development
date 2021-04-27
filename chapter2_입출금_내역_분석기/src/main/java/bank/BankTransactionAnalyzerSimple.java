package bank;

import bank.parser.BankStatementCSVParser;
import bank.parser.BankStatementParser;
import bank.processor.BankStatementProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    // 특정 파서와의 결합 제거
    public void analyze(final String fileName, final BankStatementParser bankStatementParser) throws IOException {
        final Path path = Paths.get(RESOURCES + fileName);
        final List<String> lines = Files.readAllLines(path);
        final List<BankTransaction> bankTransactions = bankStatementParser.parseLinesFrom(lines);
        final BankStatementProcessor bankStatementProcessor = new BankStatementProcessor(bankTransactions);

        collectSummary(bankStatementProcessor);
    }
}
