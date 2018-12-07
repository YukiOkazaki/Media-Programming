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

class BoardObservable extends Observable { 
    int b[] = new int[16];//board
    for(int i = 0;i<16;i++){
	b[i] = 0;
    }
    int koma[] = new int[16];
    koma[0] = 1;
    koma[1] = 2;
    koma[2] = 3;
    koma[3] = 5;
    koma[4] = 6;
    koma[5] = 7;
    koma[6] = 10;
    koma[7] = 14;
    koma[8] = 15;
    koma[9] = 21;
    koma[10] = 30;
    koma[11] = 35;
    koma[12] = 42;
    koma[13] = 70;
    koma[14] = 105;
    koma[15] = 210;
    int sp = 0; //select position 
    public void set_piece(int s,int p){ //sum,position
	b[p] = s;
	sp = 0;
	setChanged();
	notifyObservers();
    }  
    
    public void get_piece(int p){
	return b[p];
    }
    public void get_lineval(int p1,p2,p3,p4){
	int n1 = get_piece(p1);
	int n2 = get_piece(p2); 
	int n3 = get_piece(p3);
	int n4 = get_piece(p4);
	int mul;
	mul = n1*n2*n3*n4; /*b[p1]*b[p2]*b[p3]*b[p4];*/
	return mul;
    }
    public void is_complete(){
	if(get_lineval(0,4,8,12)%16 == 0 ||get_lineval(0,4,8,12)%2 !=0){
	} 
	
	get_lineval(1,5,9,13);
	get_lineval(2,6,10,14);
	get_lineval(3,7,11,15);
	get_lineval(0,1,2,3);
	get_lineval(4,5,6,7);
	get_lineval(8,9,10,11);
	get_lineval(12,13,14,15);
	get_lineval(0,5,10,15);
	get_lineval(3,6,9,12);
	
    }
    public void set_selectpiece(int s,int p){
	sp = s;
	koma[p] = 0;
	setChanged();
	notifyObservers();
    }
    public void get_selectpiece(){
	return sp;
    }
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

class BoardFrame extends JFrame{
    
    public BoardFrame(){
	this.seTitle("Quarto");
	this.setSize(300,200);
	BoardObservable b = new BoardObservable();
	public JFrame BattleFrame = new JFrame(); 
	public JFrame SubFrame = new JFrame();
	public JFrame WaitFrame = new JFrame();
	this.setLayout(new GridLayout(1,2));         //画面をBattleFrame,SubFrame用に２分割
	this.add(BattleFrame);
	this.add(SubFrame);
	BattleFrame.setLayout(new GridLayout(4,4));  //BattleFrame内を１６分割
	for(int i = 0; i < 16; i++){
	    BattleFrame.add(new Battle(b, i));
	}
	SubFrame.setLayout(new GridLayout(2,1));     //SubFrame内をWaitFrame,BoardObserver用に２分割
	SubFrame.add(WaitFrame);
	SubFrame.add(new BoardObserver(b));
	WaitFrame.setLayout(new GridLayout(4,4));    //WaitFrame内を１６分割
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

