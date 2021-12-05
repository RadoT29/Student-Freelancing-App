package nl.tudelft.sem.template.services;

import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.repositories.ApplicationRepository;
import nl.tudelft.sem.template.repositories.NonTargetedCompanyOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NonTargetedCompanyOfferService extends OfferService {

    @Autowired
    private transient NonTargetedCompanyOfferRepository nonTargetedCompanyOfferRepository;

    @Autowired
    private transient ApplicationRepository applicationRepository;

    /** Method for applying to a NonTargetedCompanyOffer.
     *
     * @param application Application that we want to save.
     * @param offerId Offer the application targets.
     * @return Application if saved successfully.
     */
    public Application apply(Application application, Long offerId) {
        NonTargetedCompanyOffer nonTargetedCompanyOffer = nonTargetedCompanyOfferRepository
                .getOfferById(offerId);
        if (nonTargetedCompanyOffer == null) {
            throw new IllegalArgumentException("There is no offer associated with this id");
        }
        if (nonTargetedCompanyOffer.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("This offer is not active anymore");
        }
        if (applicationRepository
                .existsByStudentIdAndNonTargetedCompanyOffer(
                        application.getStudentId(),
                        nonTargetedCompanyOffer)) {
            throw new IllegalArgumentException("Student already applied to this offer");
        }
        application.setNonTargetedCompanyOffer(nonTargetedCompanyOffer);
        application.setStatus(Status.PENDING);
        return applicationRepository.save(application);
    }

    /**
     * Service, which accepts an application and declines all others.
     *
     * @param application - the application, which we want to accept.
     */
    public void accept(Application application) {
        NonTargetedCompanyOffer nonTargetedCompanyOffer = nonTargetedCompanyOfferRepository
                .getOfferById(application.getNonTargetedCompanyOffer().getId());
        if (nonTargetedCompanyOffer == null) {
            throw new IllegalArgumentException(
                    "There is no offer associated with this application!");
        }
        if (nonTargetedCompanyOffer.getStatus() != Status.PENDING
            || application.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("The offer or application is not active anymore!");
        }
        if (!nonTargetedCompanyOffer.getApplications().contains(application)) {
            throw new IllegalArgumentException("Application is not valid!");
        }

        for (Application app : nonTargetedCompanyOffer.getApplications()) {
            if (app.equals(application)) {
                app.setStatus(Status.ACCEPTED);
            } else {
                app.setStatus(Status.DECLINED);
            }
            applicationRepository.save(app);
        }
        nonTargetedCompanyOffer.setStatus(Status.DISABLED);
        nonTargetedCompanyOfferRepository.save(nonTargetedCompanyOffer);
    }
}
