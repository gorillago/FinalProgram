package link.riley.csc254.gameboard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import link.riley.csc254.gameentities.Entity;
import link.riley.csc254.gameentities.Human;
import link.riley.csc254.gameentities.Mobile;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameBoard extends Application {
    private static final int BOARD_SIZE = 900;
    private static final int maximumCellSlots = 3;
    private static final int rows = 3;
    private static final int columns = 3;
    public TextArea textArea = null;
    private Cell[][] board;


    private double rowSize = BOARD_SIZE/rows;
    private double columnSize = BOARD_SIZE/columns;
    private double slotHeight = columnSize/maximumCellSlots;

    public void launchGame(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        stage.setScene(scene);
        stage.show();
    }

    private Pane createContent() {
        board = new Cell[rows][columns];
        Pane root = new Pane();

        root.setPrefSize(BOARD_SIZE+600, BOARD_SIZE);

        Button roundButton = new Button("Round");
        roundButton.setOnAction(e -> {
            round();
        });

        roundButton.setTranslateX(900);
        roundButton.setTranslateY(0);
        roundButton.setPrefWidth(598);
        roundButton.setPrefHeight(49);
        roundButton.setFont(Font.font(30));

        textArea = new TextArea();
        textArea.setTranslateX(900);
        textArea.setTranslateY(70);
        textArea.setPrefWidth(598);
        textArea.setPrefHeight(400);
        textArea.setEditable(false);

        root.getChildren().addAll(roundButton, textArea);

        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                Cell cell = new Cell(x, y);
                board[x][y] = cell;
                root.getChildren().add(cell);
            }
        }
       // Slot slot1 = new Slot(new Entity());

        board[0][0].add(new Slot(new Human()));
        board[0][0].add(new Slot(new Human()));
        board[0][0].add(new Slot(new Human()));
        return root;
    }

    public void round() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < columns; j++)
                board[i][j].round(i, j);
    }

    public class Cell extends Pane {
        private int x, y;
        CopyOnWriteArrayList<Slot> slots;

        private Rectangle border;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
            slots = new CopyOnWriteArrayList<>();

            border = new Rectangle(rowSize-2, columnSize-2);
            border.setStroke(Color.GREEN);
            setMaxSize(rowSize, columnSize);
            setTranslateX(x*rowSize);
            setTranslateY(y*columnSize);
            getChildren().add(border);
        }

        public boolean add(Slot slot) {

            boolean result = !isFull();
            if (result) {
                slots.add(slot);
                double slotNumber = slots.indexOf(slot);
                slot.setTranslateX(0);
                slot.setTranslateY(slotHeight*slotNumber);
                Text text = slot.getText();
                text.setTranslateX(1);
                text.setTranslateY((slotHeight*slotNumber)+20);
                getChildren().addAll(slot, text);
            }
            return result;
        }

        public void updateSlots() {
            if (slots.size() > 0) {
                for (Slot slot : slots) {
                    double slotNumber = slots.indexOf(slot);
                    slot.setTranslateY(slotHeight*slotNumber);
                    slot.getText().setTranslateY((slotHeight*slotNumber)+20);
                }
            }
        }

        public boolean isFull() {
            return (slots.size() >= maximumCellSlots);
        }

        public void remove(Slot slot) {
            slots.remove(slot);
            getChildren().removeAll(slot, slot.getText());
            updateSlots();
        }

        void round(int startRow, int startColumn) {
            for (Slot slot : slots) {
                boolean dead = false;

                //Attack management;
                if (slots.size() > 1 && !slot.equals(slots.get(1))) {
                    Slot slot1 = slot;
                    Slot slot2 = slots.get(1);
                    textArea.appendText(String.format("%s attacks %s\n", slot1.getEntity(), slot2.getEntity()));
                    Attack combat = new Attack();
                    String currentMessage = "";
                    if (slot1.getEntity().getHealth() > .0) {
                        combat.attack(slot1.getEntity(), slot2.getEntity());
                        slot2.getEntity().subtractHealth(combat.damage);
                        currentMessage = "\t" + combat.getMessage();
                        slot.updateText();
                    }
                    if (slot2.getEntity().getHealth() < .10) {
                        currentMessage += " and is killed";
                        board[startRow][startColumn].remove(slot2);
                    }
                    //System.out.println(currentMessage);
                    textArea.appendText(currentMessage+"\n");
                }


            }
            for (Slot slot : slots) {

                //Now try to move the slot if it is mobile.
                move(slot, startRow, startColumn);
            }
        }

        void move(Slot slot, int startRow, int startColumn) {
            Random r = new Random();
            Entity entity = slot.getEntity();
            int distance = 0;

            if (entity instanceof Mobile) {
                distance = ((Mobile) entity).getRange();
                if (distance > 0) {
                    int direction = r.nextInt(9);
                    int deltax = 0;
                    int deltay = 0;
                    switch (direction) {
                        case 0: //no movement
                            break;
                        case 1: //south
                            deltay = (int) Math.round(distance * Math.random());
                            break;
                        case 2: //north
                            deltay = -(int) Math.round(distance * Math.random());
                            break;
                        case 3: //east
                            deltax = (int) Math.round(distance * Math.random());
                            break;
                        case 4: //west
                            deltax = -(int) Math.round(distance * Math.random());
                            break;
                        case 5: //southeast
                            deltax = (int) Math.round(distance * Math.random());
                            deltay = (int) Math.round(distance * Math.random());
                            break;
                        case 6: //southwest
                            deltax = -(int) Math.round(distance * Math.random());
                            deltay = (int) Math.round(distance * Math.random());
                            break;
                        case 7: //northheast
                            deltax = (int) Math.round(distance * Math.random());
                            deltay = -(int) Math.round(distance * Math.random());
                            break;
                        case 8: //northwest
                            deltax = -(int) Math.round(distance * Math.random());
                            deltay = -(int) Math.round(distance * Math.random());
                            break;

                    }
                    int newRow = (startRow + deltay) % rows;
                    newRow = (newRow < 0) ? rows + newRow : newRow;
                    int newColumn = (startColumn + deltax) % columns;
                    newColumn = (newColumn < 0) ? columns + newColumn : newColumn;
                    //System.out.printf("Trying to move %s from (%d, %d) to (%d,%d)\n", entity,startRow,startColumn,newRow,newColumn);
                    if (!board[newRow][newColumn].isFull()) {
                        board[startRow][startColumn].remove(slot);
                        board[newRow][newColumn].add(slot);
                    }
                }
            }

        }
    }
    final class Attack {
        String message;
        double damage;

        protected void attack(Entity a, Entity b) {
            message = a.getSymbol() + " ";

            //Is there an attack?
            boolean attackHappens = (Math.random() < a.getAggressiveness());
            if (attackHappens) {
                message += a.getAttackMessage() + " at " + b.getSymbol();
                damage = Math.random() * 0.5 * a.getStrength();
                message += String.format(" and does %1.2f damage", damage);
            } else {
                message += a.getPassiveMessage();
                damage = 0;
            }
        }

        String getMessage() {
            return message;
        }
    }

    public class Slot extends Rectangle{
        private Entity entity;
        private Text text;

        public Slot(Entity entity) {
            setWidth(rowSize-2);
            setHeight(slotHeight-2);
            setFill(Color.WHITE);
            this.entity = entity;
            text = new Text();
            text.setFont(Font.font(20));
            text.setText(entity.toString());
        }

        public Text getText() {
            return text;
        }
        public void updateText() {
            text.setText(entity.toString());
        }
        public Entity getEntity() {
            return entity;
        }
    }
}
