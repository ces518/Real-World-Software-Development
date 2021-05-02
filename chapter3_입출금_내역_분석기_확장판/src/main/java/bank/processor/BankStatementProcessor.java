package bank.processor;

import bank.BankTransaction;
import bank.filter.BankTransactionFilter;
import bank.summarizer.BankTransactionSummarizer;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

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
//        double total = 0;
//        for (final BankTransaction bankTransaction : bankTransactions) {
//            if (bankTransaction.getDate().getMonth() == month) {
//                total += bankTransaction.getAmount();
//            }
//        }
//        return total;
    }

    public double calculateTotalForCategory(final String category) {
        return summarizeTransactions(
            (acc, bankTransaction) ->
                bankTransaction.getDescription().equals(category) ? acc + bankTransaction.getAmount() : acc
        );
//        double total = 0;
//        for (BankTransaction bankTransaction : bankTransactions) {
//            if (bankTransaction.getDescription().equals(category)) {
//                total += bankTransaction.getAmount();
//            }
//        }
//        return total;
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
//        final List<BankTransaction> result = new ArrayList<>();
//        for (final BankTransaction bankTransaction : bankTransactions) {
//            if (bankTransaction.getAmount() >= amount) {
//                result.add(bankTransaction);
//            }
//        }
//        return result;
    }

    // 특정 월의 입출금 내역 찾기
    public List<BankTransaction> findTransactionsInMonth(final Month month) {
        return findTransactions(bankTransaction -> bankTransaction.getDate().getMonth() == month);
//        final List<BankTransaction> result = new ArrayList<>();
//        for (final BankTransaction bankTransaction : bankTransactions) {
//            if (bankTransaction.getDate().getMonth() == month) {
//                result.add(bankTransaction);
//            }
//        }
//        return result;
    }

    // 특정 월이나 금액으로 입출금 내역 찾기
    public List<BankTransaction> findTransactionsInMonthAndGreater(final Month month, final int amount) {
        return findTransactions(
            bankTransaction ->
                bankTransaction.getDate().getMonth() == month && bankTransaction.getAmount() >= amount
        );
//        final List<BankTransaction> result = new ArrayList<>();
//        for (final BankTransaction bankTransaction : bankTransactions) {
//            if (bankTransaction.getDate().getMonth() == month && bankTransaction.getAmount() >= amount) {
//                result.add(bankTransaction);
//            }
//
//        }
//        return result;
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
