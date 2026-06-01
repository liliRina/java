package car.leasing.exception;

public class DeletionNotAllowedException extends RuntimeException {
    public DeletionNotAllowedException(String objClass, String nameParam, Object param){
        super(objClass + " с " + nameParam + " = " + param + " недоступен для удаления");
    }
}
