import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class BoardObservable extends Observable { 
    private int b[] = new int[16];  //board
    private int koma[] = {1,2,3,5,6,7,10,14,15,21,30,35,42,70,105,210};
    private int sp = 0; //select position 
    public void set_piece(int s,int p){ //sum,position
	b[p] = s;
	sp = 0;
	setChanged();
	notifyObservers();
    }  
    
    public int get_piece(int p){
	return b[p];
    }
    public int get_standbypiece(int p){
	return koma[p];
    }
    public int get_lineval(int p1, int p2, int p3, int p4){
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
    public int get_selectpiece(){
	return sp;
    }
}

class BoardObserver extends JPanel implements Observer {
    protected BoardObservable BO;
    protected JLabel label;
    protected int val;
    public BoardObserver(BoardObservable observable){
	BO = observable;
	BO.addObserver(this);
	label = new JLabel();
	this.add(label);
	val = BO.get_selectpiece();
	label.setText(String.valueOf(val));
	label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,26)); 
    }
    
    public void update(Observable o, Object arg){
	val = BO.get_selectpiece();
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.3),(int)(icon.getIconHeight()*0.3),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	    label.setText(String.valueOf(val));
	}
    }
    
}

class Battle extends BoardObserver implements MouseListener {   //BattleはBoardObserverを継承
    protected int place;
    public Battle(BoardObservable observable, int place){
	super(observable);
	this.place = place;
	this.addMouseListener(this);
        this.val = this.BO.get_piece(place);
	this.label.setText(String.valueOf(this.val));	
    }
    
    @Override                                                //updateをOverrideでBattle用に変更
    public void update(Observable o, Object arg){
        this.val = this.BO.get_piece(place);
	this.label.setText(String.valueOf(this.val));
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.15),(int)(icon.getIconHeight()*0.15),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    this.label.setIcon(smallicon);
	} else {
	    this.label.setIcon(null);
	    this.label.setText(String.valueOf(this.val));
	}
    }
    
    public void mouseClicked(MouseEvent e){
	this.val = this.BO.get_selectpiece();
	if(this.val == 0)
	    return;
	if(this.BO.get_piece(place) != 0)
	    return;
	this.BO.set_piece(this.val, place);
	
	this.setBackground(null);
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e){ }
    public void mouseEntered(MouseEvent e) { 
	this.setBackground(new Color(0,0,255,50));
    }
    public void mouseExited(MouseEvent e)  {
	this.setBackground(null);
    }
    
}

class Standby extends Battle {                                  //StandbyはBattleを継承
    public Standby(BoardObservable observable, int place){
	super(observable, place);
	this.place = place;
	this.addMouseListener(this);
	this.val = this.BO.get_standbypiece(this.place);
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.075),(int)(icon.getIconHeight()*0.075),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    this.label.setIcon(smallicon);
	} else {
	    this.label.setIcon(null);
	    this.label.setText(String.valueOf(this.val));
	}
    }
    
    @Override                                                //updateをOverrideでStandby用に変更
    public void update(Observable o, Object arg){
	this.val = this.BO.get_standbypiece(this.place);
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.075),(int)(icon.getIconHeight()*0.075),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    this.label.setIcon(smallicon);
	} else {
	    this.label.setIcon(null);
	    this.label.setText(String.valueOf(this.val));
	}
    }
    
    @Override                                                //mouseClickedもStandby用に変更
    public void mouseClicked(MouseEvent e){
	System.out.println(+place+"");
	this.val = this.BO.get_standbypiece(this.place);
	System.out.println(this.val+"");
	if(BO.get_selectpiece() != 0)
	    return;
	if(this.val != 0)
	    this.BO.set_selectpiece(this.val, this.place);

	this.setBackground(null);
    }
    
}

class BoardFrame extends JFrame {
    
    public JPanel BattlePanel, SubPanel, WaitPanel;
    public BoardObservable b;
    public BoardFrame(){
	b = new BoardObservable();
	BattlePanel = new JPanel(); 
	SubPanel = new JPanel();
	WaitPanel = new JPanel();
	this.setLayout(new GridLayout(1,2));         //画面をBattlePanel,SubPanel用に２分割
	BattlePanel.setLayout(new GridLayout(4,4));  //BattlePanel内を16分割
	for(int i = 0; i < 16; i++){
	    BattlePanel.add(new Battle(b, i));
	}
	SubPanel.setLayout(new GridLayout(2,1));     //SubPanel内をWaitPanel,BoardObserver用に２分割
	WaitPanel.setLayout(new GridLayout(4,4));    //WaitPanel内を16分割
	for(int j = 0; j < 16; j++){
	    WaitPanel.add(new Standby(b, j));
	}
	SubPanel.add(WaitPanel);
	SubPanel.add(new BoardObserver(b));
	this.add(BattlePanel);
	this.add(SubPanel);
	this.setTitle("Quarto");
	this.setSize(1600,800);
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setVisible(true);
    }
    
    public static void main(String argv[]) {
	new BoardFrame();
    }

}

