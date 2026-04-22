package com.genius.budgetmanager.service;

import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.Expense;
import com.genius.budgetmanager.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository repository;

    public List<Campaign> getAllCampaigns() {
        return repository.findAll();
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return repository.findAll().stream()
                .filter(c -> c.getStatus().equalsIgnoreCase(status)) // se corrigió c.getType() por c.getStatus()
                .collect(Collectors.toList());
    }

    public Campaign getCampaignById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + id));
    }

    public BudgetSummary getBudgetSummary(Long campaignId) {
        Campaign campaign = getCampaignById(campaignId);

        BudgetSummary summary = new BudgetSummary();
        summary.setCampaignId(campaign.getId());
        summary.setCampaignName(campaign.getName());
        summary.setClient(campaign.getClient());
        summary.setTotalBudget(campaign.getBudget());
        summary.setSpent(campaign.getSpent());
        summary.setRemaining(campaign.getBudget() - campaign.getSpent()); // se corrigió campaign.getBudget() por campaign.getSpent()
        summary.setPercentageUsed(
                campaign.getBudget() != null && campaign.getBudget() > 0 // se agrega operador ternairo para validar presupuesto
                        ? Math.round((campaign.getSpent() / campaign.getBudget()) * 10000.0) / 100.0
                        : 0.0
        );
        return summary;
    }

    public List<Expense> getExpensesByCampaign(Long campaignId) {
        return repository.findExpensesByCampaignId(campaignId);
    }

    public Expense addExpense(Long campaignId, Expense expense) {
        Campaign campaign = getCampaignById(campaignId);
        if (expense.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        List<String> validCategories = List.of("ads_spend", "creative", "tools", "agency_fee");
        if (!validCategories.contains(expense.getCategory())) {
            throw new IllegalArgumentException("Invalid category: " + expense.getCategory());
        }
        if (expense.getAmount() == null || expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount is required");
        }
        expense.setCampaignId(campaignId);
        campaign.setSpent(campaign.getSpent() + expense.getAmount());
        return repository.saveExpense(expense);
    }

    public Campaign updateBudget(Long campaignId, Double newBudget) {
        Campaign campaign = getCampaignById(campaignId);
        campaign.setBudget(newBudget);
        return campaign;
    }
}
