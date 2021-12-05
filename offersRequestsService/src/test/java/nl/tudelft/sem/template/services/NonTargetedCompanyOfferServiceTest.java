package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.repositories.ApplicationRepository;
import nl.tudelft.sem.template.repositories.NonTargetedCompanyOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
class NonTargetedCompanyOfferServiceTest {

    @Autowired
    private transient NonTargetedCompanyOfferService service;

    @MockBean
    private transient NonTargetedCompanyOfferRepository offerRepository;

    @MockBean
    private transient ApplicationRepository applicationRepository;

    private transient NonTargetedCompanyOffer offer;
    private transient Application application;
    private transient String student;

    @BeforeEach
    void setup() {
        List<String> expertise = List.of("e1", "e2", "e3");
        List<String> requirements = List.of("r1", "r2", "r3");
        student = "student";
        offer = new NonTargetedCompanyOffer("title", "description",
                20, 520, expertise,
                Status.PENDING, requirements, "facebook");
        application = new Application(student, 5, Status.PENDING, offer);
    }

    @Test
    void applyNonExistantTest() {
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(null);
        String errorMessage = "There is no offer associated with this id";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.apply(application, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void applyNotActiveTest() {
        offer.setStatus(Status.DISABLED);
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        String errorMessage = "This offer is not active anymore";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.apply(application, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void applyAlreadyAppliedTest() {
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        Mockito.when(applicationRepository
                        .existsByStudentIdAndNonTargetedCompanyOffer(student, offer))
                .thenReturn(true);
        String errorMessage = "Student already applied to this offer";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.apply(application, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void applyValidTest() {
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        Mockito.when(applicationRepository
                        .existsByStudentIdAndNonTargetedCompanyOffer(student, offer))
                .thenReturn(false);
        Mockito.when(applicationRepository.save(application))
                .thenReturn(application);

        assertEquals(application, service.apply(application, 3L));
    }

    @Test
    void applySetterTest() {
        Application app = Mockito.mock(Application.class);
        app.setStudentId(student);
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        Mockito.when(applicationRepository
                        .existsByStudentIdAndNonTargetedCompanyOffer(student, offer))
                .thenReturn(false);
        Mockito.when(applicationRepository.save(application))
                .thenReturn(application);
        service.apply(app, 3L);
        Mockito.verify(app).setNonTargetedCompanyOffer(offer);
    }

    @Test
    void acceptTest() {
        Application declined = new Application(student, 10, Status.PENDING, offer);
        offer.setApplications(List.of(application, declined));
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);

        service.accept(application);
        Mockito.verify(offerRepository, times(1)).save(any());
        Mockito.verify(applicationRepository, times(2)).save(any());
        assertSame(application.getStatus(), Status.ACCEPTED);
        assertSame(offer.getStatus(), Status.DISABLED);
        assertSame(declined.getStatus(), Status.DECLINED);
    }

    @Test
    void acceptTestFailNull() {
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(null);

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(application));
        String errorMessage = "There is no offer associated with this application!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptTestFailEmpty() {
        offer.setApplications(new ArrayList<>());
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(application));
        String errorMessage = "Application is not valid!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptTestFailStatusApplication() {
        application.setStatus(Status.DECLINED);
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(application));
        String errorMessage = "The offer or application is not active anymore!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptTestFailStatusOffer() {
        application.setStatus(Status.DISABLED);
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(application));
        String errorMessage = "The offer or application is not active anymore!";
        assertEquals(errorMessage, exception.getMessage());
    }

}