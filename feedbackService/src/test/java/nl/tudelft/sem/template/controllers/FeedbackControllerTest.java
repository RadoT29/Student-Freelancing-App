package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import nl.tudelft.sem.template.domain.Response;
import nl.tudelft.sem.template.domain.dtos.requests.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.responses.FeedbackResponse;
import nl.tudelft.sem.template.services.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@AutoConfigureMockMvc
public class FeedbackControllerTest {
    private transient final long id = 1;
    private transient final String userName = "username";
    private transient final String userRole = "STUDENT";
    @Autowired
    private transient FeedbackController feedbackController;
    @MockBean
    private transient FeedbackService feedbackService;
    private transient FeedbackResponse feedbackResponse;
    private transient FeedbackRequest feedbackRequest;

    @BeforeEach
    void setUp() {
        feedbackResponse = new FeedbackResponse("review", 0, "from", "to");
        feedbackRequest = new FeedbackRequest("review", 0, "from", "to");
    }

    @Test
    void getByIdTest() {
        when(feedbackService.getById(id)).thenReturn(feedbackResponse);

        ResponseEntity<Response<FeedbackResponse>> expected = feedbackController.getById(id);
        ResponseEntity<Response<FeedbackResponse>> actual = ResponseEntity
            .ok()
            .body(new Response<>(feedbackResponse, null));

        assertEquals(expected, actual);
    }

    @Test
    void createTest() {
        when(feedbackService.create(feedbackRequest, userName, userRole))
            .thenReturn(Pair.of(feedbackResponse, id));

        ResponseEntity<Response<FeedbackResponse>> expected = ResponseEntity
            .created(URI.create("http://feedback-service/" + id))
            .body(new Response<>(feedbackResponse, null));

        ResponseEntity<Response<FeedbackResponse>> actual = feedbackController
            .create(feedbackRequest, userName, userRole);

        assertEquals(expected, actual);
    }
}
