import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Piece {

}

class BoardObservable {
    
}

class BoardObserver {

}

class Battle {

}

class Standby {

}

class BoardFrame extends JFrame {

    public BoardFrame(){
	this.seTitle("Quarto");
	this.setSize(300,200);
	BoardObservable b = new BoardObservable();
	public JFrame BattleFrame = new JFrame(); 
	public JFrame SubFrame = new JFrame();
	public JFrame WaitFrame = new JFrame();
	this.setLayout(new GridLayout(1,2));
	this.add(BattleFrame);
	this.add(SubFrame);
	BattleFrame.setLayout(new GridLayout(4,4)); 
	for(int i = 0; i < 16; i++){
	    BattleFrame.add(new Battle(b, i));
	}
	SubFrame.setLayout(new GridLayout(2,1));
	SubFrame.add(WaitFrame);
	SubFrame.add(new BoardObserver(b));
	WaitFrame.setLayout(new GridLayout(4,4));
	for(int j = 0; j < 16; j++){
	    WaitFrame.add(new Standby(b, j));
	}
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.pack();
	this.setVisible(true);
    }

    public static void main(String argv[]) {
	new BoardFrame();
    }
}
