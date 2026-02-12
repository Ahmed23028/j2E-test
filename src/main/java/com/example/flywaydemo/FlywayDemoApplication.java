package com.example.flywaydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application principale Spring Boot avec Flyway
 * 
 * Flyway exécutera automatiquement toutes les migrations au démarrage
 * Les fichiers SQL dans src/main/resources/db/migration seront exécutés
 * dans l'ordre de leur numéro de version (V1, V2, V3, etc.)
 */
@SpringBootApplication
public class FlywayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlywayDemoApplication.class, args);
        System.out.println("\n=========================================");
        System.out.println("Application démarrée avec succès!");
        System.out.println("Flyway a exécuté toutes les migrations");
        System.out.println("Accédez à /h2-console pour voir la base de données");
        System.out.println("=========================================\n");
    }
}
