package nl.tudelft.sem.template.controllers;

import java.util.List;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.responses.Response;
import nl.tudelft.sem.template.services.StudentOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentOfferController {

    @Autowired
    private transient StudentOfferService studentOfferService;

    /** Endpoint for creating StudentOffers.
     *
     * @param userName Name of person making the request.
     * @param userRole Role of the person making the request.
     * @param studentOffer StudentOffer than needs to bed created.
     * @return ResponseEntity that can take various codes.
     *          401 UNAUTHORIZED if user not authenticated.
     *          403 FORBIDDEN if user not a student or not author of offer.
     *          400 BAD REQUEST if conditions for offer are not met.
     *          201 CREATED with Offer in body if successfull.
     */
    @PostMapping("/student/create")
    public ResponseEntity<Response<Offer>>
        saveStudentOffer(@RequestHeader("x-user-name") String userName,
                         @RequestHeader("x-user-role") String userRole,
                         @RequestBody StudentOffer studentOffer) {
        if (userName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, "User is not authenticated"));
        }
        if (!studentOffer.getStudentId().equals(userName) || !userRole.equals("STUDENT")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User not allowed to post this StudentOffer"));
        }
        Response<Offer> response;
        try {
            response =
                    new Response<>(studentOfferService.saveOffer(studentOffer),
                            null);

            return new ResponseEntity<>(response,
                    HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            response =
                    new Response<>(null, e.getMessage());

            return new ResponseEntity<>(response,
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint for getting all StudentOffers.
     *
     * @return 200 OK ResponseEntity with a Response with a list of StudentOffers
     *     if the requester has permission.
     *     It is to be decided how the authentication will handle the other case.
     */
    @GetMapping("/student/getAllOffers")
    public ResponseEntity<Response<List<StudentOffer>>>
        getAllStudentOffers() {
        // We have to check if the requester has the rights to view the student offers.

        Response<List<StudentOffer>> responseStudentOffers =
            new Response<>(studentOfferService.getOffers(), null);
        return ResponseEntity.ok(responseStudentOffers);
    }

    /**
     * Endpoint for getting all offers of the same student.
     *
     * @param studentId the id of the student.
     * @return 200 OK ResponseEntity with a Response
     *     which contains list of StudentOffers if valid
     *     and 400 BAD Request with a Response containing error message otherwise.
     */
    @GetMapping("/student/getOffers/{studentId}")
    public ResponseEntity<Response<List<StudentOffer>>>
        getStudentOffersById(@PathVariable String studentId) {
        // Check if student with that id exists.
        // Also check if the one who seeks the info can get it.

        Response<List<StudentOffer>> responseOffersById;
        try {
            responseOffersById =
                    new Response<>(studentOfferService
                            .getOffersById(studentId),
                            null);

            return ResponseEntity.ok(responseOffersById);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();

            responseOffersById =
                    new Response<>(null, exception.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(responseOffersById);
        }
    }
}
