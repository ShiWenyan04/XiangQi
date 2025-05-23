package chess;

import javax.swing.*;
import javax.swing.filechooser.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Stack;

public class ChineseChess
{
  static ChineseChess app;
  JFrame mainFrame;
  ChineseChessCtrl control;
  public JMenuItem menuRevert;
  public JMenuItem menuRedo;

  public ChineseChess()
  {
    mainFrame = new JFrame("中国象棋");
    mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    control = new ChineseChessCtrl(mainFrame);
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("文件");
    KeyStroke ctrl_n = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
    JMenuItem item = new JMenuItem("新的游戏");
    item.setActionCommand("NEW");
    item.setAccelerator(ctrl_n);
    item.addActionListener(control);
    menu.add(item);
    
    KeyStroke shift_n = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.SHIFT_MASK);
    item = new JMenuItem("加载游戏");
    item.setActionCommand("LOAD");
    item.setAccelerator(shift_n);
    item.addActionListener(control);
    menu.add(item);
    
    menu.addSeparator();
    KeyStroke ctrl_s = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
    item = new JMenuItem("保存游戏");
    item.setActionCommand("SAVE");
    item.setAccelerator(ctrl_s);
    item.addActionListener(control);
    menu.add(item);
    
    
    menu.addSeparator();
    KeyStroke ctrl_q = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK);
    item = new JMenuItem("退出");
    item.setActionCommand("EXIT");
    item.setAccelerator(ctrl_q);
    item.addActionListener(control);
    menu.add(item);
    
    menuBar.add(menu);
    
    menu = new JMenu("操作");
    KeyStroke ctrl_z = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
    menuRevert = new JMenuItem("悔棋");
    menuRevert.setActionCommand("REVERT");
    menuRevert.setEnabled(false);
    menuRevert.setAccelerator(ctrl_z);
    menuRevert.addActionListener(control);
    menu.add(menuRevert);
    menu.addSeparator();
    
    KeyStroke ctrl_y = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK);
    menuRedo = new JMenuItem("重做");
    menuRedo.setActionCommand("REDO");
    menuRedo.setEnabled(false);
    menuRedo.setAccelerator(ctrl_y);
    menuRedo.addActionListener(control);
    menu.add(menuRedo);
    
    menuBar.add(menu);
    mainFrame.setJMenuBar(menuBar);

    Game game = new Game();
    mainFrame.getContentPane().setLayout(new GridBagLayout());
    BoardView.defaultBoardView().setGame(game);

    GridBagConstraints constrs = new GridBagConstraints();
    
    mainFrame.add(BoardView.defaultBoardView(), constrs);

    constrs.fill = GridBagConstraints.BOTH;
    constrs.gridwidth = constrs.gridheight = GridBagConstraints.REMAINDER;
    constrs.weighty = 1.0;
    mainFrame.setSize(320, 400);
    mainFrame.setLocationRelativeTo(null);
    mainFrame.setMinimumSize(new Dimension(320,400));

    mainFrame.addWindowListener(control);
  }
  static public ChineseChess runningApplication()
  {
    if (app == null) app = new ChineseChess();
    return app;
  }

  private void start()
  {
      mainFrame.setVisible(true);
  }

  public static void main(String [] args)
  {
    ChineseChess.runningApplication().start();
  }
}

class ChineseChessCtrl extends WindowAdapter implements ActionListener
{
  JFrame mainFrame;
  public ChineseChessCtrl(JFrame frame) { mainFrame = frame; }
  @Override public void actionPerformed(ActionEvent event)
  {
    if (event.getActionCommand().compareTo("NEW") == 0)
      {
        Game game = new Game();
        BoardView.defaultBoardView().setGame(game);
        BoardView.defaultBoardView().setColor('r');
      }
    if (event.getActionCommand().compareTo("REVERT") == 0) {
        BoardView.defaultBoardView().game.revert();
    }
    if (event.getActionCommand().compareTo("REDO") == 0) {
        BoardView.defaultBoardView().game.redo();
    }
    if (event.getActionCommand().compareTo("SAVE") == 0) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter( new FileNameExtensionFilter("Chinese Chess File (*.chess)", "chess") );
        chooser.setDialogTitle("Save the Game to...");
        chooser.setDialogType(JFileChooser.SAVE_DIALOG | JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.showDialog(null, null);
        File target = chooser.getSelectedFile();
        String path = target.getAbsolutePath();
        if ( !path.toLowerCase().endsWith(".chess") ) {
            path+=".chess";
            target = new File(path);
        }
        if (target.exists() &&
                JOptionPane.showConfirmDialog(mainFrame, 
                target.getName()+" already exists, overwrite?", "Overwrite Confirm", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) !=0 )
            return;
        try {
            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream serialization = new ObjectOutputStream(out);
            int redoTimes = 0;
            for (; BoardView.defaultBoardView().game.redo(); redoTimes++){}
            serialization.writeInt(redoTimes);
            while(BoardView.defaultBoardView().game.revert()){}
            serialization.writeObject(BoardView.defaultBoardView().game.redo);
            serialization.close(); out.close();

            // 还原格局    
            while(BoardView.defaultBoardView().game.redo()){}
            for (; redoTimes>0; BoardView.defaultBoardView().game.revert(), redoTimes--){}
            
            JOptionPane.showMessageDialog(mainFrame, "Successfully saved \""+path+'"');
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(mainFrame, 
                    "Failure occurred while saving the game!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    if (event.getActionCommand().compareTo("LOAD") == 0) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter( new FileNameExtensionFilter("Chinese Chess File (*.chess)", "chess") );
        chooser.setDialogTitle("Load Game...");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG | JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.showDialog(null, null);
        File target = chooser.getSelectedFile();
        String path = target.getAbsolutePath();
        try {
            FileInputStream in = new FileInputStream(path);
            ObjectInputStream deserialization = new ObjectInputStream(in);
            int redoTimes = deserialization.readInt();
            Stack<StepRecord> records = (Stack<StepRecord>)deserialization.readObject();
            deserialization.close(); in.close();
            
            Game game = new Game();
            BoardView.defaultBoardView().setGame(game);
            BoardView.defaultBoardView().setColor('r');
            while(!records.empty()) {
                game.move(records.pop().moveText());
            }
            for (; redoTimes>0; game.revert(), redoTimes--){}
            JOptionPane.showMessageDialog(mainFrame, "Successfully Open \""+path+'"');
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(mainFrame, 
                    "Failure occurred while loading the game!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    if (event.getActionCommand().compareTo("EXIT") == 0) {
        WindowEvent e = new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING);
        windowClosing(e);
    }
  }
  @Override public void windowClosing(WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING) {
            //处理Jframe关闭事件
            int choice = JOptionPane.showConfirmDialog(mainFrame, 
              "确定退出", "真的要退出游戏吗？", 
              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if ( choice == 0 ) System.exit(0);
        }else{
            //忽略其他事件，交给JFrame处理
            super.windowClosing(event);
        }
    }
}
