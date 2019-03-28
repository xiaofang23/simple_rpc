package demo.rpc_entity;

import java.io.Serializable;

public class RpcRequest implements Serializable {

    private String id;
    private String serviceName;
    private String methodName;
    private Class[] parametersType;
    private Object[] parameters;
    public RpcRequest(){
        this.id = new String();
        this.serviceName = new String();
        this.methodName = new String();
        this.parametersType = new Class[]{};
        this.parameters = new Object[]{};
    }
    public RpcRequest(String id,String serviceName, String methodName, Class[] parametersType, Object[] parameters) {
        this.id =id ;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parametersType = parametersType;
        this.parameters = parameters;
    }

    public String getServiceName() {
        return serviceName;
    }
    public String getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParametersType() {
        return parametersType;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setID(String id) {
        this.id = id;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParametersType(Class[] parametersType) {
        this.parametersType = parametersType;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
