package br.edu.umc.loginseguro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Sessões HTTP armazenadas na coleção {@code sessions} do MongoDB Atlas
 * (Spring Session). Assim o login sobrevive a reinícios da aplicação e
 * funciona com várias instâncias atrás de um balanceador.
 */
@Configuration
@EnableMongoHttpSession(collectionName = "sessions", maxInactiveIntervalInSeconds = SessionConfig.TIMEOUT_SECONDS)
public class SessionConfig {

    /** Sessão expira após 30 minutos sem atividade. */
    public static final int TIMEOUT_SECONDS = 30 * 60;

    /** Cookie de sessão protegido contra leitura por JavaScript e envio entre sites. */
    @Bean
    public CookieSerializer cookieSerializer(@Value("${app.session.cookie-secure:false}") boolean secure) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(SecurityConfig.SESSION_COOKIE);
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax");
        serializer.setUseSecureCookie(secure);
        serializer.setCookiePath("/");
        return serializer;
    }
}
