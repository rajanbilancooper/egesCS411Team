package com.Eges411Team.UnifiedPatientManager.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserSession entity.
 * Tests session state management and getter/setter operations.
 */
class UserSessionTest {

    private UserSession userSession;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        userSession = new UserSession();
        userSession.setId(1L);
        userSession.setUser(testUser);
        userSession.setSessionToken("token123");
        userSession.setCreatedAt(LocalDateTime.now());
        userSession.setExpiresAt(LocalDateTime.now().plusHours(1));
        userSession.setIpAddress("192.168.1.1");
        userSession.setIsActive(true);
        userSession.setLastActivityTime(LocalDateTime.now());
    }

    @Test
    void testUserSessionGettersSetters() {
        // Test Id
        userSession.setId(5L);
        assertEquals(5L, userSession.getId());

        // Test User
        User newUser = new User();
        newUser.setId(2L);
        userSession.setUser(newUser);
        assertEquals(newUser, userSession.getUser());

        // Test Session Token
        userSession.setSessionToken("newToken456");
        assertEquals("newToken456", userSession.getSessionToken());

        // Test Created At
        LocalDateTime created = LocalDateTime.now().minusHours(2);
        userSession.setCreatedAt(created);
        assertEquals(created, userSession.getCreatedAt());

        // Test Expires At
        LocalDateTime expires = LocalDateTime.now().plusHours(2);
        userSession.setExpiresAt(expires);
        assertEquals(expires, userSession.getExpiresAt());

        // Test IP Address
        userSession.setIpAddress("10.0.0.1");
        assertEquals("10.0.0.1", userSession.getIpAddress());

        // Test Is Active
        userSession.setIsActive(false);
        assertFalse(userSession.getIsActive());

        // Test Last Activity Time
        LocalDateTime lastActivity = LocalDateTime.now().minusMinutes(5);
        userSession.setLastActivityTime(lastActivity);
        assertEquals(lastActivity, userSession.getLastActivityTime());
    }

    @Test
    void testUserSessionActiveState() {
        assertTrue(userSession.getIsActive());
        
        userSession.setIsActive(false);
        assertFalse(userSession.getIsActive());
    }

    @Test
    void testUserSessionTokenManagement() {
        String originalToken = userSession.getSessionToken();
        assertNotNull(originalToken);
        assertEquals("token123", originalToken);

        String newToken = "updated-token-789";
        userSession.setSessionToken(newToken);
        assertEquals(newToken, userSession.getSessionToken());
    }
}
