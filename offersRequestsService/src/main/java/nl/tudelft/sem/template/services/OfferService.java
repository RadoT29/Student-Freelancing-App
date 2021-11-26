package nl.tudelft.sem.template.services;

import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.repositories.NonTargetedCompanyOfferRepository;
import nl.tudelft.sem.template.repositories.OfferRepository;
import nl.tudelft.sem.template.repositories.StudentOfferRepository;
import nl.tudelft.sem.template.repositories.TargetedCompanyOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfferService {

    private static final transient double MAX_HOURS = 20;
    private static final transient double  MAX_WEEKS = 26;

    @Autowired
    private transient OfferRepository offerRepository;

    @Autowired
    private StudentOfferRepository studentOfferRepository;

    @Autowired
    private TargetedCompanyOfferRepository targetedCompanyOfferRepository;

    @Autowired
    private NonTargetedCompanyOfferRepository nonTargetedCompanyOfferRepository;


    /**
     * Method for saving offers.
     *
     * @param offer Offer that needs to be saved.
     * @return Saved Offer.
     * @throws IllegalArgumentException Thrown when the offer is not valid
     *                                  e.g. exceeds 20 hours per week or 6 month duration
     */
    public Offer saveOffer(Offer offer) throws IllegalArgumentException {
        if (offer.getHoursPerWeek() > MAX_HOURS) {
            throw new IllegalArgumentException("Offer exceeds 20 hours per week");
        }
        if (offer.getTotalHours() / offer.getHoursPerWeek() > MAX_WEEKS) {
            throw new IllegalArgumentException("Offer exceeds 6 month duration");
        }
        offer.setStatus(Status.PENDING);
        return offerRepository.save(offer);
    }
}

