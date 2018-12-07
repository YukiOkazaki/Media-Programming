import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Piece {
    private int height, color, shape, hole;
    public Piece(int height, int color, int shape, int hole){
	this.height = height;
	this.color = color;
	this.shape = shape;
	this.hole = hole;
    }
    
    public int get_val(){
	return height * color * shape * hole;
    }
}

class BoardObservable {
    
}

class BoardObserver extends JPanel implements Obserever {
    protected BoardObservable BO;
    protected JLabel label;
    protected int val;
    public BoardObserver(BoardObservable observable){
	BO = observable;
	BO.addObserver(this);
	label = new JLabel();
	this.add(label);
    }
    
    public void update(Observable o, Object arg){
	val = BO.get_selectpiece();
	label.setText(String.valueOf(val));
    }

}

class Battle extends BoardObserver implements MouseListener {   //BattleはBoardObserverを継承
    protected int place;
    public Battle(BoardObservable observable, int place){
	super(observable);
	this.place = place;
	this.addMouseListener(this);
    }

    @Override                                                //updateをOverrideでBattle用に変更
    public void update(Observable o, Object arg){
        this.val = this.BO.get_piece(place);
	this.label.setText(String.valueOf(this.val));
    }
    
    public void mouseClicked(MouseEvent e){
	this.val = get_selectpiece();
	this.BO.set_piece(this.val, place);
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e){ }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e)  { }
    
}

class Standby extends Battle {                                  //StandbyはBattleを継承
    public Standby(BoardObservable Observable, int place){
	super(observable);
	this.place = place;
	this.addMouseListener(this);
    }
    
    @Override                                                //updateをOverrideでStandby用に変更
    public void update(Observable o, Object arg){
	this.val = get_piece(this.place);
	this.label.setText(String.valueOf(this.val));
    }
   
    @Override                                                //mouseClickedもStandby用に変更
    public void mouseClicked(MouseEvent e){
	this.val = get_piece(this.place);
	this.BO.set_piece(this.val, this.place);
    }
    
}

class BoardFrame {

}
