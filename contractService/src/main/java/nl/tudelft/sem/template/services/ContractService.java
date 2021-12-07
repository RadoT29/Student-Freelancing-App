package nl.tudelft.sem.template.services;

import java.time.LocalDate;

import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.enums.ContractStatus;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.repositories.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractService {

    @Autowired
    private transient ContractRepository contractRepository;

    private static final transient double MAX_HOURS = 20;
    private static final transient double MAX_WEEKS = 26;

    /**
     * PRIVATE HELPER METHOD which validates a contract's parameters.
     *
     * @param contract The contract to be validated.
     * @throws IllegalArgumentException Thrown when the contract is not valid
     *                                  e.g. exceeds 20 hours per week, 6 month duration
     *                                  or the company's id and student's id are the same
     *                                  or if there is an existing active contract already
     */
    private void validateContract(Contract contract) throws IllegalArgumentException {
        // contract between the same user:
        if (contract.getCompanyId().equals(contract.getStudentId())

                //max no of hours exceeded:
                || contract.getHoursPerWeek() > MAX_HOURS

                //no of weeks exceeded:
                || contract.getTotalHours() / contract.getHoursPerWeek() > MAX_WEEKS) {

            throw new IllegalArgumentException("One or more contract parameters are invalid.");
        }
        if (contractRepository.findActiveContract(contract.getCompanyId(),
                contract.getStudentId()) != null) {
            throw new IllegalArgumentException(
                    "Please cancel the existing contract with this party.");
        }
    }

    /**
     * Saves a contract in the repository.
     *
     * @param contract The contract to be saved.
     * @return The saved contract.
     * @throws IllegalArgumentException If the contract has invalid parameters
     *                                  or if one already exists.
     */
    public Contract saveContract(Contract contract) throws IllegalArgumentException {
        validateContract(contract);

        // Set startDate:
        contract.setStartDate(LocalDate.now());

        // Set endDate:
        int weeks = (int) Math.ceil(contract.getTotalHours() / contract.getHoursPerWeek());
        LocalDate date = contract.getStartDate().plusWeeks(weeks);
        contract.setEndDate(date);    //LocalDate is immutable, so different memory address here

        // Set as active
        contract.setContractStatus(ContractStatus.ACTIVE);

        return contractRepository.save(contract);
    }

    /**
     * Gets the contract (active or not) between 2 parties from the repository.
     *
     * @param companyId The id of the company.
     * @param studentId The id of the student.
     * @param active    If the query needs an active contract or not.
     * @return the found contract or null if not found.
     * @throws ContractNotFoundException Thrown if a contract doesn't exist.
     */
    public Contract getContract(String companyId, String studentId, boolean active)
            throws ContractNotFoundException {
        Contract contract;
        if (active) {
            contract = contractRepository.findActiveContract(companyId, studentId);
        } else {
            contract = contractRepository
                    .findFirstByCompanyIdEqualsAndStudentIdEqualsOrderByEndDateDesc(
                            companyId, studentId
                    );
        }

        if (contract == null) {
            throw new ContractNotFoundException(companyId, studentId);
        } else {
            return contract;
        }
    }

    /**
     * Terminates a contract.
     *
     * @param contractId The contract that is to be terminated.
     * @throws ContractNotFoundException Thrown if a contract doesn't exist.
     */
    public void terminateContract(Long contractId) throws ContractNotFoundException {
        if (contractRepository.findById(contractId).isEmpty()) {
            throw new ContractNotFoundException(contractId);
        }
        contractRepository.terminateContract(contractId);
    }

}
