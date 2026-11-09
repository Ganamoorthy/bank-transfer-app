
package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.*;
import com.example.bankapp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Populate all attributes required by bank.html
    private void populateDashboard(Model model, String keyword) {

        model.addAttribute("customers", bankService.getCustomers(keyword));

        model.addAttribute("allCustomers", bankService.getCustomers(null));

        model.addAttribute("totalCustomers",
                bankService.getCustomers(null).size());

        model.addAttribute("totalBalance",
                bankService.getTotalBalance());

        model.addAttribute("successCount",
                bankService.countStatus(TransactionStatus.SUCCESS));

        model.addAttribute("failedCount",
                bankService.countStatus(TransactionStatus.FAILED));

        model.addAttribute("transactions",
                bankService.getLatestTransactions());

        model.addAttribute("transferRequest",
                new TransferRequest());

        model.addAttribute("customer",
                new Customer());

        model.addAttribute("keyword", keyword);
    }

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        populateDashboard(model, keyword);
        return "bank";
    }

    @PostMapping("/customers")
    public String addCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes ra) {

        try {
            bankService.createCustomer(customer);
            ra.addFlashAttribute("success", "Customer added successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute TransferRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {

            populateDashboard(model, null);

            // Preserve the submitted transfer information
            model.addAttribute("transferRequest", request);

            return "bank";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure());

            ra.addFlashAttribute(
                    "success",
                    "Transfer completed successfully");

        } catch (Exception e) {

            auditService.recordFailure(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    e.getMessage());

            String errorMessage = e.getMessage();

            if (request.isSimulateFailure()) {
                errorMessage = "All changes were rolled back. " + errorMessage;
            }

            ra.addFlashAttribute("error", errorMessage);
        }

        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String history(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);

        model.addAttribute(
                "transactions",
                bankService.getHistory(accountNumber));

        return "history";
    }
}
