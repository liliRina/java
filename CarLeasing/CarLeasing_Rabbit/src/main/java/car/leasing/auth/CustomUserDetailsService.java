package car.leasing.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import car.leasing.clients.ClientRepository;
import car.leasing.clients.domain.Client;
import car.leasing.exception.InvalidParameterException;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;
    public CustomUserDetailsService(ClientRepository clientRepository){
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login){
        Client client = clientRepository.findByLogin(login)
                .orElseThrow(() -> new InvalidParameterException("Неверный логин или/и пароль"));

        return new org.springframework.security.core.userdetails.User(
                client.getLogin(),
                client.getPassword(),
                client.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList())
        );
    }
}