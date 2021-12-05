package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nl.tudelft.sem.template.domain.Feedback;
import nl.tudelft.sem.template.domain.Rating;
import nl.tudelft.sem.template.domain.dtos.requests.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.responses.ContractResponse;
import nl.tudelft.sem.template.domain.dtos.responses.FeedbackResponse;
import nl.tudelft.sem.template.domain.dtos.responses.UserRoleResponse;
import nl.tudelft.sem.template.domain.dtos.responses.UserRoleResponseWrapper;
import nl.tudelft.sem.template.exceptions.ContractNotExpiredException;
import nl.tudelft.sem.template.exceptions.FeedbackNotFoundException;
import nl.tudelft.sem.template.exceptions.InvalidFeedbackDetailsException;
import nl.tudelft.sem.template.exceptions.InvalidRoleException;
import nl.tudelft.sem.template.exceptions.InvalidUserException;
import nl.tudelft.sem.template.exceptions.NoExistingContractException;
import nl.tudelft.sem.template.exceptions.UserServiceUnavailableException;
import nl.tudelft.sem.template.repositories.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public class FeedbackServiceTest {
    @Autowired
    private transient FeedbackService feedbackService;

    @MockBean
    private transient FeedbackRepository feedbackRepository;
    @MockBean
    private transient RestTemplate restTemplate;

    private transient FeedbackResponse feedbackResponse;
    private transient FeedbackRequest feedbackRequest;
    private transient Feedback feedback;
    private transient Rating rating;
    private transient UserRoleResponse userRoleResponse;
    private transient UserRoleResponseWrapper userRoleResponseWrapper;
    private final transient ContractResponse contractResponse =
        new ContractResponse("EXPIRED");

    private transient final long id = 1;
    private transient final String userName = "username";
    private transient final String userRole = "STUDENT";

    @BeforeEach
    void setUp() {
        feedbackResponse = new FeedbackResponse("review", 0, userName, "to");
        feedbackRequest = new FeedbackRequest("review", 0, userName, "to");
        rating = new Rating(0);
        feedback = new Feedback(id, "review", rating, userName, "to");
        userRoleResponse = new UserRoleResponse();
        userRoleResponse.setRole("COMPANY");
        userRoleResponseWrapper = new UserRoleResponseWrapper();
        userRoleResponseWrapper.setData(userRoleResponse);
    }

    @Test
    void getByIdTestFound() {
        when(feedbackRepository.findById(id)).thenReturn(Optional.of(feedback));

        var expected = feedbackResponse;
        var actual = feedbackService.getById(id);

        assertEquals(expected, actual);
    }

    @Test
    void getByIdTestNotFound() {
        when(feedbackRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(FeedbackNotFoundException.class, () -> feedbackService.getById(id));
    }

    @Test
    void createTest() {
        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        when(restTemplate.getForObject(anyString(), eq(ContractResponse.class)))
            .thenReturn(contractResponse);

        when(feedbackRepository.save(any(Feedback.class)))
            .thenReturn(feedback);

        var expected = Pair.of(feedbackResponse, id);
        var actual = feedbackService
            .create(feedbackRequest, userName, userRole);

        assertEquals(expected.getFirst(), actual.first());
    }

    @Test
    void createTest2() {
        userRoleResponseWrapper.setData(new UserRoleResponse("STUDENT"));
        var newUserRole = "COMPANY";

        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        when(restTemplate.getForObject(anyString(), eq(ContractResponse.class)))
            .thenReturn(contractResponse);

        when(feedbackRepository.save(any(Feedback.class)))
            .thenReturn(feedback);

        var expected = Pair.of(feedbackResponse, id);
        var actual = feedbackService
            .create(feedbackRequest, userName, newUserRole);

        assertEquals(expected.getFirst(), actual.first());
    }

    @Test
    void createWithActiveContractTest() {
        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        contractResponse.setStatus("ACTIVE");

        when(restTemplate.getForObject(anyString(), eq(ContractResponse.class)))
            .thenReturn(contractResponse);

        assertThrows(ContractNotExpiredException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    void createWithAuthorSameAsRecipient() {
        feedbackRequest.setTo(feedbackRequest.getFrom());

        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        when(restTemplate.getForObject(anyString(), eq(ContractResponse.class)))
            .thenReturn(contractResponse);

        assertThrows(InvalidFeedbackDetailsException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    void createUserServiceUnavailable() {
        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(null);

        assertThrows(UserServiceUnavailableException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    void createAuthorHasSameRoleAsRecipient() {
        userRoleResponseWrapper.setData(new UserRoleResponse("STUDENT"));
        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        assertThrows(InvalidUserException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    void createRecipientDoesNotExist() {
        userRoleResponseWrapper.setData(null);
        userRoleResponseWrapper.setErrorMessage("An error occurred");

        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        assertThrows(InvalidUserException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    void createWithoutContract() {
        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        when(restTemplate.getForObject(anyString(), eq(ContractResponse.class)))
            .thenThrow(HttpClientErrorException.class);

        assertThrows(NoExistingContractException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    void createWithoutUserService() {
        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        when(restTemplate.getForObject(anyString(), eq(ContractResponse.class)))
            .thenThrow(RestClientException.class);

        assertThrows(UserServiceUnavailableException.class,
            () -> feedbackService.create(feedbackRequest, userName, userRole));
    }

    @Test
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    void createWithInvalidRole() {
        final var newUserRole = "Gamer";

        when(restTemplate.getForObject(anyString(), eq(UserRoleResponseWrapper.class)))
            .thenReturn(userRoleResponseWrapper);

        assertThrows(InvalidRoleException.class,
            () -> feedbackService.create(feedbackRequest, userName, newUserRole));
    }
}
