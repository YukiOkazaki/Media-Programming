import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
 
class CommServer {
    private ServerSocket serverS = null;
    private Socket clientS = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private int port=0;
 
    CommServer() {}
    CommServer(int port) { open(port); }
    CommServer(CommServer cs) { serverS=cs.getServerSocket(); open(cs.getPortNo()); }
    
    ServerSocket getServerSocket() { return serverS; } 
    int getPortNo() { return port; }
 
    // サーバ用のソケット(通信路)のオープン
    // サーバ用のソケットはクライアントからの接続待ち専用．
    // ポート番号のみを指定する．
    boolean open(int port){
      this.port=port;
      try{ 
     if (serverS == null) { serverS = new ServerSocket(port); }
      } catch (IOException e) {
         System.err.println("ポートにアクセスできません。");
         System.exit(1);
      }
      try{
         clientS = serverS.accept();
         out = new PrintWriter(clientS.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(clientS.getInputStream()));
      } catch (IOException e) {
         System.err.println("Acceptに失敗しました。");
         System.exit(1);
      }
      return true;
    }
 
    // データ送信
    boolean send(String msg){
        if (out == null) { return false; }
        out.println(msg);
        return true;
    }
 
    // データ受信
    String recv(){
        String msg=null;
        if (in == null) { return null; }
        try{
          msg=in.readLine();
        } catch (SocketTimeoutException e){
	    System.err.println("タイムアウトです．");
          return null;
        } catch (IOException e) {
          System.err.println("受信に失敗しました。");
          System.exit(1);
        }
        return msg;
    }
 
    // タイムアウトの設定
    int setTimeout(int to){
        try{
          clientS.setSoTimeout(to);
        } catch (SocketException e){
          System.err.println("タイムアウト時間を変更できません．");
          System.exit(1);
        }
        return to;
    }
 
    // ソケットのクローズ (通信終了)
    void close(){
      try{
        in.close();  out.close();
        clientS.close();  serverS.close();
      } catch (IOException e) {
          System.err.println("ソケットのクローズに失敗しました。");
          System.exit(1);
      }
      in=null; out=null;
      clientS=null; serverS=null;
    }
}
 
class CommClient {
   Socket clientS = null;
   BufferedReader in = null;
   PrintWriter out = null;
 
   CommClient() {}
   CommClient(String host,int port) { open(host,port); }
 
   // クライアントソケット(通信路)のオープン　
   // 接続先のホスト名とポート番号が必要．
   boolean open(String host,int port){
     try{
       clientS = new Socket(InetAddress.getByName(host), port);
       in = new BufferedReader(new InputStreamReader(clientS.getInputStream()));
       out = new PrintWriter(clientS.getOutputStream(), true);
     } catch (UnknownHostException e) {
       System.err.println("ホストに接続できません。");
       System.exit(1);
     } catch (IOException e) {
       System.err.println("IOコネクションを得られません。");
       System.exit(1);
     }
     return true;
   }
 
    // データ送信
    boolean send(String msg){
      if (out == null) { return false; }
      out.println(msg);
      return true;
    }
 
    // データ受信
    String recv(){
        String msg=null;
        if (in == null) { return null; }
        try{
          msg=in.readLine();
        } catch (SocketTimeoutException e){
            return null;
        } catch (IOException e) {
          System.err.println("受信に失敗しました。");
          System.exit(1);
        }
        return msg;
    }
 
    // タイムアウトの設定
    int setTimeout(int to){
        try{
          clientS.setSoTimeout(to);
        } catch (SocketException e){
          System.err.println("タイムアウト時間を変更できません．");
          System.exit(1);
        }
        return to;
    }
 
    // ソケットのクローズ (通信終了)
    void close(){
      try{
        in.close();  out.close();
        clientS.close();
      } catch (IOException e) {
          System.err.println("ソケットのクローズに失敗しました。");
          System.exit(1);
      }
      in=null; out=null;
      clientS=null;
    }
}


class BoardObservable extends Observable { 
    private boolean server;
    private boolean single = false;
    private CommServer sv = null;
    private CommClient cl = null;
    private int b[] = new int[16];  //board
    private int koma[] = {1,5,7,35,2,10,14,70,3,15,21,105,6,30,42,210};
    private int sp = 0; //select position 
    private int playernum = 2;        //操作するのが何Pなのか1or2
    private int situation = 0;        //0が選択画面、1が盤面に置く 2が終了
    private int completeline;
    private int selectplace;      //どの場所からその駒を持って来たかを保存する変数
    
    public BoardObservable(boolean server, String host, int port){
	this.server = server;
	if(server){
	    System.out.println("Waiting for connection with port no: "+port);
	    sv = new CommServer(port);
	    sv.setTimeout(1);
	    System.out.println("Connected!");
	} else {
	    cl = new CommClient(host, port);
	    cl.setTimeout(1);
	    System.out.println("Connected to "+host+":"+port+"!");
	}
    }

    public BoardObservable(){
	this.server = true;
	this.single = true;
    }

   
    public void initialize_board(){
	sp = 0;
	playernum = 2;
	setChanged();
	notifyObservers();
    }
    public void finish_board(){
	situation = 2;
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
	int c[] =new int[10];
        c[0] = get_lineval(0,4,8,12);
        c[1] = get_lineval(1,5,9,13);
        c[2] = get_lineval(2,6,10,14);
        c[3] = get_lineval(3,7,11,15);
        c[4] = get_lineval(0,1,2,3);
        c[5] = get_lineval(4,5,6,7);
	c[6] = get_lineval(8,9,10,11);
	c[7] = get_lineval(12,13,14,15);
	c[8] = get_lineval(0,5,10,15);
	c[9] = get_lineval(3,6,9,12);
	for(int i=0;i<10;i++){
	    if(c[i]%16 == 0 || c[i]%2 != 0 || 
	       c[i]%81 == 0 || c[i]%3 != 0 || 
	       c[i]%625 == 0 || c[i]%5 != 0 || 
	       c[i]%2401 == 0 || c[i]%7 != 0){
		if(c[i] == 0){
		    continue;
		}else{
		    completeline = i;
		    return 1;
		}
	    }
	}
	return 0;
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
    public int get_completeline(){
	return 1;
    }
    public int is_inline(int i, int p){
	return 1;
    }

  
    public boolean isServer() { return server; }
    public boolean isSingle() { return single; }

    public void sendselect(int place){
	if(server){
	    String msg = String.format("%d %d %d %d %d", sp, playernum, situation, selectplace, place);
	    sv.send(msg);
	} else {
	    String msg = String.format("%d %d %d %d %d", sp, playernum, situation, selectplace, place);
	    cl.send(msg);
	}
    }
    public void sendbattle(int place, int val){
	if(server){
	    String msg = String.format("%d %d %d", situation, place, val);
	    sv.send(msg);
	} else { 
	    String msg = String.format("%d %d %d", situation, place, val);
	    cl.send(msg);
	}
    }
    public void sendcomplete(int situation, int playernum){
	if(server){
	    String msg = String.format("%d %d", situation, playernum);
	    sv.send(msg);
	} else { 
	    String msg = String.format("%d %d", situation, playernum);
	    cl.send(msg);
	}
    }

    
    public void recvselect(){
	if(server){
	    String msg = sv.recv();
	    if(msg == null) return;
	    String[] sm = msg.split(" ");
	    set_selectpiece(Integer.parseInt(sm[0]), Integer.parseInt(sm[4]));
	    set_playernum(Integer.parseInt(sm[1]));
	    set_situation(Integer.parseInt(sm[2]));
	} else {
	    String msg = cl.recv();
	    if(msg == null) return;
	    String[] sm = msg.split(" ");
	    set_selectpiece(Integer.parseInt(sm[0]), Integer.parseInt(sm[4]));
	    set_playernum(Integer.parseInt(sm[1]));
	    set_situation(Integer.parseInt(sm[2]));
	}
	setChanged();
	notifyObservers();
    }
    
    public void recvbattle(){
	if(server){
	    String msg = sv.recv();
	    if(msg == null) return;
	    String[] sm = msg.split(" ");
	    set_piece(Integer.parseInt(sm[2]), Integer.parseInt(sm[1]));
	    set_situation(Integer.parseInt(sm[0]));
	} else {
	    String msg = cl.recv();
	    if(msg == null) return;
	    String[] sm = msg.split(" ");
	    set_piece(Integer.parseInt(sm[2]), Integer.parseInt(sm[1]));
	    set_situation(Integer.parseInt(sm[0]));
	}
	setChanged();
	notifyObservers();
    }	    
    public void recvcomplete(){
	if(server){
	    String msg = sv.recv();
	    if(msg == null) return;
	    String[] sm = msg.split(" ");
	    set_playernum(Integer.parseInt(sm[1]));
	    set_situation(Integer.parseInt(sm[0]));
	} else {
	    String msg = cl.recv();
	    if(msg == null) return;
	    String[] sm = msg.split(" ");
	    set_playernum(Integer.parseInt(sm[1]));
	    set_situation(Integer.parseInt(sm[0]));
	}
	setChanged();
	notifyObservers();
    }
    
}

class BoardObserver extends JPanel implements Observer,ActionListener {                       //observer側のすべての親クラス
    private Timer timer;
    protected BoardObservable BO;
    protected JLabel label;
    protected int val;
    protected int playernum;
    protected int situation;
    public BoardObserver(BoardObservable observable){
	BO = observable;
	BO.addObserver(this);                                                  //observerに登録
	label = new JLabel();
	this.setLayout(new BorderLayout());                                    //panelは通常FlowLayoutなのでBorderLayoutに変更
	this.add(label, BorderLayout.CENTER);                                  //panel内に表示されるラベルの追加
	label.setHorizontalAlignment(JLabel.CENTER);                           //ラベルの位置は水平方向で中心
	label.setVerticalAlignment(JLabel.CENTER);                             //ラベルの位置は垂直方向で中心
	label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));                 //ラベルのフォントの設定
	timer = new Timer(10,this);
	timer.start();
    }
    
    public void update(Observable o, Object arg){}
    
    public void actionPerformed(ActionEvent e){
	if(!BO.isSingle()){
	    if(BO.get_situation() == 0)
		BO.recvselect();
	    if(BO.get_situation() == 1)
	    BO.recvbattle();
	}
    }

}

class Select extends BoardObserver implements ActionListener {

    private JLabel playerlabel;                                                //何Pが操作するべきなのか等の情報を表示するラベル
    private String maincolor, opponentcolor;                                   //IP,2Pそれぞれの色を指定する
    public Select(BoardObservable observable){
	super(observable);                                                     //親のコンストラクタを一度呼びだす
	playerlabel = new JLabel();
	this.add(playerlabel, BorderLayout.SOUTH);                             //playerlabelを下に配置
	val = BO.get_selectpiece();
	playerlabel.setHorizontalAlignment(JLabel.CENTER);                     //plyaerlabelの水平方向の位置は中心
	playerlabel.setText("");
	playerlabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));           //フォント設定
	BO.initialize_board();                                                 //一度初期化(updateを呼ぶため)

    }

    @Override
    public void update(Observable o, Object arg){                              //BoardObserverを継承しているのでupdateをoverride
	val = BO.get_selectpiece();                                            //選ばれた駒の値をvalに入れる
	if(val != 0){                                                          //選ばれた駒が有効（値が0でない）場合は選ばれた値の画像を表示
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");               //iconに選ばれた値の画像を読み込む
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*1.0),(int)(icon.getIconHeight()*1.0),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);                     //大きさを調整したsmallimageをsmalliconに読み込ませる
	    label.setIcon(smallicon);                                          //ラベルに配置する
	} else {
	    label.setIcon(null);                                               //もし有効な駒でなければ何も表示しない
	}
	playernum = BO.get_playernum();                                        //何Pの番なのかをplayernumに入れる
	if(playernum == 1){                                                    //playernumに応じて色を決める
	    maincolor = "blue";
	    opponentcolor = "red";
	} else {
	    maincolor = "red";
	    opponentcolor = "blue";
	}
	situation = BO.get_situation();                                        //選択画面なのか、盤面に置く画面なのかをsituationとして受け取る
	if(situation == 0)                                                     //situationに応じてメッセージを表示する
	    playerlabel.setText("<html><span style='font-size:24pt; color: "+maincolor+";'>"+playernum+"P</span>が相手の駒を選んでください<br>そろっていれば<span style='font-size:26pt; color: #FFCC00;'>Quarto!</span>と宣言してください</html>");
	if(situation == 1)
	    playerlabel.setText("<html><span style='font-size:24pt; color: "+maincolor+";'>"+playernum+"P</span>が駒を盤面に置いてください<br>または<span style='font-size:24pt; color: "+opponentcolor+";'>"+(playernum %2 +1)+"P</span>が相手の駒を選びなおしてください</html>");
    }
    

}

class Battle extends BoardObserver implements MouseListener {   //BattleはBoardObserverを継承, MouseListenerを追加
    private int place;
    public Battle(BoardObservable observable, int place){
	super(observable);                                      //親のコンストラクタを一度呼び出す
	this.place = place;
	this.addMouseListener(this);                            //MouseListenerを追加
        val = BO.get_piece(place);
    }
    
    @Override                                                   //updateをOverrideでBattle用に変更
    public void update(Observable o, Object arg){
        val = BO.get_piece(place);                              //盤面に置かれるべき駒の値をvalに入れる
	if(val != 0){                                           //駒が有効であれば値の画像を表示 表示方法はSelectと同様
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.7),(int)(icon.getIconHeight()*0.7),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	}
	if(BO.get_situation() == 2){
	    if(BO.is_inline(BO.get_completeline(), place) == 1){
		this.setBackground(new Color(255,0,0,100));
	    }
	}
    }
    
    
    public void mouseClicked(MouseEvent e){                     //マウスで盤面をクリックされた時の動作
	val = BO.get_selectpiece();                             //どの駒が選ばれているかをvalに入れる
	if(val == 0)                                            //何も選ばれていなければそのまま終了
	    return;
	if(BO.get_piece(place) != 0)                            //盤面にすでに置かれている場合はそのまま終了
	    return;
	BO.set_situation(0);                                    //situationを0（選択画面）に変更
	BO.set_piece(val, place);                               //盤面に選ばれた駒を置く
	if(!BO.isSingle()){
	    BO.sendbattle(place, val);
	}
	setBackground(null);                                    //背景（青）を消す
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e){ }
    public void mouseEntered(MouseEvent e) { 
	if(BO.get_situation() != 2)
	    this.setBackground(new Color(0,0,255,50));              //マウスがこのpanelに入った場合背景を青にする
    }
    public void mouseExited(MouseEvent e)  {
	if(BO.get_situation() != 2)
	    this.setBackground(null);                               //マウスがこのpanelから出た場合背景を消す
    }
    
}

class Standby extends BoardObserver implements MouseListener {  //StandbyはBoardObserverを継承, MouseListenerを追加
    private int tmp;
    private int place;
    private int selectplace;
    public Standby(BoardObservable observable, int place){            
	super(observable);                                      //親のコンストラクタを一度呼びだす
	this.place = place;
	this.addMouseListener(this);                            //MouseListenerを追加
	val = BO.get_standbypiece(place);                       //現在ある駒の値をvalに入れる
	if(val != 0){                                           //もし値があればその値の画像を表示 表示方法は同様                                  
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.4),(int)(icon.getIconHeight()*0.4),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {
	    label.setIcon(null);
	}
    }
    
    @Override                                                   //updateをOverrideでStandby用に変更
    public void update(Observable o, Object arg){
	val = BO.get_standbypiece(place);                       //現在ある駒の値をvalに入れる
	if(val != 0){                                           //駒が有効であればその値の画像を表示 表示方法は同様
	    label.setText("");
	    ImageIcon icon = new ImageIcon("./img/"+val+".png");
	    Image smallimg = icon.getImage().getScaledInstance((int)(icon.getIconWidth()*0.4),(int)(icon.getIconHeight()*0.4),Image.SCALE_DEFAULT);
	    ImageIcon smallicon = new ImageIcon(smallimg);
	    label.setIcon(smallicon);
	} else {                                                //駒がなければ何も表示しない
	    label.setIcon(null);
	}
    }
    
    public void mouseClicked(MouseEvent e){                     //マウスで置き駒をクリックされた時の動作
	val = BO.get_standbypiece(place);                       //現在ある駒の値をvalに入れる
	tmp = BO.get_selectpiece();                             //現在選ばれている(Select)にある駒の値をtmpに入れる
	if(val == 0)                                            //駒がなければそのまま終了
	    return;
	if(tmp == 0)                                            //駒が選ばれていなければ、playernumを変更
	    BO.set_playernum(BO.get_playernum()%2 + 1);
	else 
	    return;
	BO.set_situation(1);                                    //situaitonを盤面におく状態（1）に変更
	BO.set_selectpiece(val, place);                         //valを選びSelectにセットする
	if(!BO.isSingle()){
	    BO.sendselect(place);
	}
	setBackground(null);                                    //背景(青)を消す

    }
    
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e){ }
    public void mouseEntered(MouseEvent e) { 
	this.setBackground(new Color(0,0,255,50));              //マウスの動作はBattleと同様
    }
    public void mouseExited(MouseEvent e)  {
	this.setBackground(null);
    }
    
}

class CompleteButton extends BoardObserver implements ActionListener {   //BoardObsever継承, ActionListenerを追加
    private JButton complete;
    private String maincolor;
    public CompleteButton(BoardObservable observable){
	super(observable);                                      //親コンストラクタを呼び出す
	complete = new JButton("<html><span style='font-size:50pt; color: #FFCC00;' >Quarto!</span></html>"); //ボタンを作成
	complete.setFocusPainted(false);                        //ボタンの枠線を消す
	this.setLayout(new GridLayout(2,1));                    //panel内をGirdLayoutにし、縦に二分割
	this.add(complete);                                     //ボタンを下に追加;
	complete.addActionListener(this);                       //ActionListenerを追加
    }

    @Override
    public void actionPerformed(ActionEvent e) {                //ボタンが押された時の動作
	super.actionPerformed(e);
	if(!BO.isSingle()){
	    BO.recvcomplete();
	}
	situation = BO.get_situation();                         //situationを入手
	if(e.getSource()==complete || situation == 2){
	    playernum = BO.get_playernum();                         //playernumを入手
	    if(playernum == 1){                                     //playernumに応じて色を決める
		maincolor = "blue";
	    } else {
		maincolor = "red";
	    }
	    if(situation == 0 || situation == 2){                                     //判定できるのは盤面に置いた後のみ(situaitonが0)
		if(BO.is_complete() == 1 || situation == 2){                          //is_completeが1ならば揃っている
		    label.setText("<html>揃っています<br><span style='font-size:30pt; color:"+ maincolor+";'>"+playernum+"P</span>の勝ちです</html>");
		    BO.finish_board();
		    if(!BO.isSingle()){
			BO.sendcomplete(situation, playernum);
		    }
		} else{
		    label.setText("揃っていません");
		}
	    }
	}
    }
}

class BoardFrame extends JFrame {
    
    public JPanel BattlePanel, SubPanel, WaitPanel, scPanel;
    public Battle tmp1;
    public Standby tmp2;
    public BoardObservable b;
    public BoardFrame(BoardObservable bo){
	b = bo;
	BattlePanel = new JPanel(); 
	SubPanel = new JPanel();
	WaitPanel = new JPanel();
	scPanel = new JPanel();
	this.setLayout(new GridLayout(1,2));         //画面をBattlePanel,SubPanel用に２分割
	BattlePanel.setLayout(new GridLayout(4,4));  //BattlePanel内を16分割
	for(int i = 0; i < 16; i++){
	    tmp1 = new Battle(b, i);
	    BattlePanel.add(tmp1);
	    tmp1.setBorder(new LineBorder(Color.BLACK, 3)); 
	}
	SubPanel.setLayout(new GridLayout(2,1));     //SubPanel内をWaitPanel,Select用に２分割
	WaitPanel.setLayout(new GridLayout(4,4));    //WaitPanel内を16分割
	for(int j = 0; j < 16; j++){
	    tmp2 = new Standby(b, j);
	    WaitPanel.add(tmp2);
	    tmp2.setBorder(new LineBorder(Color.BLUE, 1));
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
    
    public static void main(String[] args) {
	String str;
	boolean server=false;
	if (args.length<2){
	    System.out.println("Usage : java BoardFrame {server/single/{host name}} {port no.} \n");     
	    System.exit(1);
	}
	BoardObservable bo;
	if (args[0].equals("server")){
	    server=true;
	    System.out.println("Server mode");
	    str="server";
	    bo = new BoardObservable(server,args[0],Integer.parseInt(args[1]));
	}else if (args[0].equals("single")){
	    server=true;
	    System.out.println("Single mode");
	    str="single";
	    bo = new BoardObservable();
	}else{
	    server=false;
	    System.out.println("Client mode");
	    str="cilent";
	    bo = new BoardObservable(server,args[0],Integer.parseInt(args[1]));
	}
	BoardFrame frame = new BoardFrame(bo);
    }
    
}
