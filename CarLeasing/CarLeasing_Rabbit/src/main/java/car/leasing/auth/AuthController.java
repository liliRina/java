package car.leasing.auth;

import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import car.leasing.clients.Clients;
import car.leasing.clients.domain.Client;
import car.leasing.clients.domain.ClientRequest;

import java.util.List;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final Clients clients;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ClientRequest request) {
        Client client = new Client(request.fullName(), request.passportNumber(), request.phoneNumber()
                , request.login(), passwordEncoder.encode(request.password()));
        clients.register(client);
        return ResponseEntity
                .accepted()
                .body("Клиент отправлен на обработку");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate( //userDetailsService.loadUserByUsername("*") passwordEncoder.matches("*", хэшИзБД)
                new UsernamePasswordAuthenticationToken( // просто непроверенный токен
                        request.login,
                        request.password
                )
        );

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList();

        String token = JwtUtil.generateToken(request.login, roles);
        return ResponseEntity.ok(token);
    }
    record AuthRequest (String login, String password){}
}

