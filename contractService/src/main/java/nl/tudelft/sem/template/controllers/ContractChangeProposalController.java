package nl.tudelft.sem.template.controllers;

import java.util.List;
import nl.tudelft.sem.template.dtos.requests.ContractChangeRequest;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ChangeProposalNotFoundException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidChangeProposalException;
import nl.tudelft.sem.template.services.ContractChangeProposalService;
import nl.tudelft.sem.template.services.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractChangeProposalController {

    @Autowired
    private transient ContractChangeProposalService changeProposalService;

    @Autowired
    private transient ContractService contractService;

    private final transient String nameHeader = "x-user-name";

    /**
     * Submit a contract change proposal.
     *
     * @param userName      The id of the user making the request.
     * @param contractId    The id of the contract the user wants to change.
     * @param changeRequest The request containing the new contract parameters.
     * @return 201 CREATED with the saved proposal if the proposal is valid;
     *         400 BAD REQUEST if the contract is inactive,
     *         or if the change proposal parameters are invalid;
     *         404 NOT FOUND if the contract is not found,
     *         401 UNAUTHORIZED if the user is not in the contract;
     */
    @PostMapping("/{contractId}/changeProposals")
    public ResponseEntity<Object> proposeChange(
            @RequestHeader(nameHeader) String userName,
            @PathVariable Long contractId,
            @RequestBody ContractChangeRequest changeRequest) {

        try {
            Contract contract = contractService.getContract(contractId);
            ContractChangeProposal proposal =
                    changeRequest.toContractChangeProposal(contract, userName);
            ContractChangeProposal p = changeProposalService.submitProposal(proposal);

            return new ResponseEntity<>(p, HttpStatus.CREATED);

        } catch (ContractNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch( InvalidChangeProposalException | InactiveContractException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Accept a contract change proposal.
     *
     * @param userName   The id of the user making the request.
     * @param proposalId The id of the proposal that will be accepted.
     * @return 200 OK with the updated contract if everything is valid;
     *         400 BAD REQUEST if the proposal is invalid
     *         or if the contract is inactive;
     *         404 NOT FOUND if the proposal is not found,
     *         401 UNAUTHORIZED if the user is not the one that should review the proposal.
     */
    @PutMapping("/changeProposals/{proposalId}/accept")
    public ResponseEntity<Object> acceptProposal(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "proposalId") Long proposalId) {

        try {
            Contract contract = changeProposalService.acceptProposal(proposalId, userName);
            return new ResponseEntity<>(contract, HttpStatus.OK);
        } catch (ChangeProposalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InactiveContractException | InvalidChangeProposalException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Reject a contract change proposal.
     *
     * @param userName   The id of the user making the request.
     * @param proposalId The id of the proposal that will be rejected.
     * @return 200 OK if successful,
     *         400 BAD REQUEST if the contract is inactive,
     *         404 NOT FOUND if the proposal is not found,
     *         401 UNAUTHORIZED if the user is not the one that should review the proposal.
     */
    @PutMapping("/changeProposals/{proposalId}/reject")
    public ResponseEntity<String> rejectProposal(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "proposalId") Long proposalId) {

        try {
            changeProposalService.rejectProposal(proposalId, userName);
            return ResponseEntity.ok(null);
        } catch (ChangeProposalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InactiveContractException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Delete a contract change proposal.
     *
     * @param userName   The id of the user making the request.
     * @param proposalId The id of the proposal that will be deleted.
     * @return 200 OK if successful,
     *         404 NOT FOUND if the proposal is not found,
     *         401 UNAUTHORIZED if the user is not the one that submitted the proposal.
     */
    @DeleteMapping("/changeProposals/{proposalId}")
    public ResponseEntity<String> deleteProposal(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "proposalId") Long proposalId) {

        try {
            changeProposalService.deleteProposal(proposalId, userName);
            return ResponseEntity.ok(null);
        } catch (ChangeProposalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Get all proposed changes on a contract.
     *
     * @param userName   The id of the user making the request.
     * @param contractId The id of the contract.
     * @return 200 OK if successful,
     *         400 BAD REQUEST if the contract is inactive,
     *         404 NOT FOUND if the contract was not found,
     *         401 UNAUTHORIZED if the user is not in the contract.
     */
    @GetMapping("/{contractId}/changeProposals")
    public ResponseEntity<Object> getProposalsOfContract(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "contractId") Long contractId) {

        try {
            Contract contract = contractService.getContract(contractId);
            List<ContractChangeProposal> proposals =
                    changeProposalService.getProposals(contract, userName);
            return ResponseEntity.ok().body(proposals);
        } catch (ContractNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InactiveContractException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

}
