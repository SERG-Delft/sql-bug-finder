package db.tables;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Answers {

    @Id
    private Long answerId;
    private Long questionId;
    private Long userId;
    private String isAccepted;
    private Long score;
    private Date lastActivityDate;
    private Date lastEditDate;
    private Date creationDate;

    @Override
    public String toString() {
        return String.format(
            "answer_id: %s, question_id: %s, user_id: %s, is_accepted: %s, score: %s, last_activity_date: %s, last_edit_date: %s, creation_date: %s",
            answerId, questionId, userId, isAccepted, score, lastActivityDate, lastEditDate, creationDate);
    }
}
