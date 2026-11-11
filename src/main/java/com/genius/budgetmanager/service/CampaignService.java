package com.genius.budgetmanager.service;

import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.Expense;
import com.genius.budgetmanager.model.GlobalBudgetSummary;
import com.genius.budgetmanager.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository repository;

    public List<Campaign> getAllCampaigns() {
        return repository.findAll();
    }
    
    public List<Campaign> searchCampaigns(String status, String client) {
        return repository.findAll().stream()
                .filter(c -> status == null || status.isBlank() || c.getStatus().equalsIgnoreCase(status))
                .filter(c -> client == null || client.isBlank() || c.getClient().equalsIgnoreCase(client))
                .collect(Collectors.toList());
    }
    
    public List<Campaign> getCampaignsByStatus(String status) {
        return repository.findAll().stream()
                .filter(c -> c.getStatus().equalsIgnoreCase(status))
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
        summary.setRemaining(campaign.getBudget() - campaign.getSpent());
        summary.setPercentageUsed(
                Math.round((campaign.getSpent() / campaign.getBudget()) * 10000.0) / 100.0
        );

        return summary;
    }

    public List<Expense> getExpensesByCampaign(Long campaignId) {
        getCampaignById(campaignId);
        return repository.findExpensesByCampaignId(campaignId);
    }

    public Expense addExpense(Long campaignId, Expense expense) {
        Campaign campaign = getCampaignById(campaignId);
        expense.setCampaignId(campaignId);
        campaign.setSpent(campaign.getSpent() + expense.getAmount());
        return repository.saveExpense(expense);
    }

    public GlobalBudgetSummary getGlobalBudgetSummary() {
        List<Campaign> active = repository.findAll().stream()
                .filter(c -> "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());

        double totalBudget    = active.stream().mapToDouble(Campaign::getBudget).sum();
        double totalSpent     = active.stream().mapToDouble(Campaign::getSpent).sum();
        double totalAvailable = totalBudget - totalSpent;
        double pct = totalBudget > 0
                ? Math.round((totalSpent / totalBudget) * 10000.0) / 100.0
                : 0.0;

        GlobalBudgetSummary summary = new GlobalBudgetSummary();
        summary.setActiveCampaigns(active.size());
        summary.setTotalBudget(totalBudget);
        summary.setTotalSpent(totalSpent);
        summary.setTotalAvailable(totalAvailable);
        summary.setConsumptionPercentage(pct);
        return summary;
    }

    public Campaign updateBudget(Long campaignId, Double newBudget) {
        Campaign campaign = getCampaignById(campaignId);
        campaign.setSpent(0.0);
        campaign.setBudget(newBudget);
        return campaign;
    }
    
    public Campaign createCampaign(Campaign campaign) {
        if (campaign.getName() == null || campaign.getName().isBlank()
                || campaign.getClient() == null || campaign.getClient().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name y client son obligatorios");
        }
        if (campaign.getBudget() == null || campaign.getBudget() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "budget debe ser mayor a 0");
        }
        campaign.setSpent(0.0);
        if (campaign.getStatus() == null || campaign.getStatus().isBlank()) {
            campaign.setStatus("draft");
        }
        return repository.saveCampaign(campaign);
    }
}
