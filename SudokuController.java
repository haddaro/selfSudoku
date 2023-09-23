import java.util.LinkedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


// The controller code for a JavaFx app that lets you build your own Sudoku game and then play it
public class SudokuController {
	
	private final int SIZE = 3;
	private final int BLOCK = SIZE*SIZE;
	private final int MIN = 1;
	private final int MAX = 9;
	private final int BIGFONT = 20;
	
	private TextField fields[] = new TextField[BLOCK*BLOCK]; // All the TextFields
	private TextField empty = new TextField();
	//The linked lists in the following arrays are declared here and initialized later
	private LinkedList<TextField>[] blocks = new LinkedList[BLOCK]; // Array of SIZE linked lists, if two pointers are on the same list, the fields they point to belong to the same block
	private LinkedList<TextField>[] rows = new LinkedList[BLOCK]; // same as above for rows
	private LinkedList<TextField>[] columns = new LinkedList[BLOCK]; //same as above for columns
	
    @FXML
    private GridPane grid;
    
    public void initialize() {
    	
    	initLists(); 
    	
    	double fieldSize = (grid.getPrefHeight() / BLOCK); // The grid was made square in SceneBuilder
    	
    	//create TextFields:
    	for (int i=0; i < BLOCK * BLOCK; i++) {
    		int block, row, column;
    		TextField currentField;
    		fields[i] = new TextField();
    		currentField = fields[i]; // We need this pointer to send it to onAction()
    		fields[i].setPrefSize(fieldSize, fieldSize);
    		
    		improveLooks(fields[i]); 
    		
    		
    		//Calculate row, column and block and put three pointers to it in the relevant linked list in each array: blocks, rows, columns:
    		row = i / BLOCK;
    		column = i % BLOCK;
    		block = (row / SIZE) * SIZE + (column / SIZE);
    		addToArrays(fields[i], row, column, block);
    		
    		//Differentiate between the blocks:
    		if (block % 2 == 0)
    			colorTile(fields[i]);
    			
    		//Add every textField to the grid:
    		grid.add(fields[i], i % BLOCK, i / BLOCK);
    		
    		//Set a function call when the user hits 'Enter':
    		fields[i].setOnAction(new EventHandler<ActionEvent>() { //Call an anonymous class that inherits from EventHandler, to override its handle()
    		    @Override
    		    public void handle(ActionEvent event) {
    		    	checkInput(row, column, block, currentField);
    		    }
    		});
    	}//end of for
    	
    	showInstructions();
    	
    }//end of initialize
    
    //Starts over
    @FXML
    void clearPressed(ActionEvent event) {
    	for (int i = 0; i < (BLOCK*BLOCK); i++) {
    		fields[i].clear();
    		fields[i].setEditable(true);
    	}
    	for (int i = 0; i < BLOCK; i++) { // clears all lists of pointer (a simple clear() for the list did not do the trick)
    		for (TextField tf : blocks[i])
    			tf = empty;
    		for (TextField tf : rows[i])
    			tf = empty;
    		for (TextField tf : columns[i])
    			tf = empty;
    	}
    }

    //Sets the board fixed so we can start playing
    @FXML
    void setPressed(ActionEvent event) {
    	for (TextField tf : fields) {
    		if (!tf.getText().isEmpty()) {
    			tf.setStyle("-fx-text-inner-color: blue;");
    			tf.setEditable(false);
    		}
    	}
    	wishGoodLuck();
    }
    //Puts a pointer to every TextField in a linked list for its column, another linked list for its row, and anothe linked list for its block
    private void addToArrays(TextField tf, int row, int column, int block){
    	rows[row].add(tf);
    	columns[column].add(tf);
    	blocks[block].add(tf);
    }
    
    //Checks if an input is correct
    private void checkInput(int row, int column, int block, TextField tf) {
    	String s = tf.getText();
    	if (s.isEmpty()) //If the user just deleted the input and hit 'enter' we don't want to check the empty String
    		return;
    	int num;
    	try {
    		num = Integer.parseInt(s);
    	}
    	catch (NumberFormatException e) {
    		num = 0;
    	}
    	if ((num < 1 || num > 9)) { 
    		Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid input");
			alert.setContentText("Enter a digit between " + MIN + " and " + MAX);
			alert.showAndWait();
			tf.clear(); // we don't want this invalid input anymore
    	}
    	else { //check if it fits
    		if (isInListTwice(rows[row], num) || isInListTwice(columns[column], num) || isInListTwice(blocks[block], num)) {
    			Alert alert = new Alert(AlertType.ERROR);
    			alert.setTitle("Error");
    			alert.setHeaderText("Invalid input");
    			alert.setContentText(num + " cannot be entered here");
    			alert.showAndWait();
    			tf.clear(); // we don't want this wrong input anymore
    		}
    		else if (isFinished()) { //check if the player has completed the board
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Yay!");
    			alert.setHeaderText("You've completed the Sudoku");
    			alert.setContentText("Press 'Clear' to play again");
    			alert.showAndWait();
    		}
 
    	}
    	
    }//End of checkInput
    
    //Helps with checking if the input adhered to the rules of the game
    private boolean isInListTwice (LinkedList<TextField> textFieldList, int num) {
    	//the number num is 100% in the list because we put it there, but we want to check if it's there twice
    	int i = 0;
    	try {
	    	for (TextField current : textFieldList) {
	    		try {
	    			if (Integer.parseInt(current.getText()) == num) {
	    				i++;
	    			}
	    		}
	    		catch(NumberFormatException e) {
	    			//do nothing and move on iterating the list
	    			//it is here because some TextField may contain an empty string as text after clear()
	    		}
	    	}
    	}
		catch (NullPointerException e) {
			//again do nothing.

    	}
    	boolean result = (i == 1) ? false : true;
    	return result;
    }
    
    //Checks if the player has completed the game
    private boolean isFinished() {
    	for (int i=0; i < BLOCK*BLOCK; i++) {
    		if (fields[i].getText().isEmpty())
    			return false;
    	}
    	return true;
    }
    
    //explains how the game works
    private void showInstructions() {
    	Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Instructions");
		alert.setHeaderText("Build your Sudoku board");
		alert.setContentText("Enter numbers, and press 'set' to play");
		alert.showAndWait();
    }
    //Centers the text and enlarges the font
    private void improveLooks(TextField tf) {
    	tf.setAlignment(Pos.CENTER);
		Font font = new Font("System Regular", BIGFONT);
		tf.setFont(font);
    }
    
    //Colors a TextField
    private void colorTile(TextField tf) {
    	BackgroundFill bf = new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY);
    	Background background = new Background(bf);
    	tf.setBackground(background);
		tf.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }
    
    private void initLists() {
    	for (int i = 0; i < BLOCK; i++) { // initialize all lists
    	    blocks[i] = new LinkedList<TextField>();
    	    rows[i] = new LinkedList<TextField>();
    	    columns[i] = new LinkedList<TextField>();
    	}
    }
    //Pops a msg when the board is set for playing
    private void wishGoodLuck() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Board is set");
		alert.setHeaderText("Start Playing. To start over, press 'Clear'");
		alert.setContentText("Good luck");
		alert.showAndWait();
    }
    
}//End of Controller

