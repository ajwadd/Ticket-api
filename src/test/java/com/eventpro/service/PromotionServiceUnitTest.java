package com.eventpro.service;

import com.eventpro.entity.Evenement;
import com.eventpro.entity.Promotion;
import com.eventpro.enums.TypeReduction;
import com.eventpro.exception.ValidationException;
import com.eventpro.mapper.PromotionMapper;
import com.eventpro.repository.EvenementRepository;
import com.eventpro.repository.OrganisateurRepository;
import com.eventpro.repository.PromotionRepository;
import com.eventpro.service.impl.PromotionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceUnitTest {

    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private EvenementRepository evenementRepository;
    @Mock
    private OrganisateurRepository organisateurRepository;
    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion promoPourcentage;
    private Promotion promoMontantFixe;
    private Evenement evenement;

    @BeforeEach
    void setUp() {
        evenement = Evenement.builder()
                .id(1L)
                .titre("Concert Test")
                .build();

        promoPourcentage = Promotion.builder()
                .id(10L)
                .code("PROMO20")
                .typeReduction(TypeReduction.PERCENTAGE)
                .valeurReduction(new BigDecimal("20.00"))
                .dateDebut(LocalDateTime.now().minusDays(1))
                .dateFin(LocalDateTime.now().plusDays(10))
                .limiteUtilisation(10)
                .nombreUtilisations(0)
                .nombreReservees(0)
                .build();

        promoMontantFixe = Promotion.builder()
                .id(20L)
                .code("FIXE50")
                .typeReduction(TypeReduction.FIXED_AMOUNT)
                .valeurReduction(new BigDecimal("50.00"))
                .dateDebut(LocalDateTime.now().minusDays(1))
                .dateFin(LocalDateTime.now().plusDays(10))
                .limiteUtilisation(5)
                .nombreUtilisations(0)
                .nombreReservees(0)
                .build();
    }

    @Test
    @DisplayName("Calcul réduction en pourcentage : 20% sur 800 MAD donne 160 MAD")
    void testCalculReductionPourcentage() {
        BigDecimal montantBrut = new BigDecimal("800.00");
        BigDecimal reduction = promoPourcentage.calculerReduction(montantBrut);

        assertEquals(new BigDecimal("160.00"), reduction);
        BigDecimal montantNet = montantBrut.subtract(reduction).max(BigDecimal.ZERO);
        assertEquals(new BigDecimal("640.00"), montantNet);
    }

    @Test
    @DisplayName("Calcul réduction montant fixe : 50 MAD sur 300 MAD donne 50 MAD")
    void testCalculReductionMontantFixe() {
        BigDecimal montantBrut = new BigDecimal("300.00");
        BigDecimal reduction = promoMontantFixe.calculerReduction(montantBrut);

        assertEquals(new BigDecimal("50.00"), reduction);
        BigDecimal montantNet = montantBrut.subtract(reduction).max(BigDecimal.ZERO);
        assertEquals(new BigDecimal("250.00"), montantNet);
    }

    @Test
    @DisplayName("Garantie que la réduction ne dépasse pas le montant brut (montant net non négatif)")
    void testCalculReductionPlafonnee() {
        BigDecimal montantBrut = new BigDecimal("30.00"); // Inférieur à la réduction fixe de 50 MAD
        BigDecimal reduction = promoMontantFixe.calculerReduction(montantBrut);

        assertEquals(new BigDecimal("30.00"), reduction);
        BigDecimal montantNet = montantBrut.subtract(reduction).max(BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), montantNet);
    }

    @Test
    @DisplayName("Validation du quota promo : quota épuisé doit lever une exception")
    void testQuotaEpuise() {
        promoPourcentage.setNombreUtilisations(10); // Limite atteinte (10/10)

        when(promotionRepository.findByCodeWithLock("PROMO20")).thenReturn(Optional.of(promoPourcentage));

        assertThrows(ValidationException.class, () ->
                promotionService.validerEtObtenirPromotion("PROMO20", evenement, new BigDecimal("800.00")));
    }
}
