package remindme.Enums;

public enum ExecutionMethod {
    PC_STARTUP("Pc Startup"),
    CUSTOM_TIME_RANGE("Custom Time Range");

    private final String executionMethodName;

    private ExecutionMethod(String executionMethodName) {
        this.executionMethodName = executionMethodName;
    }

    public String getExecutionMethodName() {
        return executionMethodName;
    }

    public static ExecutionMethod getExecutionMethodbyName(String executionMethod) {
        for (ExecutionMethod method : ExecutionMethod.values()) {
            if (method.getExecutionMethodName().equals(executionMethod)) {
                return method;
            }
        }

        return null;
    }

    public static ExecutionMethod getDefaultExecutionMethod() {
        return PC_STARTUP;
    }
}
