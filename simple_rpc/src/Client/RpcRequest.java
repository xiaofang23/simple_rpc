package Client;

import java.io.Serializable;

public class RpcRequest implements Serializable {
    public String serviceName;
    public String methodName;
    public Class[] parametersType;
    public Object[] parameters;
    public RpcRequest(){

    }
    public RpcRequest(String serviceName, String methodName, Class[] parametersType, Object[] parameters) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parametersType = parametersType;
        this.parameters = parameters;
    }
    public String getServiceName() {
        return serviceName;
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
