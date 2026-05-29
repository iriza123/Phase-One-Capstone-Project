package lab3.service;

import lab1.model.Loan;
import lab2.dao.LoanDAO;
import lab2.dao.impl.LoanDAOImpl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class LoanService {

    private final LoanDAO dao = new LoanDAOImpl();
    private final AccountService accSvc = new AccountService();

    public Loan requestLoan(int customerId, int accountId, BigDecimal amount, String reason) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new Exception("Loan amount must be greater than zero.");
        if (reason == null || reason.isBlank())
            throw new Exception("Please provide a reason for your loan request.");
        accSvc.getById(accountId); // validate account exists
        Loan loan = new Loan();
        loan.setCustomerId(customerId);
        loan.setAccountId(accountId);
        loan.setAmount(amount);
        loan.setReason(reason);
        return dao.save(loan);
    }

    public void approveLoan(int loanId) throws Exception {
        List<Loan> pending = dao.findByStatus("PENDING");
        Loan loan = pending.stream()
            .filter(l -> l.getLoanId() == loanId)
            .findFirst()
            .orElseThrow(() -> new Exception("Loan not found or not pending."));
        accSvc.getById(loan.getAccountId()); // ensure account still exists
        // credit approved amount to the account
        lab1.model.Account acc = accSvc.getById(loan.getAccountId());
        accSvc.updateBalance(acc.getAccountId(), acc.getBalance().add(loan.getAmount()));
        dao.updateStatus(loanId, "APPROVED", "Loan approved and credited to account.");
    }

    public void rejectLoan(int loanId, String notes) throws Exception {
        dao.updateStatus(loanId, "REJECTED", notes == null || notes.isBlank() ? "Loan rejected." : notes);
    }

    public List<Loan> getByCustomer(int customerId) throws SQLException {
        return dao.findByCustomerId(customerId);
    }

    public List<Loan> getPending() throws SQLException {
        return dao.findByStatus("PENDING");
    }

    public List<Loan> getAll() throws SQLException {
        return dao.findAll();
    }

    public int countPending() throws SQLException {
        return dao.countPending();
    }
}
