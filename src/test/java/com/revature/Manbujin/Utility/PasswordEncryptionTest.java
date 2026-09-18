package com.revature.Manbujin.Utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncryptionTest {

    @Test
    void encrypt_validPassword() {
        String password = "password123";

        String encrypted = PasswordEncryption.encrypt(password);

        assertNotNull(encrypted);
        assertNotEquals(password, encrypted);
    }

    @Test
    void encrypt_nullPassword() {
        assertThrows(
                RuntimeException.class,
                () -> PasswordEncryption.encrypt(null)
        );
    }


    @Test
    void decrypt_validEncryptedPassword() {
        String password = "password123";

        String encrypted = PasswordEncryption.encrypt(password);
        String decrypted = PasswordEncryption.decrypt(encrypted);

        assertEquals(password, decrypted);
    }

    @Test
    void decrypt_invalidEncryptedPassword() {
        String invalidPassword = "not-valid-encryption";

        assertThrows(
                RuntimeException.class,
                () -> PasswordEncryption.decrypt(invalidPassword)
        );
    }
}