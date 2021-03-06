import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

class CommServer {
  private ServerSocket serverS = null;
  private Socket clientS = null;
  private PrintWriter out = null;
  private BufferedReader in = null;
  private int port = 0;

  CommServer() {
  }

  CommServer(int port) {
    open(port);
  }

  CommServer(CommServer cs) {
    serverS = cs.getServerSocket();
    open(cs.getPortNo());
  }

  ServerSocket getServerSocket() {
    return serverS;
  }

  int getPortNo() {
    return port;
  }

  // サーバ用のソケット(通信路)のオープン
  // サーバ用のソケットはクライアントからの接続待ち専用．
  // ポート番号のみを指定する．
  boolean open(int port) {
    this.port = port;
    try {
      if (serverS == null) {
        serverS = new ServerSocket(port);
      }
    } catch (IOException e) {
      System.err.println("ポートにアクセスできません。");
      System.exit(1);
    }
    try {
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
  boolean send(String msg) {
    if (out == null) {
      return false;
    }
    out.println(msg);
    return true;
  }

  // データ受信
  String recv() {
    String msg = null;
    if (in == null) {
      return null;
    }
    try {
      msg = in.readLine();
    } catch (SocketTimeoutException e) {
      // System.err.println("タイムアウトです．");
      return null;
    } catch (IOException e) {
      System.err.println("受信に失敗しました。");
      System.exit(1);
    }
    return msg;
  }

  // タイムアウトの設定
  int setTimeout(int to) {
    try {
      clientS.setSoTimeout(to);
    } catch (SocketException e) {
      System.err.println("タイムアウト時間を変更できません．");
      System.exit(1);
    }
    return to;
  }

  // ソケットのクローズ (通信終了)
  void close() {
    try {
      in.close();
      out.close();
      clientS.close();
      serverS.close();
    } catch (IOException e) {
      System.err.println("ソケットのクローズに失敗しました。");
      System.exit(1);
    }
    in = null;
    out = null;
    clientS = null;
    serverS = null;
  }
}

class CommClient {
  Socket clientS = null;
  BufferedReader in = null;
  PrintWriter out = null;

  CommClient() {
  }

  CommClient(String host, int port) {
    open(host, port);
  }

  // クライアントソケット(通信路)のオープン
  // 接続先のホスト名とポート番号が必要．
  boolean open(String host, int port) {
    try {
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
  boolean send(String msg) {
    if (out == null) {
      return false;
    }
    out.println(msg);
    return true;
  }

  // データ受信
  String recv() {
    String msg = null;
    if (in == null) {
      return null;
    }
    try {
      msg = in.readLine();
    } catch (SocketTimeoutException e) {
      return null;
    } catch (IOException e) {
      System.err.println("受信に失敗しました。");
      System.exit(1);
    }
    return msg;
  }

  // タイムアウトの設定
  int setTimeout(int to) {
    try {
      clientS.setSoTimeout(to);
    } catch (SocketException e) {
      System.err.println("タイムアウト時間を変更できません。");
      System.exit(1);
    }
    return to;
  }

  // ソケットのクローズ (通信終了)
  void close() {
    try {
      in.close();
      out.close();
      clientS.close();
    } catch (IOException e) {
      System.err.println("ソケットのクローズに失敗しました。");
      System.exit(1);
    }
    in = null;
    out = null;
    clientS = null;
  }
}

class Line { // 各列の4つの要素について
  int p1, p2, p3, p4;

  public Line(int p1, int p2, int p3, int p4) {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
    this.p4 = p4;
  }
}

class BoardObservable extends Observable {
  private boolean server;
  private boolean single = false;
  private CommServer sv = null;
  private CommClient cl = null;
  private int b[]; // 盤面
  private int koma[]; // 駒を収納する配列
  private Line l[]; // 盤面上の列を収納する配列
  private int sp; // プレイヤーが選んだ位置(select position)
  private int playernum; // 操作するのが何Pなのか1or2
  private int mynum; // playerの番号サーバーが1P,クライアントが2P
  private int situation; // 0が選択画面，1が盤面に置く 2が終了
  private int completeline; // 要素がそろっている列

  public BoardObservable(boolean server, String host, int port) {
    this.server = server;
    if (server) {
      System.out.println("Waiting for connection with port no: " + port);
      sv = new CommServer(port);
      sv.setTimeout(1);
      System.out.println("Connected!");
      mynum = 1;
    } else {
      cl = new CommClient(host, port);
      cl.setTimeout(1);
      System.out.println("Connected to " + host + ":" + port + "!");
      mynum = 2;
    }
  }

  public BoardObservable() {
    this.server = true;
    this.single = true;
  }

  public void initialize_board() {
    b = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 盤面を初期化
    koma = new int[] { 1, 5, 7, 35, 2, 10, 14, 70, 3, 15, 21, 105, 6, 30, 42, 210 }; // 置き駒配列
    l = new Line[] { new Line(0, 4, 8, 12), new Line(1, 5, 9, 13), new Line(2, 6, 10, 14), new Line(3, 7, 11, 15),
        new Line(0, 1, 2, 3), new Line(4, 5, 6, 7), new Line(8, 9, 10, 11), new Line(12, 13, 14, 15),
        new Line(0, 5, 10, 15), new Line(3, 6, 9, 12), };
    situation = 0;
    completeline = 0;
    sp = 0;
    setChanged();
    notifyObservers();
  }

  public void finish_board() {
    situation = 2;
    setChanged();
    notifyObservers();
  }

  public void set_piece(int s, int p) { // sは盤面にセットする値 pは盤面の位置
    b[p] = s;
    sp = 0; // 選択した位置を初期化
    setChanged();
    notifyObservers();
  }

  public void set_selectpiece(int s, int p) {
    sp = s;
    koma[p] = 0; // 置き駒配列から消去
    setChanged();
    notifyObservers();
  }

  public void set_standbypiece(int s, int p) {
    koma[p] = s;
    setChanged();
    notifyObservers();
  }

  public int get_piece(int p) {
    return b[p];
  }

  public int get_standbypiece(int p) {
    return koma[p];
  }

  public int get_lineval(Line l) {
    int n1 = get_piece(l.p1);
    int n2 = get_piece(l.p2);
    int n3 = get_piece(l.p3);
    int n4 = get_piece(l.p4);
    int mul;
    mul = n1 * n2 * n3 * n4; // 勝敗判定計算のため，列の4つの駒の値の積を求める
    return mul;
  }

  public int is_complete() {
    int c[] = new int[10];
    for (int i = 0; i < 10; i++) {
      c[i] = get_lineval(l[i]);// １０つの列について
      if (c[i] % 16 == 0 || c[i] % 2 != 0 || c[i] % 81 == 0 || c[i] % 3 != 0 || c[i] % 625 == 0 || c[i] % 5 != 0
          || c[i] % 2401 == 0 || c[i] % 7 != 0) { // 勝敗判定計算
        if (c[i] == 0) {
          continue;
        } else {
          completeline = i; // どの列で揃ったのか保存する
          return 1;
        }
      }
    }
    if (is_draw() == 1)
      return 1;
    return 0;
  }

  public int get_selectpiece() {
    return sp;
  }

  public int get_mynum() {
    return mynum;
  }

  public int get_playernum() {
    return playernum;
  }

  public void set_playernum(int num) {
    playernum = num;
  }

  public int get_situation() {
    return situation;
  }

  public void set_situation(int num) {
    situation = num;
  }

  public int get_completeline() {
    return completeline;
  }

  public void set_completeline(int num) {
    completeline = num;
  }

  public int is_inline(int i, int p) { // 位置pがどの列に含まれているかを返す
    if (l[i].p1 == p || l[i].p2 == p || l[i].p3 == p || l[i].p4 == p)
      return 1;
    return 0;
  }

  public int is_draw() { // 引き分けかどうかを判定する
    int sum = 0;
    for (int i = 0; i < 16; i++) {
      sum += koma[i];
    }
    if (sum == 0)
      return 1;
    else
      return 0;
  }

  public boolean isServer() {
    return server;
  }

  public boolean isSingle() {
    return single;
  }

  public void sendplayernum(int playernum) {
    String msg = String.format("%d", playernum);
    if (server) {
      sv.send(msg);
    } else {
      cl.send(msg);
    }
  }

  public void recvplayernum() {
    String msg;
    if (server) {
      msg = sv.recv();
    } else {
      msg = cl.recv();
    }
    if (msg == null)
      return;
    String[] sm = msg.split(" ");
    set_playernum(Integer.parseInt(sm[0]));
    setChanged();
    notifyObservers();
  }

  public void sendselect(int place) {
    String msg = String.format("%d %d %d %d", situation, playernum, sp, place);
    if (server) {
      sv.send(msg);
    } else {
      cl.send(msg);
    }
  }

  public void sendbattle(int place, int val) {
    String msg = String.format("%d %d %d", situation, place, val);
    if (server) {
      sv.send(msg);
    } else {
      cl.send(msg);
    }
  }

  public void recvselect() {
    String msg;
    if (server) {
      msg = sv.recv();
    } else {
      msg = cl.recv();
    }
    if (msg == null)
      return;
    String[] sm = msg.split(" ");
    if (Integer.parseInt(sm[0]) != 2) {
      set_selectpiece(Integer.parseInt(sm[2]), Integer.parseInt(sm[3]));
    } else {
      set_completeline(Integer.parseInt(sm[3]));
    }
    set_playernum(Integer.parseInt(sm[1]));
    set_situation(Integer.parseInt(sm[0]));
    setChanged();
    notifyObservers();
  }

  public void recvbattle() {
    String msg;
    if (server) {
      msg = sv.recv();
    } else {
      msg = cl.recv();
    }
    if (msg == null)
      return;
    String[] sm = msg.split(" ");
    set_piece(Integer.parseInt(sm[2]), Integer.parseInt(sm[1]));
    set_situation(Integer.parseInt(sm[0]));
    setChanged();
    notifyObservers();
  }
}

class BoardObserver extends JPanel implements Observer, ActionListener { // observer側のすべての親クラス
  protected Timer timer;
  protected BoardObservable BO;
  protected JLabel label;
  protected int val;
  protected int playernum;
  protected int situation;
  protected int mynum;

  public BoardObserver(BoardObservable observable) {
    BO = observable;
    BO.addObserver(this); // observerに登録
    label = new JLabel();
    this.setLayout(new BorderLayout()); // panelは通常FlowLayoutなのでBorderLayoutに変更
    this.add(label, BorderLayout.CENTER); // panel内に表示されるラベルの追加
    label.setHorizontalAlignment(JLabel.CENTER); // ラベルの位置は水平方向で中心
    label.setVerticalAlignment(JLabel.CENTER); // ラベルの位置は垂直方向で中心
    label.setFont(new Font(Font.DIALOG, Font.BOLD, 40)); // ラベルのフォントの設定
    timer = new Timer(10, this); //ネットワーク対戦時同期させるためのTimer
    timer.start();
    mynum = BO.get_mynum();

  }

  public void update(Observable o, Object arg) {
  }

  public void actionPerformed(ActionEvent e) { // ネットワーク対戦時Timerが呼ぶ
    if (!BO.isSingle()) {
      if (BO.get_situation() == 0) // 駒を選択している時はrecvselectを呼ぶ
        BO.recvselect();
      if (BO.get_situation() == 1) // 駒を配置しようとしているときはrecvbattleを呼ぶ
        BO.recvbattle();
    }
  }
}

class Select extends BoardObserver implements ActionListener {
  private JLabel playerlabel; // 何Pが操作するべきなのか等の情報を表示するラベル
  private String maincolor; // 1P,2Pそれぞれの色を指定する

  public Select(BoardObservable observable, int senkou) {
    super(observable); // 親のコンストラクタを一度呼びだす
    playerlabel = new JLabel();
    this.add(playerlabel, BorderLayout.SOUTH); // playerlabelを下に配置
    val = BO.get_selectpiece();
    playerlabel.setHorizontalAlignment(JLabel.CENTER); // plyaerlabelの水平方向の位置は中心
    playerlabel.setText("");
    playerlabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20)); // フォント設定
    BO.initialize_board(); // 一度初期化(updateを呼ぶため)
    if (BO.isServer() && !BO.isSingle())
      BO.sendplayernum(senkou); // server側であれば決定した先攻をModelに送信する
    else if (!BO.isServer() && !BO.isSingle())
      BO.recvplayernum(); // client側であればModelから先攻を受け取る

  }

  @Override // updateをOverrideでSelect用に変更
  public void update(Observable o, Object arg) { // BoardObserverを継承しているのでupdateをoverride
    val = BO.get_selectpiece(); // 選ばれた駒の値をvalに入れる
    if (val != 0) { // 選ばれた駒が有効（値が0でない）場合は選ばれた値の画像を表示
      label.setText("");
      ImageIcon icon = new ImageIcon("./img/" + val + ".png"); // iconに選ばれた値の画像を読み込む
      Image smallimg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * 1.0),
          (int) (icon.getIconHeight() * 1.0), Image.SCALE_DEFAULT);
      ImageIcon smallicon = new ImageIcon(smallimg); // 大きさを調整したsmallimageをsmalliconに読み込ませる
      label.setIcon(smallicon); // ラベルに配置する
    } else {
      label.setIcon(null); // もし有効な駒でなければ何も表示しない
    }
    playernum = BO.get_playernum(); // 何Pの番なのかをplayernumに入れる
    if (playernum == 1) { // playernumに応じて色を決める
      maincolor = "blue";
    } else {
      maincolor = "red";
    }
    situation = BO.get_situation(); // 選択画面なのか，盤面に置く画面なのかをsituationとして受け取る
    if (situation == 0) { // situationに応じてメッセージを表示する
      if (BO.isSingle()) {
        playerlabel.setText("<html><span style='font-size:24pt; color: " + maincolor + ";'>" + playernum
            + "P</span>は相手の駒を選んでください<br>揃っていれば<span style='font-size:26pt; color: #FF8C00;'>Quarto!</span>を押してください</html>");
      } else {
        if (playernum == mynum) { // ネットワーク対戦時，手番に合わせて表示するメッセージを変える
          playerlabel.setText(
              "<html><span style='font-size:24pt; color: blue;'>あなた</span>は相手の駒を選んでください<br>揃っていれば<span style='font-size:26pt; color: #FF8C00;'>Quarto!</span>と押してください</html>");
        } else {
          playerlabel
              .setText("<html>待機中...   <br><span style='font-size:24pt; color: red;'>あいて</span>が駒を選んでいます</html>");
        }
      }
    }
    if (situation == 1) {
      if (BO.isSingle()) {
        playerlabel.setText("<html><span style='font-size:24pt; color: " + maincolor + ";'>" + playernum
            + "P</span>は駒を盤面に置いてください</html>");
      } else {
        if (playernum == mynum) { // ネットワーク対戦時，手番に合わせて表示するメッセージを変える
          playerlabel.setText("<html><span style='font-size:24pt; color: blue;'>あなた</span>は駒を盤面に置いてください</html>");
        } else {
          playerlabel
              .setText("<html>待機中...   <br><span style='font-size:24pt; color: red;'>あいて</span>が駒を置いています</html>");
        }
      }
    }
    if (situation == 2) { // 終了した場合(situationが2)の場合はメッセージを表示させない
      playerlabel.setText("");
    }
  }

}

class Battle extends BoardObserver implements MouseListener { // BattleはBoardObserverを継承, MouseListenerを追加
  private int place;

  public Battle(BoardObservable observable, int place) {
    super(observable); // 親のコンストラクタを一度呼び出す
    this.place = place;
    this.addMouseListener(this); // MouseListenerを追加
    val = BO.get_piece(place);
  }

  @Override // updateをOverrideでBattle用に変更
  public void update(Observable o, Object arg) {
    val = BO.get_piece(place); // 盤面に置かれるべき駒の値をvalに入れる
    if (val != 0) { // 駒が有効であれば値の画像を表示 表示方法はSelectと同様
      label.setText("");
      ImageIcon icon = new ImageIcon("./img/" + val + ".png");
      Image smallimg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * 0.7),
          (int) (icon.getIconHeight() * 0.7), Image.SCALE_DEFAULT);
      ImageIcon smallicon = new ImageIcon(smallimg);
      label.setIcon(smallicon);
    } else {
      label.setIcon(null);
    }
    if (BO.get_situation() == 2) { // ゲーム終了の場合
      if (BO.is_inline(BO.get_completeline(), place) == 1 && BO.is_draw() == 0) { // 揃っている列にPanelのplaceが入っていれば，背景を赤くする
        this.setBackground(new Color(255, 50, 50, 250));
      }
    } else {
      this.setBackground(null);
    }
  }

  public void mouseClicked(MouseEvent e) { // マウスで盤面をクリックされた時の動作
    if (!BO.isSingle() && BO.get_playernum() != mynum)
      return;
    val = BO.get_selectpiece(); // どの駒が選ばれているかをvalに入れる
    if (val == 0) // 何も選ばれていなければそのまま終了（動作不可能にさせる）
      return;
    if (BO.get_piece(place) != 0) // 盤面にすでに置かれている場合はそのまま終了（動作不可能にさせる）
      return;
    BO.set_situation(0); // situationを0（選択画面）に変更
    BO.set_piece(val, place); // 盤面に選ばれた駒を置く
    if (!BO.isSingle()) {
      BO.sendbattle(place, val); // ネットワーク対戦時，置いた駒の情報を相手に送る
    }
    setBackground(null); // 背景（青）を消す
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
    if (BO.get_situation() != 2)
      this.setBackground(new Color(0, 0, 255, 50)); // マウスがこのpanelに入った場合背景を青にする
  }

  public void mouseExited(MouseEvent e) {
    if (BO.get_situation() != 2)
      this.setBackground(null); // マウスがこのpanelから出た場合背景を消す
  }

}

class Standby extends BoardObserver implements MouseListener { // StandbyはBoardObserverを継承, MouseListenerを追加
  private int tmp;
  private int place;

  public Standby(BoardObservable observable, int place) {
    super(observable); // 親のコンストラクタを一度呼びだす
    this.place = place;
    this.addMouseListener(this); // MouseListenerを追加
    val = BO.get_standbypiece(place); // 現在ある駒の値をvalに入れる
    if (val != 0) { // もし値があればその値の画像を表示 表示方法は同様
      label.setText("");
      ImageIcon icon = new ImageIcon("./img/" + val + ".png");
      Image smallimg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * 0.4),
          (int) (icon.getIconHeight() * 0.4), Image.SCALE_DEFAULT);
      ImageIcon smallicon = new ImageIcon(smallimg);
      label.setIcon(smallicon);
    } else {
      label.setIcon(null);
    }
  }

  @Override // updateをOverrideでStandby用に変更
  public void update(Observable o, Object arg) {
    val = BO.get_standbypiece(place); // 現在ある駒の値をvalに入れる
    if (val != 0) { // 駒が有効であればその値の画像を表示 表示方法は同様
      label.setText("");
      ImageIcon icon = new ImageIcon("./img/" + val + ".png");
      Image smallimg = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * 0.4),
          (int) (icon.getIconHeight() * 0.4), Image.SCALE_DEFAULT);
      ImageIcon smallicon = new ImageIcon(smallimg);
      label.setIcon(smallicon);
    } else { // 駒がなければ何も表示しない
      label.setIcon(null);
    }
  }

  public void mouseClicked(MouseEvent e) { // マウスで置き駒をクリックされた時の動作
    if (BO.get_situation() == 2)
      return;
    if (!BO.isSingle() && BO.get_playernum() != mynum) // ネットワーク対戦時，相手の手番であればこちらは何もしない（操作不可能にする）
      return;
    val = BO.get_standbypiece(place); // 現在ある駒の値をvalに入れる
    tmp = BO.get_selectpiece(); // 現在選ばれている(Select)にある駒の値をtmpに入れる
    if (val == 0) // 駒がなければそのまま終了
      return;
    if (tmp == 0) // 駒が選ばれていなければ，playernumを変更（2度選ぶのを防ぐ）
      BO.set_playernum(BO.get_playernum() % 2 + 1);
    else
      return;
    BO.set_situation(1); // situaitonを盤面におく状態（1）に変更
    BO.set_selectpiece(val, place); // valを選びSelectにセットする
    if (!BO.isSingle()) {
      BO.sendselect(place); // ネットワーク対戦時，選ばれた場所を相手に送信する
    }
    setBackground(null); // 背景(青)を消す

  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
    this.setBackground(new Color(0, 0, 255, 50)); // マウスの動作はBattleと同様
  }

  public void mouseExited(MouseEvent e) {
    this.setBackground(null);
  }

}

class CompleteButton extends BoardObserver implements ActionListener { // BoardObsever継承, ActionListenerを追加
  private JButton complete;
  private String maincolor;
  private JButton restart;
  private JButton quit;
  private JPanel finishpanel;

  public CompleteButton(BoardObservable observable) {
    super(observable); // 親コンストラクタを呼び出す
    complete = new JButton("<html><img src = 'file:title/quartobutton.png' width=350 height=100></html>"); // ボタンを作成;
    complete.setPreferredSize(null);
    complete.setFocusPainted(false); // ボタンの枠線を消す
    this.setLayout(new GridLayout(2, 1)); // panel内をGirdLayoutにし，縦に二分割
    this.add(complete); // ボタンを下に追加;
    complete.addActionListener(this); // ActionListenerを追加
    restart = new JButton("<html><img src = 'file:title/restart.png' width=175 height=75></html>");
    quit = new JButton("<html><img src = 'file:title/quitgame.png' width=175 height=75></html>");
    restart.setMargin(new Insets(10, 0, 0, 0));
    quit.setMargin(new Insets(10, 0, 0, 0));
    restart.addActionListener(this);
    quit.addActionListener(this);
    restart.setFocusPainted(false);
    quit.setFocusPainted(false);
    finishpanel = new JPanel();
    finishpanel.setLayout(new GridLayout(1, 2));
    finishpanel.add(restart);
    finishpanel.add(quit);
  }

  @Override
  public void actionPerformed(ActionEvent e) { // ボタンが押された時の動作
    super.actionPerformed(e);
    if (e.getSource() == restart) { // 再戦ボタンが押されると初期状態に戻す
      label.setText("");
      this.remove(finishpanel);
      this.add(complete);
      repaint();
      timer.start();
      BO.initialize_board();
    }
    if (e.getSource() == quit) { // 終了ボタンが押されるとプログラムを終了させる
      System.exit(0);
    }
    situation = BO.get_situation(); // situationを入手
    playernum = BO.get_playernum(); // playernumを入手
    if (e.getSource() == complete || situation == 2) {
      if (playernum == 1) { // playernumに応じて色を決める
        maincolor = "blue";
      } else {
        maincolor = "red";
      }
      if (situation == 0 || situation == 2) { // 判定できるのは盤面に置いた後のみ(situaitonが0)
        if (!BO.isSingle())
          BO.recvselect();
        if (BO.is_complete() == 1 || situation == 2) { // is_completeが1ならば揃っている situationが2の場合は相手が勝敗判定ボタンを押したことになるのでこちらも終了処理
          if (BO.is_draw() == 1) { // 引き分けであれば引き分けと表示
            label.setText("引き分けです");
          } else {
            if (BO.isSingle()) { 
              label.setText("<html>揃っています<br><span style='font-size:60pt; color:" + maincolor + ";'>" + playernum
                  + "P</span>の勝ちです</html>");
            } else { // ネットワーク対戦時，自分が押した場合は勝ちと表示
              if (playernum == mynum) {
                label.setText("<html>揃っています<br><span style='font-size:60pt; color: blue;'>あなた</span>の勝ちです</html>");
              } else { // 相手に押された場合は負けと表示
                label.setText("<html>揃っています<br><span style='font-size:60pt; color: red;'>あいて</span>の勝ちです</html>");
              }
            }
          }
          BO.finish_board(); // 終了処理を呼び出す
          if (situation == 0) // ネットワーク対戦時，自分が勝敗判定ボタンを押した場合どの列が揃っているかを相手に送信
            if (!BO.isSingle())
              BO.sendselect(BO.get_completeline());
          this.remove(complete); // 勝敗判定ボタンを消す
          this.add(finishpanel); // 再戦・ゲーム終了ボタンを表示する
          repaint();
          timer.stop(); // 同期をやめる
        } else {
          label.setText("揃っていません"); // 間違って押された時は揃っていませんと表示するだけ
        }
      }
    }
  }
}

class BoardFrame extends JFrame {

  public JPanel BattlePanel, SubPanel, WaitPanel, scPanel; // ゲーム画面を構成する4つのJPanel
  public Battle tmp1;
  public Standby tmp2;
  public BoardObservable b;

  public BoardFrame(BoardObservable bo, int senkou) {
    b = bo;
    b.set_playernum(senkou);
    BattlePanel = new JPanel();
    SubPanel = new JPanel();
    WaitPanel = new JPanel();
    scPanel = new JPanel();
    this.setLayout(new GridLayout(1, 2)); // 画面をBattlePanel,SubPanel用に２分割
    BattlePanel.setLayout(new GridLayout(4, 4)); // BattlePanel内を16分割
    for (int i = 0; i < 16; i++) {
      tmp1 = new Battle(b, i);
      BattlePanel.add(tmp1);
      tmp1.setBorder(new LineBorder(Color.BLACK, 3));
    }
    SubPanel.setLayout(new GridLayout(2, 1)); // SubPanel内をWaitPanel,Select用に２分割
    WaitPanel.setLayout(new GridLayout(4, 4)); // WaitPanel内を16分割
    for (int j = 0; j < 16; j++) {
      tmp2 = new Standby(b, j);
      WaitPanel.add(tmp2);
      tmp2.setBorder(new LineBorder(Color.BLUE, 1));
    }
    SubPanel.add(WaitPanel);
    scPanel.setLayout(new GridLayout(1, 2));
    scPanel.add(new Select(b, senkou));
    scPanel.add(new CompleteButton(b));
    SubPanel.add(scPanel);
    this.add(BattlePanel);
    this.add(SubPanel);
    this.setTitle("Quarto - Play");
    this.setSize(1600, 800);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }
}

class HowtoFrame extends JFrame implements ActionListener {
  public HowtoFrame() {
    this.setSize(1600, 800);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JButton b = new JButton("<html><span style='font-size:30pt; color:black;'>Close</span></html>");
    this.add(b, BorderLayout.SOUTH);
    b.addActionListener(this);
    JLabel l = new JLabel("<html><img src='file:title/howtoplay.png' width=1600 height=800></html>", JLabel.CENTER);
    this.add(l, BorderLayout.CENTER);
    this.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    this.setVisible(false);
  }
}

class TitleFrame extends JFrame implements ActionListener {

  public JPanel TitlePanel; // タイトル画面全体用のJPanel
  public JButton StartButton, HowtoButton; // ゲーム画面，ルール画面遷移用のJButton
  public JLabel SelectLabel, BackGroundLabel;
  public JRadioButton Button1P, Button2P; // 先攻後攻の選択用のJRadioButton
  public BoardObservable b;
  public String str;
  public int senkou;

  public TitleFrame(BoardObservable bo, String str) {
    ButtonGroup vacationGroup = new ButtonGroup();
    this.str = str;
    this.b = bo;
    this.senkou = 1; // 初期値として引数senkouに1を与える
    TitlePanel = new JPanel();
    this.add(TitlePanel);
    StartButton = new JButton("<html><img src = 'file:title/start.png' width=275 height=125></html>");
    HowtoButton = new JButton("<html><img src = 'file:title/howtoplaybutton.png' width=275 height=125></span></html>");
    StartButton.setMargin(new Insets(20, 0, 0, 0));
    HowtoButton.setMargin(new Insets(20, 0, 0, 0));
    BackGroundLabel = new JLabel("<html><img src='file:title/title.png' width=1600 height=800></html>"); // 背景追加
    SelectLabel = new JLabel("<html><span style='font-size:25pt; color:black;'>先攻：</span></html>");
    TitlePanel.setLayout(null);
    BackGroundLabel.setLayout(new BorderLayout());
    SelectLabel.setLayout(new BorderLayout());
    BackGroundLabel.setBounds(0, 0, 1600, 800);
    SelectLabel.setBounds(700, 400, 100, 100);
    StartButton.setBounds(300, 600, 300, 100);
    HowtoButton.setBounds(1000, 600, 300, 100);
    StartButton.addActionListener(this);
    HowtoButton.addActionListener(this);
    if (b.isServer() && !b.isSingle()) { // server mode時の先攻後攻ボタンの表示
      Button1P = new JRadioButton("<html><span style='color:black;'>1P</span></html>", true);
      Button2P = new JRadioButton("<html><span style='color:black;'>2P</span></html>");
      Button1P.setOpaque(false);
      Button2P.setOpaque(false);
      Button1P.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 30));
      Button2P.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 30));
      Button1P.setBounds(780, 400, 70, 100);
      Button2P.setBounds(850, 400, 70, 100);
      vacationGroup.add(Button1P);
      vacationGroup.add(Button2P);
      Button1P.addActionListener(this);
      Button2P.addActionListener(this);
      TitlePanel.add(Button1P);
      TitlePanel.add(Button2P);
      TitlePanel.add(SelectLabel);
    }
    TitlePanel.add(StartButton);
    TitlePanel.add(HowtoButton);
    TitlePanel.add(BackGroundLabel);

    StartButton.setFocusPainted(false);
    HowtoButton.setFocusPainted(false);
    this.setTitle("Quarto - Title : " + str);
    this.setSize(1600, 800);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) { // 先攻後攻の決定
    if (e.getSource() == StartButton) {
      b.initialize_board();
      BoardFrame f = new BoardFrame(b, senkou);
    } else if (e.getSource() == HowtoButton) {
      HowtoFrame h = new HowtoFrame();
    } else if (e.getSource() == Button2P) {
      Button2P.setSelected(true);
      Button1P.setSelected(false);
      senkou = 2;
    } else if (e.getSource() == Button1P) {
      Button1P.setSelected(true);
      Button2P.setSelected(false);
      senkou = 1;
    }
  }

  public static void main(String[] args) {
    String str;
    boolean server = false;
    if (args.length < 2) {
      System.out.println("Usage : java TitleFrame {server/single/{host name}} {port no.}");
      System.out.println("or");
      System.out.println("If you expand jar, Usage : java Quarto.jar {server/single/{host name}} {port no.} \n");
      System.exit(1);
    }
    BoardObservable bo;
    if (args[0].equals("server")) { // modeの決定をしてタイトル画面の表示
      server = true;
      System.out.println("Server mode");
      str = "server";
      bo = new BoardObservable(server, args[0], Integer.parseInt(args[1]));
    } else if (args[0].equals("single")) {
      server = true;
      System.out.println("Single mode");
      str = "single";
      bo = new BoardObservable();
    } else {
      server = false;
      System.out.println("Client mode");
      str = "client";
      bo = new BoardObservable(server, args[0], Integer.parseInt(args[1]));
    }
    TitleFrame frame = new TitleFrame(bo, str);
  }
}
