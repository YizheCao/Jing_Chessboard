import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class Jing_Chessboard implements ActionListener {
    //窗口初始化
    JFrame frame = new JFrame("井字棋");
    JMenuBar bar = new JMenuBar();
    JMenu menu_choice = new JMenu("选项");
    JMenu menu_FirstOrLast = new JMenu("先后手");
    JMenu menu_difficulty = new JMenu("难度");
    JMenuItem item_begin = new JMenuItem("开始");
    JMenuItem item_reset = new JMenuItem("重来");
    JMenuItem item_first = new JMenuItem("玩家先");
    JMenuItem item_last = new JMenuItem("电脑先");
    JMenuItem item_high = new JMenuItem("高难度");
    JMenuItem item_low = new JMenuItem("中难度");
    JMenuItem item_middle = new JMenuItem("普通难度");
    Container container = new Container();

    //游戏的数据结构
    final int row = 3;//定义行
    final int col = 3;//定义列
    final int MAX = 2000;//
    final int MIN = -2000;
    int depth;//定义搜索树的深度，也即调整电脑的难度
    int first_or_last;//定义先后手,默认玩家先（玩家0，电脑1）
    int player;//定义哪一方下棋，1为电脑，-1为玩家
    JButton[][] board_button = new JButton[row][col];//定义九个宫格，玩家下棋为圈，电脑下棋为叉
    int[][] board_value = new int[row][col];//定义九宫格对应的value，玩家下棋为-1，电脑下棋为1，未下棋为0
    int best_x,best_y;//电脑通过极大极小值法找到对自己最有利的一步
    int computer_first;//判断是否是电脑第一步，是为1，不是为0
    int computer_x;
    int computer_y;

    public Jing_Chessboard(){
        //显示窗口
        frame.setBounds(450,120,480,480);//设定大小
        frame.setResizable(false);//不能改变大小
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭行为
        frame.setLayout(new BorderLayout());//BorderLayout布局

        //添加菜单栏
        menu_choice.add(item_begin);
        menu_choice.add(item_reset);
        menu_FirstOrLast.add(item_first);
        menu_FirstOrLast.add(item_last);
        menu_difficulty.add(item_high);
        menu_difficulty.add(item_low);
        menu_difficulty.add(item_middle);
        bar.add(menu_choice);
        bar.add(menu_FirstOrLast);
        bar.add(menu_difficulty);
        frame.add(bar,BorderLayout.NORTH);

        //给菜单的选项加监听事件
        item_begin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < row; i++){
                    for(int j = 0; j < col; j++){
                        board_value[i][j] = 0;
                        board_button[i][j].setEnabled(true);//按钮重置可以点开
                        board_button[i][j].setIcon(new ImageIcon("E://eclipse//井字棋//src//white.png"));
                    }
                }
                if(first_or_last == 1){
                    computer_first = 1;
                    if(depth == 2 || depth == 4)
                        computer_play(board_value);
                    else
                        computer_play_normal(board_value);
                    computer_first = 0;
                }
            }
        });
        item_reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < row; i++){
                    for(int j = 0; j < col; j++){
                        board_button[i][j].setEnabled(false);//按钮重置可以点开
                        board_button[i][j].setIcon(new ImageIcon("E://eclipse//井字棋//src//white.png"));
                    }
                }
            }
        });
        item_first.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                first_or_last = 0;
            }
        });
        item_last.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                first_or_last = 1;
            }
        });
        item_high.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                depth = 2;
            }
        });
        item_low.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                depth = 4;
            }
        });
        item_middle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                depth = 0;
            }
        });

        //添加container容器，放一系列按钮
        frame.add(container, BorderLayout.CENTER);//加入容器放在CENTER布局位置
        container.setLayout(new GridLayout(row,col));//向container添加Grid网格布局
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                board_value[i][j] = 0;
                JButton button = new JButton();//初始化当前按钮
                board_button[i][j] = button;
                board_button[i][j].setOpaque(true);//设置按钮初始不可见
                board_button[i][j].setIcon(new ImageIcon("E://eclipse//井字棋//src//white.png"));
                board_button[i][j].setEnabled(false);//按钮重置可以点开
                board_button[i][j].addActionListener(this);
                container.add(board_button[i][j]);//将button放到容器里面
            }
        }

        //初始化
        depth = 0;
        first_or_last = 0;
        best_x = 0;
        best_y = 0;
        computer_first = 0;
        computer_x = -1;
        computer_y = -1;

        frame.setVisible(true);//显示出来
    }

    //电脑下棋，中、高难度
    void computer_play(int[][] current_board){
        MAX_MIN(depth,current_board);
        if(computer_first == 1){
            computer_x = best_x;
            computer_y = best_y;
        }
        board_value[best_x][best_y] = 1;
        board_button[best_x][best_y].setIcon(new ImageIcon("E://eclipse//井字棋//src//cha.png"));
        board_button[best_x][best_y].setEnabled(false);
    }

    //电脑下棋，普通难度
    void computer_play_normal(int[][] current_board){
        int a = 1,x = 0,y = 0;
        while(a == 1){
            x = (int)(Math.random() * 3);
            y = (int)(Math.random() * 3);
            if(current_board[x][y] == 0){
                best_x = x;
                best_y = y;
                if(computer_first == 1){
                    computer_x = x;
                    computer_y = y;
                }
                break;
            }
        }
        board_value[best_x][best_y] = 1;
        board_button[best_x][best_y].setIcon(new ImageIcon("E://eclipse//井字棋//src//cha.png"));
        board_button[best_x][best_y].setEnabled(false);
    }

    //玩家下棋
    void player_play(int x,int y){
        board_value[x][y] = -1;
        board_button[x][y].setIcon(new ImageIcon("E://eclipse//井字棋//src//circle.png"));
        board_button[x][y].setEnabled(false);
    }

    //判断是否有空格
    int is_null(int[][] current_board){
        int flag = 0;//判断是否有空格，0没有，1有
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(current_board[i][j] == 0)
                    flag = 1;
            }
        }

        return flag;
    }

    //判断是否胜利
    int is_win(int[][] current_board){
        int isWin = 0;//判断是否赢棋，1电脑赢，-1玩家赢
        for(int i = 0; i < row; i++){
            if(current_board[i][0] == 1 && current_board[i][1] == 1 && current_board[i][2] == 1)
                isWin = 1;
            else if(current_board[i][0] == -1 && current_board[i][1] == -1 && current_board[i][2] == -1)
                isWin = -1;
        }
        for(int j = 0; j < row; j++){
            if(current_board[0][j] == 1 && current_board[1][j] == 1 && current_board[2][j] == 1)
                isWin = 1;
            else if(current_board[0][j] == -1 && current_board[1][j] == -1 && current_board[2][j] == -1)
                isWin = -1;
        }
        if(current_board[0][0] == 1 && current_board[1][1] == 1 && current_board[2][2] == 1)
            isWin = 1;
        else if(current_board[0][0] == -1 && current_board[1][1] == -1 && current_board[2][2] == -1)
            isWin = -1;
        if(current_board[2][0] == 1 && current_board[1][1] == 1 && current_board[0][2] == 1)
            isWin = 1;
        else if(current_board[2][0] == -1 && current_board[1][1] == -1 && current_board[0][2] == -1)
            isWin = -1;

        return isWin;
    }

    //评估函数
    int evaluate(int[][] current_board){
        if(is_win(current_board) == 1){
            return MAX;
        }
        if(is_win(current_board) == -1){
            return MIN;
        }

        int count = 0;
        int[][] tmp_value = new int[row][col];

        //将棋盘空白填满电脑的棋子，计算value
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(current_board[i][j] == 0)
                    tmp_value[i][j] = 1;
                else
                    tmp_value[i][j] = current_board[i][j];
            }
        }
        for(int i = 0; i < row; i++){
            count += (tmp_value[i][0] + tmp_value[i][1] + tmp_value[i][2]) / 3;
        }
        for(int j = 0; j < row; j++){
            count += (tmp_value[0][j] + tmp_value[1][j] + tmp_value[2][j]) / 3;
        }
        count += (tmp_value[0][0] + tmp_value[1][1] + tmp_value[2][2]) / 3;
        count += (tmp_value[2][0] + tmp_value[1][1] + tmp_value[0][2]) / 3;

        //将棋盘空白填满玩家的棋子，计算value
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(current_board[i][j] == 0)
                    tmp_value[i][j] = -1;
                else
                    tmp_value[i][j] = current_board[i][j];
            }
        }
        for(int i = 0; i < row; i++){
            count += (tmp_value[i][0] + tmp_value[i][1] + tmp_value[i][2]) / 3;
        }
        for(int j = 0; j < row; j++){
            count += (tmp_value[0][j] + tmp_value[1][j] + tmp_value[2][j]) / 3;
        }
        count += (tmp_value[0][0] + tmp_value[1][1] + tmp_value[2][2]) / 3;
        count += (tmp_value[2][0] + tmp_value[1][1] + tmp_value[0][2]) / 3;

        return count;
    }

    //极小极大博弈算法
    int MAX_MIN(int current_depth,int[][] currentBoard){
        int value;  //估值
        int bestValue = 0; //最好的估值
        int[][] tmp_board_value = new int[row][col];//定义电脑预测的棋盘value

        if(is_win(currentBoard) == 1 || is_win(currentBoard) == -1)
        {
            return evaluate(currentBoard);  //一般是返回极大极小值
        }
        //根据不同的玩家 进行赋值
        if(player == 1) {
            bestValue = MIN;
        }
        else if(player == -1)
        {
            bestValue = MAX;
        }
        //如果搜索深度耗尽，返回估值
        if (current_depth == 0)
            return evaluate(currentBoard);
        else if(current_depth % 2 == 0)
            player = -1;
        else if(current_depth % 2 == 1)
            player = 1;
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                tmp_board_value[i][j] = currentBoard[i][j];
            }
        }
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(tmp_board_value[i][j] == 0){
                    tmp_board_value[i][j] = player;
                    player = (player == 1) ? -1 : 1;
                    value = MAX_MIN(current_depth - 1,tmp_board_value);
                    tmp_board_value[i][j] = 0;
                    player = (player == 1) ? -1 : 1;
                    if(player == 1)
                    {
                        if(value > bestValue)
                        {
                            bestValue = value;
                            if(current_depth == depth)
                            {
                                best_x = i;
                                best_y = j;
                            }
                        }
                    }
                    else if(player == -1)
                    {
                        if(value < bestValue)
                        {
                            bestValue = value;
                            if(current_depth == depth)
                            {
                                best_x = i;
                                best_y = j;
                            }
                        }
                    }
                }
            }
        }

        return bestValue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton)e.getSource();
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(btn.equals(board_button[i][j])){
                    if(first_or_last == 0) {
                        int flag;//判断是否有空格，0没有，1有
                        int isWin_c, isWin_p;//判断是否赢棋，1电脑赢，-1玩家赢
                        player_play(i, j);
                        isWin_p = is_win(board_value);
                        if (isWin_p == -1) {
                            JOptionPane.showMessageDialog(frame, "玩家赢");
                            break;
                        }
                        flag = is_null(board_value);
                        if (flag == 0) {
                            JOptionPane.showMessageDialog(frame, "平局");
                            break;
                        }
                        //判断难度
                        if (depth == 2 || depth == 4)
                            computer_play(board_value);
                        else
                            computer_play_normal(board_value);
                        isWin_c = is_win(board_value);
                        if (isWin_c == 1) {
                            JOptionPane.showMessageDialog(frame, "电脑赢");
                            break;
                        }
                    }
                    else if(first_or_last == 1){
                        if(computer_x != -1 | computer_y != -1)
                            board_value[computer_x][computer_y] = 1;
                        int flag;//判断是否有空格，0没有，1有
                        int isWin_c, isWin_p;//判断是否赢棋，1电脑赢，-1玩家赢
                        player_play(i, j);
                        isWin_p = is_win(board_value);
                        if (isWin_p == -1) {
                            JOptionPane.showMessageDialog(frame, "玩家赢");
                            break;
                        }
                        //判断难度
                        if(depth == 2 || depth == 4)
                            computer_play(board_value);
                        else
                            computer_play_normal(board_value);
                        isWin_c = is_win(board_value);
                        if (isWin_c == 1) {
                            JOptionPane.showMessageDialog(frame, "电脑赢");
                            break;
                        }
                        flag = is_null(board_value);
                        if (flag == 0) {
                            JOptionPane.showMessageDialog(frame, "平局");
                            break;
                        }
                    }
                }
            }
        }
    }

    //主函数
    public static void main(String[] args){
        Jing_Chessboard Saolei = new Jing_Chessboard();
    }
}
