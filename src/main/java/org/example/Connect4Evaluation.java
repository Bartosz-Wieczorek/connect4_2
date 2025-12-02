package org.example;

import sac.State;
import sac.StateFunction;

public class Connect4Evaluation extends StateFunction {
    @Override
    public double calculate(State state) {
        Connect4 c4 = (Connect4) state;
        if (c4.checkWin()) return (c4.isMaximizingTurnNow()) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        return 0.0; // TODO zadanie domowe
    }
}