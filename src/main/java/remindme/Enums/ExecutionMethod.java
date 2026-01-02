package remindme.Enums;

public enum ExecutionMethod {
    PC_STARTUP("Pc Startup"),
    CUSTOM_TIME_RANGE("Custom Time Range"),
    ONE_TIME_PER_DAY("One Time Per Day");

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

    public static int executionMethodPriority(ExecutionMethod method) {
        return switch (method) {
            case ONE_TIME_PER_DAY -> 1;
            case CUSTOM_TIME_RANGE -> 2;
            case PC_STARTUP -> 3;
        };
    }

    public static ExecutionMethod getDefaultExecutionMethod() {
        return PC_STARTUP;
    }
}
