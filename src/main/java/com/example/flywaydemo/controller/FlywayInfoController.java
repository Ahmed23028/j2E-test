package com.example.flywaydemo.controller;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour afficher des informations sur Flyway
 * 
 * Ce contrôleur permet de voir l'état des migrations Flyway
 */
@RestController
@RequestMapping("/api/flyway")
public class FlywayInfoController {
    
    @Autowired(required = false)
    private Flyway flyway;
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getFlywayInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (flyway != null) {
            info.put("enabled", true);
            
            // Informations sur les migrations
            var migrations = flyway.info().all();
            var current = flyway.info().current();
            
            info.put("totalMigrations", migrations.length);
            info.put("currentVersion", current != null && current.getVersion() != null 
                ? current.getVersion().toString() 
                : "baseline");
            
            info.put("migrations", java.util.Arrays.stream(migrations)
                .map(m -> {
                    Map<String, Object> mig = new HashMap<>();
                    mig.put("version", m.getVersion() != null ? m.getVersion().toString() : "baseline");
                    mig.put("description", m.getDescription());
                    mig.put("type", m.getType().toString());
                    mig.put("state", m.getState().toString());
                    if (m.getInstalledOn() != null) {
                        mig.put("installedOn", m.getInstalledOn().toString());
                    }
                    return mig;
                })
                .toList());
        } else {
            info.put("enabled", false);
            info.put("message", "Flyway n'est pas configuré");
        }
        
        return ResponseEntity.ok(info);
    }
}
