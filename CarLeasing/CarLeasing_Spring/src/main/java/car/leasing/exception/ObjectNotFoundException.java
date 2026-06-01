package car.leasing.exception;

public class ObjectNotFoundException extends RuntimeException{
    public ObjectNotFoundException(String objClass, String nameSearchParam, Object searchParam){
        super(objClass + " c " + nameSearchParam + " = " + searchParam + " не найден");
    }
}
