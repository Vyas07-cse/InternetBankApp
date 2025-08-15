package com.example.internetbanking.util;

import java.util.Arrays;
import java.util.List;

public class BillTypeValidator {
    private static final List<String> VALID_TYPES = Arrays.asList("electricity", "water", "gas", "internet");

    public static boolean isValid(String type) {
        if (type == null) return false;
        return VALID_TYPES.contains(type.toLowerCase());
    }

    public static List<String> getValidTypes() {
        
        return Arrays.asList("Electricity", "Water", "Gas", "Internet");
    }
}
