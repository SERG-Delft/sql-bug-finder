package db.tables;

import org.springframework.data.annotation.Id;

public class Owners {

    @Id
    private Long userId;
    private String userType;
    private Long reputation;
    private Long acceptRate;
    private String displayName;
    private String link;

    @Override
    public String toString() {
        return String.format("user_id: %s, user_type: %s, reputation: %s, accept_rate: %s, display_name: %s, link: %s", userId, userType, reputation, acceptRate, displayName, link);
    }
}
