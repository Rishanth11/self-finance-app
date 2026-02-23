package com.pro.finance.selffinanceapp.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class XirrCalculator {

    public static double calculate(List<Double> amounts, List<LocalDate> dates) {

        if (amounts.size() < 2) return 0;

        double guess = 0.1;

        for (int i = 0; i < 1000; i++) {

            double fValue = xnpv(guess, amounts, dates);
            double derivative = dxnpv(guess, amounts, dates);

            if (Math.abs(derivative) < 1e-10) break;

            double newGuess = guess - fValue / derivative;

            if (Math.abs(newGuess - guess) < 0.0001)
                return newGuess * 100;

            guess = newGuess;
        }

        return guess * 100;
    }

    private static double xnpv(double rate, List<Double> amounts, List<LocalDate> dates) {
        double sum = 0;
        LocalDate start = dates.get(0);
        for (int i = 0; i < amounts.size(); i++) {
            double days = ChronoUnit.DAYS.between(start, dates.get(i)) / 365.0;
            sum += amounts.get(i) / Math.pow(1 + rate, days);
        }
        return sum;
    }

    private static double dxnpv(double rate, List<Double> amounts, List<LocalDate> dates) {
        double sum = 0;
        LocalDate start = dates.get(0);
        for (int i = 0; i < amounts.size(); i++) {
            double days = ChronoUnit.DAYS.between(start, dates.get(i)) / 365.0;
            sum += -days * amounts.get(i) / Math.pow(1 + rate, days + 1);
        }
        return sum;
    }
}
