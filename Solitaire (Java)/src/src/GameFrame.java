/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

/**
 *
 * @author Martin
 */
/**
 *
 * @author Jan Gula[xgulaj00@stud.fit.vutbr.cz]
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class GameFrame extends JComponent implements MouseListener{
    public static int activeInstances;
     Card c;   
     private static final int CARD_WIDTH = 73;
     private static final int CARD_HEIGHT = 97;
     private static final int SPACING = 5;  //distance between cards
     private static final int FACE_UP_OFFSET = 15;  //distance for cascading face-up cards
     private static final int FACE_DOWN_OFFSET = 5; 
     public Game gameInstance;
     public int WindowNumber;
     public JFrame gameContainer;
     public int rowPos = -1;
    public int colPos = -1;
    private boolean hintptp = false; 
    private boolean hintptf = false;
    private boolean hintwtp = false;
    private boolean hintwastetof = false;
    private boolean hints = false;
    private boolean hint = false;
    private boolean pt = false;
    private boolean tp = false;
    private boolean nohint = false;
    private boolean winc = false;
    public GameFrame(Game Solitaire, int number) {
        activeInstances++;
        this.gameInstance = Solitaire;
        WindowNumber = number;
        gameContainer = new JFrame("Solitaire");
        JPanel mainPanel = new JPanel();
        if (WindowNumber == 1)
        {
           
            
            gameContainer.setSize(700,400);
            gameContainer.setLocation(0,0);
            
        }
        else if (WindowNumber == 2)
        {
            gameContainer.setSize(700,400);
            gameContainer.setLocation(680,0);
        }
         else if (WindowNumber == 3)
        {
            gameContainer.setSize(700,400);
            gameContainer.setLocation(0,390);
        }
         else if (WindowNumber == 4)
        {
            gameContainer.setSize(700,400);
            gameContainer.setLocation(680,390);
        }
        
         gameContainer.getContentPane().setBackground(Color.green);
        gameContainer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JMenuBar gameMenu = new JMenuBar();
        gameContainer.setJMenuBar(gameMenu);
        JMenuItem gameOptions = new JMenu("Options");
        JMenuItem newGame = new JMenuItem("New");
        JMenuItem saveGame = new JMenuItem("Save");
        JMenuItem loadGame = new JMenuItem("Load");
        JMenuItem help = new JMenuItem("Help");
        JMenuItem undoLast = new JMenuItem("Undo");
        JMenuItem exitGame = new JMenuItem("Exit");
        gameMenu.add(gameOptions);
        gameOptions.add(newGame);
        gameOptions.add(saveGame);
        gameOptions.add(loadGame);
        gameOptions.add(help);
        gameOptions.add(undoLast);
        gameOptions.add(exitGame);
        
        gameContainer.getContentPane().add(this);
        this.addMouseListener(this);
        gameContainer.setVisible(true);
        newGame.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            new_game_perfomed(evt);
        }
    });
        saveGame.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            save_game_perfomed(evt);
        }
    });
         loadGame.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt){
         
         load_game_perfomed(evt);
            
        }
    });
         undoLast.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt){
         
         undo_last_perfomed(evt);
            
        }
    });
         exitGame.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            exit_game_perfomed(evt);
        }
    });
         help.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent ev){
            if ((gameInstance.deck.isEmpty()) && (gameInstance.waste.size() == 0)){
                
                if (gameInstance.possiblePiletoFoundation()){
                    
                    hintptf = true;
                }
                else if (gameInstance.possiblePileToPile()){
                    hintptp = true;
                    
                } else {
                    nohint = true;
                }
            }
            else if(gameInstance.deck.size() == 24){
                
                if (gameInstance.possiblePiletoFoundation()){
                    hintptf = true;
                    
                }
                else if (gameInstance.possiblePileToPile()){
                    hintptp = true;
                    
                }
                else{
                    hints = true;
                   
                }
            }
            else if(gameInstance.waste.size() != 0){
                
                if (gameInstance.possiblePiletoFoundation()){
                    hintptf = true;
                    
                }
                else if (gameInstance.possiblePileToPile()){
                    hintptp = true;
                    
                }
                else if(gameInstance.possibleWasteToFoundation()){
                    hintwastetof = true;
                    
                }
                else if(gameInstance.possibleWasteToPile()){
                    hintwtp = true;
                    
                }
                else{
                    hints = true;
                    
                }
            } else if ((!gameInstance.deck.isEmpty()) && (gameInstance.waste.size() == 0)){
                
                if (gameInstance.possiblePiletoFoundation()){
                  
                    hintptf = true;
                }
                else if (gameInstance.possiblePileToPile()){
                    hintptp = true;
                    
                } else {
                    hints = true;
                }
                
            }
            hint = true;
            if (nohint){
                JOptionPane.showMessageDialog(gameContainer,"No more moves found, game over.", "Hint", JOptionPane.INFORMATION_MESSAGE);
                nohint = false;
            }
            if (hints == true){
                JOptionPane.showMessageDialog(gameContainer,"Click stock, no moves found.", "Hint", JOptionPane.INFORMATION_MESSAGE);
                hints = false;
            }
            if (hintwtp == true){
                JOptionPane.showMessageDialog(gameContainer,"Waste ->  Pile " + (gameInstance.auxf + 1) + ".", "Hint", JOptionPane.INFORMATION_MESSAGE);
                hintwtp = false;
            }
            if (hintwastetof){
                JOptionPane.showMessageDialog(gameContainer,"Waste -> Foundation " + (gameInstance.auxe + 1) + ".", "Hint", JOptionPane.INFORMATION_MESSAGE);
                hintwastetof = false;
            }
            if (hintptp){
                JOptionPane.showMessageDialog(gameContainer,"Pile " + (gameInstance.auxa + 1) + ". -> Pile " + (gameInstance.auxb + 1) + ".", "Hint", JOptionPane.INFORMATION_MESSAGE);
                hintptp = false;
            }
            if (hintptf){
                JOptionPane.showMessageDialog(gameContainer,"Pile " + (gameInstance.auxc + 1) + ". -> Foundation " + (gameInstance.auxd + 1) + "", "Hint", JOptionPane.INFORMATION_MESSAGE);
                hintptf = false;
            }
            
        }});
    }
    
        public void new_game_perfomed(java.awt.event.ActionEvent evt) {
         
         new Game(gameInstance.GameNumber + 1);
         gameInstance.GameNumber = gameInstance.GameNumber + 1;
         
}
       
       public void save_game_perfomed(java.awt.event.ActionEvent evt)
       {
         gameInstance.save_game();
             
       }
        public void load_game_perfomed(java.awt.event.ActionEvent evt) 
        {
          try
          {
            
          gameInstance.load_game();
          
        }
         catch(NoClassDefFoundError e) {
          JOptionPane.showMessageDialog(null, e);
}       catch (ClassNotFoundException ex) {
            Logger.getLogger(GameFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        public void undo_last_perfomed(java.awt.event.ActionEvent evt)
       {
         gameInstance.do_undo();
             
       }
        public void exit_game_perfomed(java.awt.event.ActionEvent evt)
       {
         gameInstance.exit_game();
             
       }        
      @Override
      public void paintComponent(Graphics g)
	{
	        
		g.setColor(new Color(0, 128, 0));
                g.fillRect(0, 0, getWidth(), getHeight());
                drawCard(g, gameInstance.deck.get(), SPACING, SPACING);
                drawCard(g, gameInstance.waste.get(), SPACING * 2 + CARD_WIDTH, SPACING);
                if (rowPos == 0 && colPos == 1)
                    drawBorder(g, SPACING * 2 + CARD_WIDTH, SPACING);
                for (int i = 0; i < 4; i++)
            drawCard(g, gameInstance.targetStack[i].get(), SPACING * (4 + i) + CARD_WIDTH * (3 + i), SPACING);
                for (int i = 0; i < 7; i++) {
                    int offset = 0;
                    for (int j =0;j<gameInstance.workingStack[i].size();j++)
                    {
                      drawCard(g, gameInstance.workingStack[i].get(j), SPACING + (CARD_WIDTH + SPACING) * i, CARD_HEIGHT + 2 * SPACING + offset);
                                      if (rowPos == 1 && colPos == i && j == gameInstance.workingStack[i].size() - 1)
					drawBorder(g, SPACING + (CARD_WIDTH + SPACING) * i, CARD_HEIGHT + 2 * SPACING + offset);
                      if (gameInstance.workingStack[i].get(j).isTurnedFaceUp())
					offset += FACE_UP_OFFSET;
				else
					offset += FACE_DOWN_OFFSET;
                   }
                    }
                if(winc){            
            String filename = "./lib/win.png";
            Image grats = new ImageIcon(filename).getImage();
            g.drawImage(grats,90,70,null);
	}
        }
      public void drawCard(Graphics g, Card c, int x, int y)
      {
          if (c == null)
          {
                g.setColor(Color.BLACK);
		g.drawRect(x, y , CARD_WIDTH, CARD_HEIGHT);
          }
          else
          {
              
              String cardName = c.cardPath();
              Image image;
              image = new ImageIcon(cardName).getImage();
              g.drawImage(image, x, y, CARD_WIDTH, CARD_HEIGHT, null);
              
          }
      }
                public void mousePressed(MouseEvent e){}
                public void mouseEntered(MouseEvent e){}
                public void mouseExited(MouseEvent e){}
                public void mouseReleased(MouseEvent e){}
                
    public void mouseClicked(MouseEvent Mevent){
        int mouseCol = Mevent.getX() / (SPACING + CARD_WIDTH);
        int mouseRow = Mevent.getY() / (SPACING + CARD_HEIGHT);
        if (mouseRow > 1)
            mouseRow = 1;
        if (mouseCol > 6)
            mouseCol = 6;

        if (mouseRow == 0 && mouseCol == 0)
            gameInstance.deckClicked();
        else if (mouseRow == 0 && mouseCol == 1)
            gameInstance.wasteClicked();
        else if (mouseRow == 0 && mouseCol >= 3)
           gameInstance.targetClicked(mouseCol - 3);
        else if (mouseRow == 1)
        gameInstance.workingClicked(mouseCol);
        winc = gameInstance.wincondition();
        repaint();    
    }
    
    public boolean isWasteClicked() {
        return (rowPos == 0 && colPos == 1);
    }
    
    public boolean isWorkingClicked() {
        return rowPos == 1;
    }
    
    public void selectWorking(int i) {
        rowPos = 1;
        colPos = i;
    }
    public void selectWaste() {
        rowPos = 0;
        colPos = 1;
    }
    public int clickedWorking() {
        if(rowPos == 1)
            return colPos;
        else
            return -1;
    }
    public void resetPositions() {
        rowPos = -1;
        colPos = -1;
    }
    public void drawBorder(Graphics g, int x, int y) {
	g.setColor(Color.YELLOW);
	g.drawRect(x, y, CARD_WIDTH, CARD_HEIGHT);
	g.drawRect(x + 1, y + 1, CARD_WIDTH - 2, CARD_HEIGHT - 2);
	g.drawRect(x + 2, y + 2, CARD_WIDTH - 4, CARD_HEIGHT - 4);
    }
    
    public void addActionListener() {
            
    }

   
}