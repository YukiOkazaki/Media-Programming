# Media-Programming
- 同じ学科の3人で作成したボードゲームのQuarto．
- Javaを用いて初めて作った作品．
- ネットワーク対戦型ボードゲームで，端末上で動作する．
- `java -jar Quarto.jar single [任意ポート番号]`で端末一つモードで対戦可能．
- サーバ側：`java -jar Quarto.jar server [任意ポート番号]`，クライアント側：`java -jar Quarto.jar [サーバ側hostname] [サーバ側ポート番号]`でネットワーク対戦が可能．
