package net.mach6;

import java.util.Set;

public interface Passable<T> {
    boolean hasPassed();
    Set<T> getPassed();
}
