package net.mach6;

import java.util.Set;

public interface Skipable<T> {
    boolean hasSkips();
    Set<T> getSkips();
}
