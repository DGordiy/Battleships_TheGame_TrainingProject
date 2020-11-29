package battleship;

import java.util.*;

public class Game {

    private final Scanner scanner;

    private enum Ship {
        Aircraft("Aircraft Carrier", 5),
        Battleship("Battleship", 4),
        Submarine("Submarine", 3),
        Cruiser("Cruiser", 3),
        Destroyer("Destroyer", 2);

        private final String name;
        private final int length;

        Ship(String name, int length) {
            this.name = name;
            this.length = length;
        }
    }

    private Map<Ship, List<int[]>> shipsOfPlayer1;
    private Map<Ship, List<int[]>> shipsOfPlayer2;

    private final char[][] fieldOfPlayer1;
    private final char[][] fieldOfPlayer2;

    private int currentPlayer;

    Game() {
        scanner = new Scanner(System.in);

        fieldOfPlayer1 = new char[10][10];
        fieldOfPlayer2 = new char[10][10];

        currentPlayer = 1;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                fieldOfPlayer1[i][j] = '~';
                fieldOfPlayer2[i][j] = '~';
            }
        }
    }

    public void start() {
        shipsOfPlayer1 = new HashMap<>();
        shipsOfPlayer2 = new HashMap<>();

        enterCoordinatesForCurrentPlayer();
        nextTurn();
        enterCoordinatesForCurrentPlayer();
        nextTurn();

        //
        //System.out.println("The game starts!");
        showFieldOfCurrentPlayer();

        //
        //System.out.println("Take a shot!");

        while (takeShotOfCurrentPlayer()) {
            nextTurn();
            showFieldOfCurrentPlayer();
        }

    }

    private void nextTurn() {
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();
        currentPlayer = 3 - currentPlayer;
    }

    private void showFieldOfCurrentPlayer() {
        if (currentPlayer == 1) {
            showField(fieldOfPlayer2);
            System.out.println("--------------------");
            showField(fieldOfPlayer1, false);
        } else {
            showField(fieldOfPlayer1);
            System.out.println("--------------------");
            showField(fieldOfPlayer2, false);
        }
    }

    private void showField(char[][] field, boolean fogOfWar) {
        System.out.print(" ");

        for (int i = 1; i <= 10; i++) {
            System.out.print(" " + i);
        }

        for (int i = 0; i < 10; i++) {
            System.out.print("\n" + (char)('A' + i));

            for (int j = 0; j < 10; j++) {
                System.out.print(" " + (!fogOfWar || field[i][j] != 'O' ? field[i][j] : '~'));
            }
        }

        System.out.println();
        System.out.println();
    }

    private void showField(char[][] field) {
        showField(field, true);
    }

    private void enterCoordinatesForCurrentPlayer() {
        System.out.printf("Player %d, place your ships on the game field\n", currentPlayer);

        char[][] field = currentPlayer == 1 ? fieldOfPlayer1 : fieldOfPlayer2;
        showField(field);

        for (Ship ship : Ship.values()) {
            System.out.printf("Enter the coordinates of the %s (%d cells):\n", ship.name, ship.length);
            enterCoordinatesOfTheShip(ship);

            showField(field, false);
        }
    }

    private void enterCoordinatesOfTheShip(Ship ship) {
        String[] coordinates = scanner.nextLine().toUpperCase().trim().split("\\s+");

        int c11 = coordinates[0].charAt(0) - 'A';
        int c12 = Integer.parseInt(coordinates[0].substring(1)) - 1;

        int c21 = coordinates[1].charAt(0) - 'A';
        int c22 = Integer.parseInt(coordinates[1].substring(1)) - 1;

        if (!putShip(ship, c11, c12, c21, c22)) {
            enterCoordinatesOfTheShip(ship);
        }
    }

    private boolean putShip(Ship ship, int c11, int c12, int c21, int c22) {

        if (c11 == c21 && Math.abs(c12 - c22) != ship.length - 1
                || c12 == c22 && Math.abs(c11 - c21) != ship.length - 1) {
            showError("Wrong length of the " + ship.name + "!");
            return false;
        }

        if (c11 != c21 && c12 != c22
            || !isCorrectCoordinate(c11, c12)
                || !isCorrectCoordinate(c21, c22)) {
            showError("Wrong ship location!");
            return false;
        }

        boolean canPut = true;
        char[][] field = currentPlayer == 1 ? fieldOfPlayer1 : fieldOfPlayer2;
        Map<Ship, List<int[]>> ships = currentPlayer == 1 ? shipsOfPlayer1 : shipsOfPlayer2;

        if (c11 == c21) {
            int c = Math.min(c12, c22);

            for (int i = 0; i < ship.length; i++) {
                if (!canPutToCell(field, c11, c + i)) {
                    canPut = false;
                    break;
                }
            }
            if (canPut) {
                List<int[]> shipCoords = new ArrayList<>();

                for (int i = 0; i < ship.length; i++) {
                    field[c11][c + i] = 'O';
                    shipCoords.add(new int[] {c11, c + i});
                }

                ships.put(ship, shipCoords);
            } else {
                showError("You placed it too close to another one.");
                return false;
            }
        } else {
            int c = Math.min(c11, c21);

            for (int i = 0; i < ship.length; i++) {
                if (!canPutToCell(field, c + i, c12)) {
                    canPut = false;
                    break;
                }
            }
            if (canPut) {
                List<int[]> shipCoords = new ArrayList<>();

                for (int i = 0; i < ship.length; i++) {
                    field[c + i][c12] = 'O';
                    shipCoords.add(new int[] {c + i, c12});
                }

                ships.put(ship, shipCoords);
            } else {
                showError("You placed it too close to another one.");
                return false;
            }
        }

        return true;
    }

    private boolean canPutToCell(char[][] field, int i, int j) {
        for (int k = (i == 0 ? 0 : -1); k <= (i == 9 ? 0 : 1); k++) {
            for (int l = (j == 0 ? 0 : -1); l <= (j == 9 ? 0 : 1); l++) {
                if (field[i + k][j + l] != '~') {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isCorrectCoordinate(int i, int j) {
        return i >= 0 && i < 10 && j >= 0 && j < 10;
    }

    private boolean takeShotOfCurrentPlayer() {
        System.out.printf("Player %d, it's your turn:\n", currentPlayer);
        return takeShot();
    }

    private boolean takeShot() {
        String coordinate = scanner.nextLine().trim().toUpperCase();
        int i = coordinate.charAt(0) - 'A';
        int j = Integer.parseInt(coordinate.substring(1)) - 1;

        char[][] field = currentPlayer == 1 ? fieldOfPlayer2 : fieldOfPlayer1;
        Map<Ship, List<int[]>> shipsOfPlayer = currentPlayer == 1 ? shipsOfPlayer2 : shipsOfPlayer1;

        if (isCorrectCoordinate(i, j)) {
            if (field[i][j] == 'O' || field[i][j] == 'X') {
                field[i][j] = 'X';
                //showField();

                //End of game
                Iterator iterator = shipsOfPlayer.keySet().iterator();
                while (iterator.hasNext()) {
                    List listOfCoords = shipsOfPlayer.get(iterator.next());
                    for (int k = 0; k < listOfCoords.size(); k++) {
                        int[] c = (int[]) listOfCoords.get(k);
                        if (c[0] == i && c[1] == j) {
                            if (listOfCoords.size() == 1) {
                                if (shipsOfPlayer1.size() == 1) {
                                    System.out.println("You sank the last ship. You won. Congratulations!");
                                    return false;
                                } else {
                                    System.out.println("You sank a ship! Specify a new target:");
                                }

                                iterator.remove();
                                return true;
                            } else {
                                listOfCoords.remove(k);
                            }
                        }
                    }
                }

                System.out.println("You hit a ship! Try again:");
            } else {
                field[i][j] = 'M';
                //showField();
                System.out.println("You missed. Try again:");
            }

            return true;
        } else {
            showError("You entered the wrong coordinates!");
            return takeShot();
        }
    }

    private void showError(String text) {
        System.out.printf("Error! %s Try again:", text);
    }
}
