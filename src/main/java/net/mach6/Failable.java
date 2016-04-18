package net.mach6;

import java.util.Set;

public interface Failable<T> {
    boolean hasFailures();
    Set<T> getFailures();
}
