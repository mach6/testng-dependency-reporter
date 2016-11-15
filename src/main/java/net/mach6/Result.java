/*
 * Copyright (C) 2016 Doug Simmons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 */

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
