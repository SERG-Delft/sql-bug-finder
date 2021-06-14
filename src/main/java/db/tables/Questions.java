package db.tables;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Questions {

    @Id
    private Long questionId;
    private Long userId;
    private String isAnswered;
    private Long viewCount;
    private Date protectedDate;
    private Long acceptedAnswerId;
    private Long answerCount;
    private Long score;
    private Date lastActivityDate;
    private Date lastEditDate;
    private Date creationDate;
    private String link;
    private String title;

    @Override
    public String toString() {
        return String.format(
            "question_id: %s, user_id: %s, is_answered: %s, view_count: %s, protected_date: %s, accepted_answer_id: %s, answer_count: %s, score: %s, last_activity_date: %s, last_edit_date: %s, creation_date: %s, link: %s, title: %s",
            questionId, userId, isAnswered, viewCount, protectedDate, acceptedAnswerId, answerCount, score, lastActivityDate, lastEditDate, creationDate, link, title
        );
    }
}
