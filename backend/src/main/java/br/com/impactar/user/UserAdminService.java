package br.com.impactar.user;

import java.util.Locale;
import java.util.UUID;

import br.com.impactar.auth.TemporaryPasswordGenerator;
import br.com.impactar.notification.CredentialsMailer;
import br.com.impactar.notification.EmailDeliveryException;
import br.com.impactar.shared.ApiException;
import br.com.impactar.shared.PageResponse;
import br.com.impactar.user.dto.CreateUserRequest;
import br.com.impactar.user.dto.UpdateUserRequest;
import br.com.impactar.user.dto.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestão de usuários pela administração.
 *
 * Criação e reenvio de acesso enviam o e-mail DENTRO da transação:
 * se o envio falhar nos dois provedores, nada é gravado e o admin vê o erro.
 */
@Service
public class UserAdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordGenerator passwordGenerator;
    private final CredentialsMailer credentialsMailer;

    public UserAdminService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            TemporaryPasswordGenerator passwordGenerator,
                            CredentialsMailer credentialsMailer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordGenerator = passwordGenerator;
        this.credentialsMailer = credentialsMailer;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String search, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by("name").ascending());

        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        Page<User> result = term.isEmpty()
                ? userRepository.findAll(pageable)
                : userRepository.search(term, pageable);

        return PageResponse.from(result.map(UserResponse::from));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = User.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Já existe um usuário com este e-mail.");
        }

        String temporaryPassword = passwordGenerator.generate();
        User user = userRepository.saveAndFlush(
                new User(request.name().trim(), email, passwordEncoder.encode(temporaryPassword), request.role()));

        try {
            credentialsMailer.sendWelcome(user, temporaryPassword);
        } catch (EmailDeliveryException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar o e-mail com o acesso. O usuário não foi criado.");
        }
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(UUID userId, UpdateUserRequest request, UUID currentAdminId) {
        User user = findUser(userId);

        if (currentAdminId.equals(user.getId())) {
            if (!request.active()) {
                throw ApiException.badRequest("Você não pode desativar o próprio usuário.");
            }
            if (request.role() != Role.ADMIN) {
                throw ApiException.badRequest("Você não pode remover o próprio perfil de administrador.");
            }
        }

        user.setName(request.name().trim());
        user.setRole(request.role());
        user.setActive(request.active());
        return UserResponse.from(user);
    }

    @Transactional
    public void resetPassword(UUID userId) {
        User user = findUser(userId);
        if (!user.isActive()) {
            throw ApiException.badRequest("Ative o usuário antes de reenviar o acesso.");
        }

        String temporaryPassword = passwordGenerator.generate();
        user.resetPassword(passwordEncoder.encode(temporaryPassword));

        try {
            credentialsMailer.sendPasswordReset(user, temporaryPassword);
        } catch (EmailDeliveryException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar o e-mail. A senha não foi alterada.");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuário não encontrado."));
    }
}
