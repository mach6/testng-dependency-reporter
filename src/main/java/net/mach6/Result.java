package net.mach6;

public enum Result {
    SKIPPED, FAILED, PASSED;

    public static String getDotStyle(Result result) {
        switch (result) {
        case SKIPPED:
            return "style=filled,color=yellow";
        case FAILED:
            return "style=filled,color=red";
        case PASSED:
            return "style=filled,color=green";
        default:
            return "style=empty";
        }
    }
}
