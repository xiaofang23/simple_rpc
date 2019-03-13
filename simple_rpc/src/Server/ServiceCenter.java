package Server;

public interface ServiceCenter {

    void start();

    void close();

    void register(Class service,Class serviceImp);
}
