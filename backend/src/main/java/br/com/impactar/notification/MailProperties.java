package br.com.impactar.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        String fromName,
        SmtpAccount primary,
        SmtpAccount fallback
) {

    public record SmtpAccount(
            String host,
            int port,
            String username,
            String password,
            String from
    ) {

        public boolean isConfigured() {
            return notBlank(host) && notBlank(username) && notBlank(password) && notBlank(from);
        }

        private static boolean notBlank(String value) {
            return value != null && !value.isBlank();
        }
    }
}
