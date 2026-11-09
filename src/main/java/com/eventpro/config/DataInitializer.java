package com.eventpro.config;

import com.eventpro.entity.*;
import com.eventpro.enums.StatutEvenement;
import com.eventpro.enums.TypeReduction;
import com.eventpro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final AdministrateurRepository administrateurRepository;
    private final OrganisateurRepository organisateurRepository;
    private final ClientRepository clientRepository;
    private final AgentControleRepository agentControleRepository;
    private final EvenementRepository evenementRepository;
    private final PromotionRepository promotionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (administrateurRepository.count() > 0) {
            return;
        }

        log.info("Initialisation du jeu de données de démonstration EventPro...");

        // Admin
        Administrateur admin = Administrateur.builder()
                .nom("Admin Principal")
                .email("admin@eventpro.ma")
                .build();
        administrateurRepository.save(admin);

        // Organisateur
        Organisateur organisateur = Organisateur.builder()
                .nom("Casablanca Live Events")
                .email("contact@casablancalive.ma")
                .nomOrganisation("Casablanca Live Prod")
                .build();
        organisateur = organisateurRepository.save(organisateur);

        // Client
        Client client = Client.builder()
                .nom("Youssef Amrani")
                .email("youssef.amrani@gmail.com")
                .telephone("+212612345678")
                .build();
        clientRepository.save(client);

        // Agent de contrôle
        AgentControle agent = AgentControle.builder()
                .nom("Agent Entrée 1")
                .email("agent1@eventpro.ma")
                .build();
        agentControleRepository.save(agent);

        // Événement exemple : Concert à Casablanca
        Evenement concert = Evenement.builder()
                .titre("Concert Grand Casablanca 2026")
                .description("Grande soirée musicale au complexe Mohammed V")
                .lieu("Complexe Sportif Mohammed V, Casablanca")
                .dateDebut(LocalDateTime.now().plusDays(15))
                .dateFin(LocalDateTime.now().plusDays(15).plusHours(4))
                .dateOuvertureVente(LocalDateTime.now().minusDays(2))
                .dateClotureVente(LocalDateTime.now().plusDays(14))
                .statut(StatutEvenement.PUBLISHED)
                .organisateur(organisateur)
                .build();

        CategorieBillet catVip = CategorieBillet.builder()
                .nom("VIP")
                .prixUnitaire(new BigDecimal("800.00"))
                .quantiteTotale(100)
                .placesReservees(0)
                .placesVendues(0)
                .quotaMaxParClient(5)
                .evenement(concert)
                .build();

        CategorieBillet catStandard = CategorieBillet.builder()
                .nom("Standard")
                .prixUnitaire(new BigDecimal("300.00"))
                .quantiteTotale(500)
                .placesReservees(0)
                .placesVendues(0)
                .quotaMaxParClient(10)
                .evenement(concert)
                .build();

        concert.setCategories(List.of(catVip, catStandard));
        evenementRepository.save(concert);

        // Promotions de démonstration
        Promotion promoGlobal = Promotion.builder()
                .code("WELCOME20")
                .typeReduction(TypeReduction.PERCENTAGE)
                .valeurReduction(new BigDecimal("20.00"))
                .dateDebut(LocalDateTime.now().minusDays(1))
                .dateFin(LocalDateTime.now().plusMonths(1))
                .limiteUtilisation(50)
                .nombreUtilisations(0)
                .nombreReservees(0)
                .build();
        promotionRepository.save(promoGlobal);

        Promotion promoFixe = Promotion.builder()
                .code("CASA50")
                .typeReduction(TypeReduction.FIXED_AMOUNT)
                .valeurReduction(new BigDecimal("50.00"))
                .dateDebut(LocalDateTime.now().minusDays(1))
                .dateFin(LocalDateTime.now().plusMonths(1))
                .limiteUtilisation(100)
                .nombreUtilisations(0)
                .nombreReservees(0)
                .evenement(concert)
                .organisateur(organisateur)
                .build();
        promotionRepository.save(promoFixe);

        log.info("Jeu de données initialisé avec succès !");
    }
}
