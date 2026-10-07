package br.edu.umc.loginseguro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Preenche automaticamente createdAt/updatedAt dos documentos. */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
