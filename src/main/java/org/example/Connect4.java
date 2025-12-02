package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import sac.game.AlphaBetaPruning;
import sac.game.GameSearchAlgorithm;
import sac.game.GameSearchConfigurator;
import sac.game.GameState;
import sac.game.GameStateImpl;
import sac.game.MinMax;

public class Connect4 extends GameStateImpl {

    public static final boolean IS_X_AI = false;
    public static final boolean IS_O_AI = true;

    public static final int M = 6;
    public static final int N = 7;

    private static final byte X = 1; // zeton gracza maksymalizujacego
    private static final byte O = -1; // zeton gracza minimalizujacego
    private static final byte EMPTY = 0;
    private static final String[] SYMBOLS = new String[] {"O", ".", "X"};

    private byte[][] board = null;
    private int lastI = -1;
    private int lastJ = -1;
    private int movesCount = 0;

    public Connect4() {
        board = new byte[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                board[i][j] = EMPTY;
    }

    public Connect4(Connect4 parent) {
        board = new byte[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                board[i][j] = parent.board[i][j];
        lastI = parent.lastI;
        lastJ = parent.lastJ;
        movesCount = parent.movesCount;
        setMaximizingTurnNow(parent.isMaximizingTurnNow()); // kopia flagi ruchu
    }

    @Override
    public int hashCode() { // pozwoli SaCowi dla niektorych odwiedzonych wczesniej stanow brac gotowe ocen (nie uruchamiajac przeszukiwan w glab)
        byte[] flatBoard = new byte[M * N];
        int k = 0;
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                flatBoard[k++] = board[i][j];
        return Arrays.hashCode(flatBoard);
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder();
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                txt.append("|");
                txt.append(SYMBOLS[board[i][j] + 1]);
            }
            txt.append("|\n");
        }
        for (int j = 0; j < N; j++) {
            txt.append(" ");
            txt.append(j);
        }

        return txt.toString();
    }

    public boolean makeMove(int j) { // j - nr kolumny
        if ((j < 0) || (j >= N))
            return false;
        for (int i = M - 1; i >= 0; i--)
            if (board[i][j] == EMPTY) {
                board[i][j] = (isMaximizingTurnNow()) ? X : O;
                setMaximizingTurnNow(!isMaximizingTurnNow()); // odbicie flagi ruchu na przeciwną
                lastI = i;
                lastJ = j;
                movesCount++;
                return true;
            }
        return false;
    }

    public boolean checkDraw() { // uruchamiac po checkWin
        return movesCount == M * N;
    }

    public boolean checkWin() {
        if (lastI < 0)
            return false;
        byte lastSymbol = board[lastI][lastJ];
        // prawo-lewo
        int count = 0;
        for (int dj = 1; (dj <= 3) && (lastJ + dj) < N; dj++) {
            if (board[lastI][lastJ + dj] != lastSymbol)
                break;
            count++;
        }
        for (int dj = 1; (dj <= 3) && (lastJ - dj) >= 0; dj++) {
            if (board[lastI][lastJ - dj] != lastSymbol)
                break;
            count++;
        }
        if (count >= 3)
            return true;

        // dol-gora
        count = 0;
        for (int di = 1; (di <= 3) && (lastI + di) < M; di++) {
            if (board[lastI + di][lastJ] != lastSymbol)
                break;
            count++;
        }
        for (int di = 1; (di <= 3) && (lastI - di) >= 0; di++) {
            if (board[lastI - di][lastJ] != lastSymbol)
                break;
            count++;
        }
        if (count >= 3)
            return true;

        // SE-NW
        count = 0;
        for (int dk = 1; (dk <= 3) && (lastI + dk) < M && (lastJ + dk) < N; dk++) {
            if (board[lastI + dk][lastJ + dk] != lastSymbol)
                break;
            count++;
        }
        for (int dk = 1; (dk <= 3) && (lastI - dk) >= 0 && (lastJ - dk) >= 0; dk++) {
            if (board[lastI - dk][lastJ - dk] != lastSymbol)
                break;
            count++;
        }
        if (count >= 3)
            return true;

        // SW-NE
        count = 0;
        for (int dk = 1; (dk <= 3) && (lastI + dk) < M && (lastJ - dk) >= 0; dk++) {
            if (board[lastI + dk][lastJ - dk] != lastSymbol)
                break;
            count++;
        }
        for (int dk = 1; (dk <= 3) && (lastI - dk) >= 0 && (lastJ + dk) < N; dk++) {
            if (board[lastI - dk][lastJ + dk] != lastSymbol)
                break;
            count++;
        }
        if (count >= 3)
            return true;

        return false;
    }

    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        for (int j = 0; j < N; j++) {
            Connect4 child = new Connect4(this);
            if (child.makeMove(j)) {
                children.add(child);
                child.setMoveName(Integer.toString(j));
            }
        }
        return children;
    }

    public static void main(String[] args) {
        Connect4.setHFunction(new Connect4Evaluation()); // podpiecie heurystyki
        Connect4 c4 = new Connect4();
        Scanner scanner = new Scanner(System.in);
        System.out.println(c4);
        GameSearchAlgorithm ai = new AlphaBetaPruning(); // new MinMax();
        GameSearchConfigurator conf = new GameSearchConfigurator();
        conf.setDepthLimit(5.5);
        ai.setConfigurator(conf);
        while (true) {
            if (!IS_X_AI) {
                boolean isMoveLegal = false;
                do {
                    System.out.print("PLAYER X, YOUR MOVE: ");
                    int move = scanner.nextInt();
                    isMoveLegal = c4.makeMove(move);
                } while (!isMoveLegal);
            }
            else {
                ai.setInitial(c4); // nowy korzen przeszukiwan
                System.out.println("AI THINKING...");
                ai.execute();
                System.out.println("TIME [ms]: " + ai.getDurationTime());
                System.out.println("STATES CHECKED: " + ai.getClosedStatesCount());
                System.out.println("DEPTH REACHED: " + ai.getDepthReached());
                System.out.println("MOVES SCORES: " + ai.getMovesScores());
                String bestMove = ai.getFirstBestMove(); // wez pierwszy najelpszy ruch (jesli kilka o tej samej wartosci)
                System.out.println("BEST MOVE: " + bestMove);
                c4.makeMove(Integer.valueOf(bestMove));
            }
            System.out.println(c4);
            if (c4.checkWin()) {
                System.out.println("PLAYER X WINS!");
                break;
            }
            if (c4.checkDraw()) {
                System.out.println("GAME ENDS WITH A DRAW.");
                break;
            }
            if (!IS_O_AI) {
                boolean isMoveLegal = false;
                do {
                    System.out.print("PLAYER O, YOUR MOVE: ");
                    int move = scanner.nextInt();
                    isMoveLegal = c4.makeMove(move);
                } while (!isMoveLegal);
            }
            else {
                ai.setInitial(c4); // nowy korzen przeszukiwan
                System.out.println("AI THINKING...");
                ai.execute();
                System.out.println("TIME [ms]: " + ai.getDurationTime());
                System.out.println("STATES CHECKED: " + ai.getClosedStatesCount());
                System.out.println("DEPTH REACHED: " + ai.getDepthReached());
                System.out.println("MOVES SCORES: " + ai.getMovesScores());
                String bestMove = ai.getFirstBestMove(); // wez pierwszy najelpszy ruch (jesli kilka o tej samej wartosci)
                System.out.println("BEST MOVE: " + bestMove);
                c4.makeMove(Integer.valueOf(bestMove));
            }
            System.out.println(c4);
            if (c4.checkWin()) {
                System.out.println("PLAYER O WINS!");
                break;
            }
            if (c4.checkDraw()) {
                System.out.println("GAME ENDS WITH A DRAW.");
                break;
            }
        } // koniec glownej petli grania
        scanner.close();
    }
}
