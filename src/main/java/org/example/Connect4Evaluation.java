package org.example;

import sac.State;
import sac.StateFunction;

public class Connect4Evaluation extends StateFunction {

    // Wagi punktowe
    private static final int WIN_SCORE = 1000000; // Wygrana (4 w rzędzie)
    private static final int TOUCH_TOP_WIN = 900000; // Dotknięcie sufitu (specjalna zasada)
    private static final int THREE_IN_ROW = 100;  // 3 w rzędzie
    private static final int TWO_IN_ROW = 5;      // 2 w rzędzie

    @Override
    public double calculate(State state) {
        Connect4 c4 = (Connect4) state;
        byte[][] board = c4.getBoard();

        // Jeśli stan jest już wygrywający wg zasad gry (4 w rzędzie), SaC to obsłuży w checkWin,
        // ale heurystyka też powinna to odzwierciedlać.
        if (c4.checkWin()) {
            return c4.isMaximizingTurnNow() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }

        // Obliczamy wynik dla Gracza X (1) i odejmujemy wynik Gracza O (-1)
        // Dzięki temu: wysoki wynik = dobrze dla X, niski wynik = dobrze dla O.
        double scoreX = evaluateForPlayer(board, (byte) 1);
        double scoreO = evaluateForPlayer(board, (byte) -1);

        return scoreX - scoreO;
    }

    // Główna funkcja sumująca punkty dla konkretnego gracza (piece)
    private int evaluateForPlayer(byte[][] board, byte piece) {
        int score = 0;

        // 1. Promowanie grania środkiem (Center Control)
        score += scoreCenterColumn(board, piece);

        // 2. Sprawdzanie specjalnego warunku "dotknięcie najwyższego poziomu" (Rząd 0)
        score += scoreTopLevelReached(board, piece);

        // 3. Sprawdzanie linii (Poziom, Pion, Skosy)
        score += scoreHorizontal(board, piece);
        score += scoreVertical(board, piece);
        score += scoreDiagonal(board, piece);

        return score;
    }

    // --- FUNKCJE SZCZEGÓŁOWE ---

    // Zasada: Im więcej żetonów w środkowej kolumnie, tym lepiej (więcej możliwości łączenia)
    private int scoreCenterColumn(byte[][] board, byte piece) {
        int score = 0;
        int centerCol = Connect4.N / 2; // Kolumna 3
        int count = 0;

        for (int i = 0; i < Connect4.M; i++) {
            if (board[i][centerCol] == piece) {
                count++;
            }
        }
        // Mnożymy liczbę żetonów w środku przez np. 3 punkty
        score += count * 3;
        return score;
    }

    // Zasada: Jeśli dotknie najwyższego poziomu (indeks 0), wygrywa (traktujemy jak potężną nagrodę)
    private int scoreTopLevelReached(byte[][] board, byte piece) {
        for (int j = 0; j < Connect4.N; j++) {
            if (board[0][j] == piece) {
                return TOUCH_TOP_WIN;
            }
        }
        return 0;
    }

    // Sprawdzanie poziome (Horizontal)
    private int scoreHorizontal(byte[][] board, byte piece) {
        int score = 0;
        // Przechodzimy przez każdy rząd
        for (int r = 0; r < Connect4.M; r++) {
            // Przechodzimy przez kolumny, tworząc "okna" o długości 4
            for (int c = 0; c < Connect4.N - 3; c++) {
                score += evaluateWindow(
                        board[r][c], board[r][c+1], board[r][c+2], board[r][c+3],
                        piece
                );
            }
        }
        return score;
    }

    // Sprawdzanie pionowe (Vertical)
    private int scoreVertical(byte[][] board, byte piece) {
        int score = 0;
        // Przechodzimy przez każdą kolumnę
        for (int c = 0; c < Connect4.N; c++) {
            // Przechodzimy przez rzędy (do M-3, bo okno ma 4)
            for (int r = 0; r < Connect4.M - 3; r++) {
                score += evaluateWindow(
                        board[r][c], board[r+1][c], board[r+2][c], board[r+3][c],
                        piece
                );
            }
        }
        return score;
    }

    // Sprawdzanie skosów (Diagonal)
    private int scoreDiagonal(byte[][] board, byte piece) {
        int score = 0;

        // Skos w górę (Positive slope /)
        for (int r = 3; r < Connect4.M; r++) {
            for (int c = 0; c < Connect4.N - 3; c++) {
                score += evaluateWindow(
                        board[r][c], board[r-1][c+1], board[r-2][c+2], board[r-3][c+3],
                        piece
                );
            }
        }

        // Skos w dół (Negative slope \)
        for (int r = 0; r < Connect4.M - 3; r++) {
            for (int c = 0; c < Connect4.N - 3; c++) {
                score += evaluateWindow(
                        board[r][c], board[r+1][c+1], board[r+2][c+2], board[r+3][c+3],
                        piece
                );
            }
        }
        return score;
    }

    // --- OCENA OKNA (4 pól obok siebie) ---
    // To tutaj decydujemy ile punktów dać za 2, 3 lub 4 elementy
    private int evaluateWindow(byte p1, byte p2, byte p3, byte p4, byte piece) {
        int score = 0;
        byte opponent = (byte) (piece == 1 ? -1 : 1);
        byte empty = 0;

        int pieceCount = 0;
        int emptyCount = 0;
        int opponentCount = 0;

        byte[] window = {p1, p2, p3, p4};
        for (byte cell : window) {
            if (cell == piece) pieceCount++;
            else if (cell == empty) emptyCount++;
            else if (cell == opponent) opponentCount++;
        }

        // Punktacja tylko jeśli w oknie nie ma przeciwnika (bo inaczej linia jest zablokowana)
        if (opponentCount == 0) {
            if (pieceCount == 4) {
                score += WIN_SCORE;
            } else if (pieceCount == 3 && emptyCount == 1) {
                score += THREE_IN_ROW;
            } else if (pieceCount == 2 && emptyCount == 2) {
                score += TWO_IN_ROW;
            }
        }

        return score;
    }
}