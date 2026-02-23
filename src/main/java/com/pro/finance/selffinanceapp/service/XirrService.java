package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.SipTransaction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class XirrService {

    public static double calculate(List<SipTransaction> txns,
                                   double currentValue) {

        if (txns == null || txns.size() < 2 || currentValue <= 0) {
            return 0;
        }

        // 🔥 Ensure sorted by date
        txns.sort(Comparator.comparing(SipTransaction::getInvestDate));

        double guess = 0.12; // 12% realistic starting guess
        double tolerance = 0.0001;
        int maxIterations = 1000;

        for (int i = 0; i < maxIterations; i++) {

            double f = npv(txns, currentValue, guess);
            double df = derivative(txns, currentValue, guess);

            if (Math.abs(df) < 1e-10) break;

            double newGuess = guess - f / df;

            // 🔥 Safety check
            if (Double.isNaN(newGuess) || Double.isInfinite(newGuess)) {
                return 0;
            }

            if (Math.abs(newGuess - guess) <= tolerance) {
                return newGuess * 100; // return %
            }

            guess = newGuess;
        }

        return 0;
    }

    private static double npv(List<SipTransaction> txns,
                              double currentValue,
                              double rate) {

        double total = 0;

        long baseDate = txns.get(0).getInvestDate().toEpochDay();

        for (SipTransaction txn : txns) {

            long days = txn.getInvestDate().toEpochDay() - baseDate;
            double years = days / 365.0;

            total += -txn.getAmount().doubleValue()
                    / Math.pow(1 + rate, years);
        }

        long currentDays =
                LocalDate.now().toEpochDay() - baseDate;

        double currentYears = currentDays / 365.0;

        total += currentValue
                / Math.pow(1 + rate, currentYears);

        return total;
    }

    private static double derivative(List<SipTransaction> txns,
                                     double currentValue,
                                     double rate) {

        double total = 0;

        long baseDate = txns.get(0).getInvestDate().toEpochDay();

        for (SipTransaction txn : txns) {

            long days = txn.getInvestDate().toEpochDay() - baseDate;
            double years = days / 365.0;

            total += years * txn.getAmount().doubleValue()
                    / Math.pow(1 + rate, years + 1);
        }

        long currentDays =
                LocalDate.now().toEpochDay() - baseDate;

        double currentYears = currentDays / 365.0;

        total -= currentYears * currentValue
                / Math.pow(1 + rate, currentYears + 1);

        return total;
    }
}