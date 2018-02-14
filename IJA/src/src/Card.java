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
public interface Card
{
    public static enum Color
    {
       CLUBS ("C"),
       DIAMONDS ("D"),
       HEARTS ("H"),
       SPADES ("S");
       String string; 
       Color(final String in_txt)
       {
       this.string = in_txt;
       }
       @Override
       public String toString()
       {
          return this.string;
       }
      
    }
   public Card.Color color();
   public int value();
   public boolean isTurnedFaceUp();
   public boolean turnFaceUp();
   public boolean similarColorTo(Card c);
   public int compareValue(Card c); 
    @Override
    public String toString();
    @Override
    public int hashCode();
    @Override
    public boolean equals(Object o);
    public String cardPath();
    public boolean turnFaceDown();

  
    
}