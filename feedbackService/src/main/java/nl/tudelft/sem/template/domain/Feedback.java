package nl.tudelft.sem.template.domain;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.sem.template.domain.converters.RatingConverter;
import nl.tudelft.sem.template.domain.dtos.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.FeedbackResponse;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String review;
    @Convert(converter = RatingConverter.class)
    private Rating rating;
    private String author;
    private String recipient;

    public static Feedback from(FeedbackRequest feedbackRequest) {
        return new Feedback(null,
            feedbackRequest.getReview(),
            new Rating(feedbackRequest.getRating()),
            feedbackRequest.getFrom(),
            feedbackRequest.getTo()
        );
    }

    public FeedbackResponse to() {
        return new FeedbackResponse(this.review, this.rating.getStars(), this.author, this.recipient);
    }
}