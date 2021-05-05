package dms.query;

import dms.document.Document;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Query implements Predicate<Document> {
    private final Map<String, String> caluses;

    // 커스텀한 쿼리 형식 사용
    // 쿼리 형식 -> patient:Joe,body:Diet Coke
    static Query parse(final String query) {
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
