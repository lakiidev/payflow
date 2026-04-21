package com.payflow.infrastructure;

import com.payflow.TestcontainersConfiguration;
import com.payflow.application.service.WalletService;
import com.payflow.domain.model.user.User;
import com.payflow.domain.model.wallet.Wallet;
import com.payflow.infrastructure.persistence.jpa.UserJpaRepository;
import com.payflow.infrastructure.persistence.jpa.WalletJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

class CacheIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletJpaRepository walletRepository;

    @Autowired
    private CacheManager cacheManager;

    private UUID walletId;
    private UUID userId;

    @Autowired
    private UserJpaRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("test@payflow.com")
                .fullName("Test User")
                .passwordHash("test123").build();
        user = userRepository.save(user);
        userId = user.getId();

        Wallet wallet = Wallet.create(userId, Currency.getInstance("EUR"));
        wallet = walletService.save(wallet);
        walletId = wallet.getId();
    }

    @AfterEach
    void tearDown() {
        walletRepository.deleteAll();
        userRepository.deleteAll();
        cacheManager.getCache("wallets").clear();
    }


    @Test
    void getActiveByIdFirstCallPopulatesCache() {
        // Given
        walletService.getActiveById(walletId, userId);

        // When
        Cache cache = cacheManager.getCache("wallets");

        // Then
        assertNotNull(cache.get(walletId + ":" + userId));
    }

    @Test
    void getActiveByIdSecondCallReturnsCachedValue() {
        // Given
        Wallet first = walletService.getActiveById(walletId, userId);
        Wallet second = walletService.getActiveById(walletId, userId);

        // Then
        assertEquals(first.getId(), second.getId());
    }


    @Test
    void saveEvictsCache() {
        // Given
        walletService.getActiveById(walletId, userId);
        assertNotNull(cacheManager.getCache("wallets")
                .get(walletId + ":" + userId));

        // When
        Wallet wallet = walletService.getActiveById(walletId, userId);
        walletService.save(wallet);

        // Then
        assertNull(cacheManager.getCache("wallets")
                .get(walletId + ":" + userId));
    }
}
