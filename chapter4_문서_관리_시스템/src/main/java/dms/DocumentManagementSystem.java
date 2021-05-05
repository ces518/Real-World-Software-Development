package dms;

import dms.document.*;
import dms.errors.UnknownFileTypeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentManagementSystem {
    private final Map<String, Importer> importers = new HashMap<>();
    private final List<Document> documents = new ArrayList<>();

    public DocumentManagementSystem() {
        importers.put("letter", new LetterImporter());
        importers.put("report", new ReportImporter());
        importers.put("jpg", new ImageImporter());
    }

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

    // 문서관리 시스템에 저장된 모든 문서를 반환한다.
    public List<Document> contents() {
        return documents;
    }
}
