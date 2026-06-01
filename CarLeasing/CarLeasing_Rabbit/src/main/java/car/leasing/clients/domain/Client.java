package car.leasing.clients.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import car.leasing.contracts.listeners.ClientDeleteListener;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "clients")
@EntityListeners(ClientDeleteListener.class)
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long ID;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 10, nullable = false, unique = true)
    private String passportNumber;

    @Column(length = 12, nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "client_roles",
            joinColumns = @JoinColumn(name = "client_id")
    )
    @Column(name = "role")
    private Set<String> roles = new HashSet<>(Set.of("USER"));

    public Client(
            Long ID,
            String fullName,
            String passportNumber,
            String phoneNumber,
            String login,
            String password,
            Status status
    ){
        this.ID = ID;
        this.fullName = fullName.strip();
        this.passportNumber = passportNumber.strip();
        this.phoneNumber = phoneNumber.strip();
        this.login = login.strip();
        this.password = password.strip();
        this.status = status;
    }

    public Client(
            String fullName,
            String passportNumber,
            String phoneNumber,
            String login,
            String password
    ){
        this(null, fullName, passportNumber, phoneNumber, login, password, Status.NotActiveContract);
    }

    public enum Status{
        HasActiveContract,
        NotActiveContract;
        @Override
        public String toString() {
            return switch (this) {
                case HasActiveContract -> "есть активный договор";
                case NotActiveContract -> "нет активных договоров";
            };
        }
        public static Client.Status fromString(String val){
            return switch (val){
                case "есть активный договор" -> HasActiveContract;
                default -> NotActiveContract;
            };
        }
    }

    @Override
    public String toString(){
        return ID + " " + fullName + " " + passportNumber + " " + phoneNumber;
    }
    public static boolean checkFullName(String name){
        if (name == null)
            return false;
        name = name.strip();
        return name.matches("^[A-Za-zА-Яа-я]+(?:[ -][A-Za-zА-Яа-я]+)*$");
    }

    public static boolean checkPassportNumber(String passportNumber){
        if (passportNumber == null)
            return false;
        passportNumber = passportNumber.strip();
        return passportNumber.length() == 10 &&
                passportNumber.chars().allMatch(c -> c >= '0' && c <= '9');
    }
    public static boolean checkPhoneNumber(String phoneNumber){
        if (phoneNumber == null)
            return false;
        phoneNumber = phoneNumber.strip();
        return phoneNumber.matches("^\\+7\\d{10}$");
    }
}