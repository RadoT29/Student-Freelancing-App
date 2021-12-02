package nl.tudelft.sem.template.entities;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String studentId;
    private double pricePerHour;

    @JsonProperty("nonTargetedCompanyOfferId")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "non_targeted_company_offer_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NonTargetedCompanyOffer nonTargetedCompanyOffer;

    /** Constructor for the Application class.
     *
     * @param studentId String containing the name of the student applying.
     * @param pricePerHour Double with the price the student wants.
     * @param nonTargetedCompanyOffer Offer the application is for.
     */
    public Application(String studentId, double pricePerHour,
                       NonTargetedCompanyOffer nonTargetedCompanyOffer) {
        this.studentId = studentId;
        this.pricePerHour = pricePerHour;
        this.nonTargetedCompanyOffer = nonTargetedCompanyOffer;
    }
}
