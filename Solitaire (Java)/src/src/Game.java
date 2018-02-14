/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.JFileChooser;
import java.beans.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
/**
 *
 * @author Martin
 */

public class Game  {
    public CardDeck deck;
    public CardDeck waste;
    public CardDeck deck2;
    public CardDeck[] targetStack;
    public CardDeck[] workingStack;
    public GameFrame gameGUI;
    public AbstractFactorySolitaire factory;
    Random rand;
    public int[] undo_from;
    public int[] undo_from_type;
    public int[] undo_to;
    public int[] undo_to_type;
    public CardDeck[] undo_deck;
    public int undo_size;
    public int GameNumber;
    public int auxa, auxb, auxc, auxd, auxe, auxf;
    
    
     public static JFileChooser choose;
    public static void main(String[] args)
	{ 
          choose = new JFileChooser();
          choose.setCurrentDirectory(new File("."));    
          new Game(1);
	}
    
    public Game(int number)
            {
                factory = new FactoryKlondike();
                targetStack = new MyCardDeck[4];
                workingStack = new MyCardDeck[7];
                GameNumber = number;
                gameGUI = new GameFrame(this, number);
                undo_size = -1;
                undo_from = new int[10000];
                undo_to = new int[10000];
                undo_from_type = new int[10000];
                undo_to_type = new int[10000];
                undo_deck = new CardDeck[100000];
                int i = 0;
                for (Card.Color c : Card.Color.values())
                {
                  targetStack[i] = factory.createTargetPack(c);
                  i++;
                }
                for (i = 0; i<7;i++)
                {
                    workingStack[i] = factory.createWorkingPack();
                }
                deck2 = factory.createCardDeck();
                waste = factory.createCardDeck();
                deck = factory.createCardDeck();
                while (waste.pop() != null)
                {
                    
                }
                while (deck.pop() != null)
                {
                    
                }
                 ArrayList<Integer> list = new ArrayList<Integer>();
               
                  rand = new Random();
                  
                while (deck.size() != 52)
                {
                   
                     while (list.size() < 52)
                     {
                       int index = rand.nextInt(52);
                       if (!list.contains(index))
                       {
                       deck.put_work(deck2.get(index));
                       list.add(index);
                    }
                     }
                }     
                  
                
                for (i = 0; i < 7; i++)
                {
                    int j = 0;
                    while (j != i + 1)
                    {
                     Card k = deck.pop();
             
                
                     workingStack[i].put_work(k);
                        j++;
                    }
            Card karta = workingStack[i].pop();
            karta.turnFaceUp();
            workingStack[i].put_work(karta);
            
        }
    }
            
    
 public void deckClicked() {
        gameGUI.resetPositions();
        if (!gameGUI.isWasteClicked() && ! gameGUI.isWorkingClicked())
        {
            if (deck.isEmpty()) deckReset();
            else showThree(); 
        }
    }
    
    
    public void workingClicked(int i) {
        if(gameGUI.isWasteClicked()) {
            Card wasteTop = waste.get();
            if (addToWorking(wasteTop, i))
            {
                undo_size = undo_size + 1;
                workingStack[i].put(waste.pop());
                workingStack[i].get().turnFaceUp();
                undo_from_type[undo_size] = 2;
                undo_to_type[undo_size] = 3;
                undo_from[undo_size] = i;
                undo_to[undo_size] = i;
               
            }
            gameGUI.resetPositions();
            gameGUI.selectWorking(i);
            
        } else if(gameGUI.isWorkingClicked()){
            int oldWorking = gameGUI.clickedWorking();
            if(oldWorking != i) {
                CardDeck aux = takeAll(oldWorking);
                if(addToWorking(aux.get(), i)) {
                     undo_size = undo_size + 1;
                     undo_from_type[undo_size] = 5;
                     undo_from[undo_size] = oldWorking;
                     undo_to_type[undo_size] = 3;
                     undo_to[undo_size] = i;
                     workingAdd(aux, i, 1);
                    //adtopile
                    if(!workingStack[oldWorking].isEmpty()) 
                        workingStack[oldWorking].get().turnFaceUp();
                    gameGUI.resetPositions();
                } else {
                    workingAdd(aux, oldWorking, 0);
                    gameGUI.resetPositions();
                    gameGUI.selectWorking(i);
                }
            } else gameGUI.resetPositions();
        } else {
            gameGUI.selectWorking(i);
            workingStack[i].get().turnFaceUp();
        }
        
    }
    
    public boolean addToWorking(Card c, int i) {
        CardDeck workingPack = workingStack[i];
        if(workingPack.isEmpty()) 
            return c.value() == 13;
        Card top = workingPack.get();
        if(!top.isTurnedFaceUp()) 
            return false;
        return(!c.similarColorTo(top) && c.value() == top.value()-1);
    }
    
    public void workingAdd(CardDeck cardsToAdd, int i, int j)
    {
        if (j == 1)
            {
            undo_deck[undo_size] = factory.createCardDeck();
            while (undo_deck[undo_size].size () != 0)
            {
                undo_deck[undo_size].pop();
            }
            }
        while (! cardsToAdd.isEmpty())
        {
           
            workingStack[i].put_work(cardsToAdd.pop());
            if (j == 1)
            {
            undo_deck[undo_size].put_work(workingStack[i].get());
            
            }
            
        }
    }
    
    public CardDeck takeAll(int i) {
        CardDeck faceUpCards = new MyCardDeck(1,1);
        while(!workingStack[i].isEmpty() && workingStack[i].get().isTurnedFaceUp())
        {
         
            Card k = workingStack[i].pop();
            faceUpCards.put_work(k);
        }    
        return faceUpCards;
    }
    public void deckReset()
    {
        undo_size = undo_size + 1;
        undo_from_type[undo_size] = 2;
        undo_to_type[undo_size] = 1;
        undo_deck[undo_size] = factory.createCardDeck();
        while (! undo_deck[undo_size].isEmpty())
        {
            undo_deck[undo_size].pop();
        }
        while (! waste.isEmpty())
        {
            Card aux = waste.pop();
            aux.turnFaceDown();
            deck.put_work(aux);
           
        }
    } 
    public void showThree() {
            if(!deck.isEmpty()) {
                Card aux = deck.pop();
                waste.put_work(aux);
                aux.turnFaceUp();
                undo_size = undo_size + 1;
                undo_from_type[undo_size] = 1;
                undo_to_type[undo_size] = 2;
                undo_to[undo_size] = 0;
                undo_from[undo_size] = 0;
            }
        }
    
    public void wasteClicked() {
        if (! waste.isEmpty()) {
            if (! gameGUI.isWasteClicked()) 
                gameGUI.selectWaste();
            else gameGUI.resetPositions();
        }
    }
   public void targetClicked(int i) {
        if(gameGUI.isWasteClicked()) {
            if(targetPackAdd(waste.get(), i)) {
                undo_size = undo_size + 1;
                undo_from_type[undo_size] = 2;
                undo_to_type[undo_size] = 4;
                undo_to[undo_size] = i;
                undo_from[undo_size] = i;
                Card aux = waste.pop();
                targetStack[i].put_work(aux);
               
                
                }
                gameGUI.resetPositions();
            }
        
        if(gameGUI.isWorkingClicked()) {
   
            CardDeck chosenTarget = workingStack[gameGUI.clickedWorking()];
            if(targetPackAdd(chosenTarget.get(),i)) {
                undo_size = undo_size + 1;
                undo_from_type[undo_size] = 3;
                undo_to_type[undo_size] = 4;
                undo_from[undo_size] = gameGUI.clickedWorking();
                undo_to[undo_size] = i;
                Card aux = chosenTarget.pop();
                targetStack[i].put_work(aux);
                gameGUI.resetPositions();
            }
        }
         
    }
    
    public boolean targetPackAdd(Card c, int i) {
        if(targetStack[i].isEmpty())
            return(c.value() == 1);
        Card aux = targetStack[i].get();
        return ((aux.value() + 1 == c.value()) && (aux.color().equals(c.color())));
    } 

public void save_game()
{
 if(choose.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
    {
        try{
            FileOutputStream file = new FileOutputStream(choose.getSelectedFile());
            ObjectOutputStream object = new ObjectOutputStream(file);
            object.writeObject(deck);
            object.writeObject(waste);
            object.writeObject(factory);
            object.writeObject(targetStack);
            object.writeObject(workingStack);
           
           
        }
        catch(Exception ex)
        {
           ex.printStackTrace();
        }
       
    }
}
public void load_game() throws ClassNotFoundException
{
     int r = choose.showOpenDialog(null);
    if(r == JFileChooser.APPROVE_OPTION)
    {
        try
        {
          File file = choose.getSelectedFile();
    FileInputStream fileStream = new FileInputStream(file);   
    ObjectInputStream objectStream = new ObjectInputStream(fileStream); 
    this.deck = (CardDeck) objectStream.readObject();
    this.waste = (CardDeck) objectStream.readObject();
    this.factory = (AbstractFactorySolitaire) objectStream.readObject();
    this.targetStack = (CardDeck[]) objectStream.readObject();
    this.workingStack = (CardDeck[]) objectStream.readObject();
    gameGUI.gameContainer.dispose();
    this.gameGUI = new GameFrame(this, GameNumber);
        
    }
        catch(IOException e)
                {
                    JOptionPane.showMessageDialog(null, e);
                }
    }
}
public void new_g(){
    
    
}
public void do_undo(){
    if (this.undo_size == -1)
    {
       return;
    }
            
    if (this.undo_from_type[this.undo_size] == 2 && this.undo_to_type[this.undo_size] == 3)
    {
        this.waste.put_work(this.workingStack[this.undo_from[this.undo_size]].pop());
        this.waste.get().turnFaceUp();
        this.undo_size = this.undo_size - 1;
        gameGUI.gameContainer.dispose();
        this.gameGUI = new GameFrame(this, GameNumber);
        
    }
    else if (this.undo_from_type[this.undo_size] == 5 && this.undo_to_type[this.undo_size] == 3)
    {
        int popping = this.undo_deck[this.undo_size].size();
        if (!(workingStack[this.undo_from[this.undo_size]].isEmpty()))
                {
        workingStack[this.undo_from[this.undo_size]].get().turnFaceDown();
                }
        CardDeck pom = new MyCardDeck(1,1);
        while (! undo_deck[this.undo_size].isEmpty())
        {
            pom.put_work(undo_deck[undo_size].pop());
        }
        while (! pom.isEmpty())
        {
            workingStack[undo_from[undo_size]].put_work(pom.pop());
        }
        while (popping != 0)
        {
            
            workingStack[this.undo_to[this.undo_size]].pop();
            popping = popping - 1;
        }
        this.undo_size = this.undo_size - 1;
        gameGUI.gameContainer.dispose();
        this.gameGUI = new GameFrame(this, GameNumber);
    }
    else if (this.undo_from_type[this.undo_size] == 3 && this.undo_to_type[this.undo_size] == 4)
    {
        if (! workingStack[undo_from[undo_size]].isEmpty())
        {
        workingStack[undo_from[undo_size]].get().turnFaceDown();
        }
        workingStack[undo_from[undo_size]].put_work(targetStack[undo_to[undo_size]].pop());
        undo_size = undo_size - 1;
        gameGUI.gameContainer.dispose();
        this.gameGUI = new GameFrame(this, GameNumber);
        
        
    }
    else if (this.undo_from_type[this.undo_size] == 2 && this.undo_to_type[this.undo_size] == 4)
    {
        waste.put_work(targetStack[undo_to[undo_size]].pop());
        undo_size = undo_size - 1;
        gameGUI.gameContainer.dispose();
        this.gameGUI = new GameFrame(this, GameNumber);
        
        
    }
    else if (this.undo_from_type[this.undo_size] == 1 && this.undo_to_type[this.undo_size] == 2)
    {
        deck.put_work(waste.pop());
        deck.get().turnFaceDown();
        undo_size = undo_size - 1;
        gameGUI.gameContainer.dispose();
        this.gameGUI = new GameFrame(this,GameNumber );
    }
     else if (this.undo_from_type[this.undo_size] == 2 && this.undo_to_type[this.undo_size] == 1)
    {
         while (! deck.isEmpty())
        {
            Card ax = deck.pop();
            ax.turnFaceUp();
            waste.put_work(ax);
        }
        undo_size = undo_size - 1;
        gameGUI.gameContainer.dispose();
        this.gameGUI = new GameFrame(this, GameNumber);
    }
}
public void exit_game()
{
    gameGUI.gameContainer.dispose();
}
public boolean wincondition(){
        if ((!deck.isEmpty()) && (waste.size() != 0)){
            
            return false;
        }
        for (int i = 0; i < 7; i++){
            if (!workingStack[i].isEmpty()){
           
                return false;
            }
        }
        for (int i = 0; i < 4; i++){
            if (targetStack[i].size() != 13){
           
                return false;
            }
        }
        return true;
    }
    
    public boolean possiblePiletoFoundation(){
        int i,j;
        for(i = 0; i < 7; i++){
            if (workingStack[i].isEmpty())
                continue;
            Card topOfPile = workingStack[i].get();
            for (j = 0; j < 4; j++){
                if (targetStack[j].put(topOfPile)){
                    targetStack[j].pop();
                    auxc = i;
                    auxd = j;
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean cycle(int m, int l, int n, Card cardcycle){
        int i;
        for(i = l; i < n; i++){
            if(workingStack[i].put(cardcycle)){
                workingStack[i].pop();
                auxa = m;
                auxb = i;
                return true;
            }
        }
        return false;
    }

    public boolean possiblePileToPile(){
        int i,j,k;
        for(i = 0; i < 7; i++){
            if (workingStack[i].isEmpty())
                continue;
            for (j = workingStack[i].size() - 1; j >= 0; j--){
                if (!workingStack[i].get(j).isTurnedFaceUp())
                    break;
            }
            j++;
            Card cardworkingStack = workingStack[i].get(j);
            switch(i){
                case 0:
                    if(cycle(i, 1, 7, cardworkingStack))
                        return true;
                    break;
                case 1:
                    if(cycle(i, 0, 1, cardworkingStack))
                        return true;
                    if(cycle(i, 2, 7, cardworkingStack))
                        return true;
                    break;
                case 2:
                    if(cycle(i, 0, 2, cardworkingStack))
                        return true;
                    if(cycle(i, 3, 7 ,cardworkingStack))
                        return true;
                    break;
                case 3:
                    if(cycle(i, 0, 3, cardworkingStack))
                        return true;
                    if(cycle(i, 4, 7, cardworkingStack))
                        return true;
                    break;
                case 4:
                    if(cycle(i, 0, 4, cardworkingStack))
                        return true;
                    if(cycle(i, 5, 7, cardworkingStack))
                        return true;
                    break;
                case 5:
                    if(cycle(i, 0, 5, cardworkingStack))
                        return true;
                    if(cycle(i, 6, 7, cardworkingStack))
                        return true;
                    break;
                case 6:
                    if(cycle(i, 0, 6, cardworkingStack))
                        return true;
                    break;
            }
        }
        return false;
    }
    
    public boolean possibleWasteToFoundation(){
        int i;
        for (i = 0; i < 4; i++){
            if (targetStack[i].put(waste.get())){
                targetStack[i].pop();
                auxe = i;
                return true;
            }
        }
        return false;
    }

    public boolean possibleWasteToPile(){
        int i;
        for (i = 0; i < 7; i++){
            if (workingStack[i].put(waste.get())){
                workingStack[i].pop();
                auxf = i;
                return true;
            }
        }
        return false;
    }
}


        
    


