package bank.export;

import bank.result.SummaryStatistics;

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