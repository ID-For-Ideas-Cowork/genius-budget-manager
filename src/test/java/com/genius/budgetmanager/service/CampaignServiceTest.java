package com.genius.budgetmanager.service;

import com.genius.budgetmanager.repository.CampaignRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.genius.budgetmanager.model.BudgetSummary;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CampaignServiceTest {

    @Autowired
    private CampaignService service;


    // Traer lista de todas las campañas
    @Test
    void getAllCampaigns_devuelveLista() {
        List<Campaign> campaigns = service.getAllCampaigns();
        assertNotNull(campaigns);
        assertFalse(campaigns.isEmpty());
    }

    // Filtrar campañas por estado
    @Test
    void getCampaignsByStatus_filtraCorrectamente() {
        List<Campaign> result = service.getCampaignsByStatus("active");
        assertFalse(result.isEmpty());
        assertTrue(result.stream()
                .allMatch(c -> c.getStatus().equalsIgnoreCase("active")));
    }

    // Traer 1 campaña por ID
    @Test
    void getCampaignById_existente_devuelveCampaign() {
        Campaign campaign = service.getCampaignById(2L);
        assertNotNull(campaign);
        assertEquals(2L, campaign.getId());
    }

    // Lanza excepcion si el ID de la campaña no existe
    @Test
    void getCampaignById_inexistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> service.getCampaignById(29L));
    }

    // Mostrar el total restante del presupuesto para una campaña especifica
    @Test
    void getBudgetSummary_calculaRemainingCorrectamente() {
        BudgetSummary summary = service.getBudgetSummary(3L);
        double expected = 120000.0 - 67800.0;
        assertEquals(expected, 52200.0);
    }

    // Mostrar porcentaje de presupuesto gastado
    @Test
    void getBudgetSummary_calculaPorcentajeCorrecto() {
        BudgetSummary summary = service.getBudgetSummary(3L);
        double expected = Math.round((67800.0 / 120000.0) * 10000.0) / 100.0;
        assertEquals(expected, 56.5);
    }

    // Lanza excepcion si la campaña no existe
    @Test
    void getBudgetSummary_campaignInexistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> service.getBudgetSummary(99L));
    }

    // Muestra la lista de gastos realizados para una campaña
    @Test
    void getExpensesByCampaign_devuelveLista() {
        List<Expense> expenses = service.getExpensesByCampaign(3L);
        assertNotNull(expenses);
        assertFalse(expenses.isEmpty());
    }

    // Agregar nuevo gasto a la campaña, debe guardar el gasto y actualizar el spent de la campaña
    @Test
    void addExpense_actualizaSpentYGuardaExpense() {
        // Busco la campaña por id
        Campaign campaign = service.getCampaignById(3L);
        // Creo el expense
        Expense expense = new Expense(null, null, "Nuevo gasto de prueba", 1000.0, "ads_spend", "2026-04-21");
        // Lo guardo en la BD
        Expense saved = service.addExpense(3L, expense);

        // Actualiza spent de la campaña
        Campaign updateCampaign = service.getCampaignById(3L);
        assertNotNull(saved.getId());
        assertEquals(68800.0, updateCampaign.getSpent());
    }

    // Lanza excepcion con categoria invalida en addExpense
    @Test
    void addExpense_categoriaInvalida_lanzaExcepcion() {
        Expense expense = new Expense(null, null, "Gasto de prueba", 1000.0, "invalid_category", "2026-04-21");
        assertThrows(IllegalArgumentException.class,
                () -> service.addExpense(3L, expense));
    }

    //Lanza excepcion con campaña inexistente
    @Test
    void addExpense_campaignInexistente_lanzaExcepcion() {
        Expense expense = new Expense(null, null, "Otro gasto de prueba", 1000.0, "ads_spend", "2026-05-01");
        assertThrows(RuntimeException.class, () -> service.addExpense(999L, expense));
    }

    // Actualizar presupuesto
    @Test
    void updateBudget_actualizaBudget() {
        Campaign updated = service.updateBudget(6L, 75000.0);
        assertEquals(75000.0, updated.getBudget());
    }

    // Lanza excepcion con campaña inexistente
    @Test
    void updateBudget_campaignInexistente_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> service.updateBudget(999L, 50000.0));
    }
}