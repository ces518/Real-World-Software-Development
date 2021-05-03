package bank.export;

import bank.result.SummaryStatistics;

public interface Exporter {
    String export(SummaryStatistics summaryStatistics);
}
