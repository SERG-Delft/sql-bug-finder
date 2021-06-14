package db.tables;

import org.springframework.data.annotation.Id;

public class Pages {

    @Id
    private Long pageId;
    private String isParsed;

    @Override
    public String toString() {
        return String.format("page_id: %s, is_parsed: %s", pageId, isParsed);
    }
}
