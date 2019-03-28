package demo.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
public class RpcFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcInterface;

    @Autowired
    RpcFactory rpcFactory;

    public RpcFactoryBean(){}

    public RpcFactoryBean(Class<T> rpcInterface){
        this.rpcInterface = rpcInterface;
    }

    public T getObject() throws RuntimeException {
        return getRpc();
    }
    public boolean isSingleton() {
        return true;
    }
    @Override
    public Class<?> getObjectType() {
        return this.rpcInterface;
    }

    public <T> T getRpc(){
        return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(),new Class[]{rpcInterface},rpcFactory);
    }
}
