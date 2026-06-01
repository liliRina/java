package car.leasing.exception;

public class PayClosedContractException extends RuntimeException{
    public PayClosedContractException(Long id){
        super("Договор с №" + id + " уже закрыт");
    }
}
