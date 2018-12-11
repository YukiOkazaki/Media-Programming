import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class BoardObservable extends Observable { 
    private int b[] = new int[16];  //board
    private int koma[] = {1,2,3,5,6,7,10,14,15,21,30,35,42,70,105,210};
    private int sp; //select position 
    private int playernum;
    private int situation; //0が選択画面、1が盤面に置く
    public void initialize_board(){
	sp = 0;
	playernum = 2;
	setChanged();
	notifyObservers();
    }

    public void set_piece(int s,int p){ //sum,position
	b[p] = s;
	sp = 0;
	setChanged();
	notifyObservers();
    }  
    public void set_selectpiece(int s,int p){
	sp = s;
	koma[p] = 0;
	setChanged();
	notifyObservers();
    }    
    public void set_standbypiece(int s, int p){
	koma[p] = s;
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
    public int is_complete(){
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
	return 1;                            //ためしに1にしてるだけ
	
    }

    public int get_selectpiece(){
	return sp;
    }
    public int get_playernum(){
	return playernum;
    }
    public void set_playernum(int num){
	playernum = num;
    }
    public int get_situation(){
	return situation;
    }
    public void set_situation(int num){
	situation = num;
    }

}

class BoardObserver extends JPanel implements Observer {
    protected BoardObservable BO;
    protected JLabel label;
    protected int val;
    protected int playernum;
    protected int situation;
    public BoardObserver(BoardObservable observable){
	BO = observable;
	BO.addObserver(this);
	label = new JLabel();
	this.setLayout(new BorderLayout());
	this.add(label, BorderLayout.CENTER);
	label.setHorizontalAlignment(JLabel.CENTER);
	label.setVerticalAlignment(JLabel.CENTER);
	label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,18)); 
    }
    
    public void update(Observable o, Object arg){}
    
}

class Select extends BoardObserver {
    private JLabel playerlabel;
    public Select(BoardObservable observable){
	super(observable);
	playerlabel = new JLabel();
	this.add(playerlabel, BorderLayout.SOUTH);
	val = BO.get_selectpiece();
	label.setText(String.valueOf(val));
	playerlabel.setHorizontalAlignment(JLabel.CENTER);
	playerlabel.setText("");
	playerlabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,18));
	BO.initialize_board();
    }
    
    @Override
    public void update(Observable o, Object arg){
	val = BO.get_selectpiece();
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*1.0),(int)(icon.getIconHeight()*1.0),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	    label.setText(String.valueOf(val));
	}
	playernum = BO.get_playernum();
	situation = BO.get_situation();
	if(situation == 0)
	    playerlabel.setText("<html>"+playernum+"Pが相手の駒を選んでください<br>そろっていればQuarto!と宣言してください</html>");
	if(situation == 1)
	    playerlabel.setText("<html>"+playernum+"Pが駒を盤面に置いてください<br>または"+(playernum %2 +1)+"Pが相手の駒を選びなおしてください</html>");
    }
    

}

class Battle extends BoardObserver implements MouseListener {   //BattleはBoardObserverを継承
    private int place;
    public Battle(BoardObservable observable, int place){
	super(observable);
	this.place = place;
	this.addMouseListener(this);
        val = BO.get_piece(place);
	label.setText(String.valueOf(val));	
    }
    
    @Override                                                //updateをOverrideでBattle用に変更
    public void update(Observable o, Object arg){
        val = BO.get_piece(place);
	label.setText(String.valueOf(val));
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.7),(int)(icon.getIconHeight()*0.7),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	    label.setText(String.valueOf(val));
	}
    }
    
    public void mouseClicked(MouseEvent e){
	val = BO.get_selectpiece();
	if(val == 0)
	    return;
	if(BO.get_piece(place) != 0)
	    return;
	BO.set_situation(0);
	BO.set_piece(val, place);
	setBackground(null);
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

class Standby extends BoardObserver implements MouseListener {                                  //StandbyはBoardObserverを継承
    private int tmp;
    private int place;
    public Standby(BoardObservable observable, int place){
	super(observable);
	this.place = place;
	this.addMouseListener(this);
	val = BO.get_standbypiece(place);
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.4),(int)(icon.getIconHeight()*0.4),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	    label.setText(String.valueOf(val));
	}
    }
    
    @Override                                                                                    //updateをOverrideでStandby用に変更
    public void update(Observable o, Object arg){
	val = BO.get_standbypiece(place);
	if(val != 0){
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.4),(int)(icon.getIconHeight()*0.4),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	    label.setText(String.valueOf(val));
	}
    }
    
    public void mouseClicked(MouseEvent e){
	val = BO.get_standbypiece(place);
	tmp = BO.get_selectpiece();
	if(val == 0)
	    return;
	if(tmp == 0)
	    BO.set_playernum(BO.get_playernum()%2 + 1);
	BO.set_situation(1);
	BO.set_selectpiece(val, place);
	if(tmp != 0){
	    BO.set_standbypiece(tmp, place);
	}
	setBackground(null);
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

class CompleteButton extends BoardObserver implements ActionListener {
    private JButton complete;
    public CompleteButton(BoardObservable observable){
	super(observable);
	complete = new JButton("Quarto!");
	this.setLayout(new GridLayout(2,1));
	this.add(complete, BorderLayout.EAST);
	
	//	label.setVerticalAlignment(JLabel.N);
	complete.addActionListener(this);
	BO.initialize_board();
    }
    public void actionPerformed(ActionEvent e) {
	situation = BO.get_situation();
	playernum = BO.get_playernum();
	if(situation == 0){
	    if(BO.is_complete() == 1){
		label.setText("<html>揃っています<br>"+playernum+"Pの勝ちです</html>");
	    } else{
		label.setText("揃っていません");
	    }
	}
    }
}

class BoardFrame extends JFrame {
    
    public JPanel BattlePanel, SubPanel, WaitPanel, scPanel;
    public BoardObservable b;
    public BoardFrame(){
	b = new BoardObservable();
	BattlePanel = new JPanel(); 
	SubPanel = new JPanel();
	WaitPanel = new JPanel();
	scPanel = new JPanel();
	this.setLayout(new GridLayout(1,2));         //画面をBattlePanel,SubPanel用に２分割
	BattlePanel.setLayout(new GridLayout(4,4));  //BattlePanel内を16分割
	for(int i = 0; i < 16; i++){
	    BattlePanel.add(new Battle(b, i));
	}
	SubPanel.setLayout(new GridLayout(2,1));     //SubPanel内をWaitPanel,Select用に２分割
	WaitPanel.setLayout(new GridLayout(4,4));    //WaitPanel内を16分割
	for(int j = 0; j < 16; j++){
	    WaitPanel.add(new Standby(b, j));
	}
	SubPanel.add(WaitPanel);
	scPanel.setLayout(new GridLayout(1,2));
	scPanel.add(new Select(b));
	scPanel.add(new CompleteButton(b));
	SubPanel.add(scPanel);
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

