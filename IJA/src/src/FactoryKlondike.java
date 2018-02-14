/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;
import java.io.Serializable;
/**
 *
 * @author Martin
 */
import src.MyCard;
import src.Card;
import src.CardDeck;
import src.MyCardDeck;
import src.CardStack;
public class FactoryKlondike extends AbstractFactorySolitaire implements Serializable
{
	public Card createCard(Card.Color color, int value)
	{
		MyCard c;
		if (value <= 0 || value > 14)
			c = null;
		else
		    c = new MyCard(color, value);
	   return c;
	}
	public CardDeck createCardDeck()
	{
		MyCardDeck c;
		c = new MyCardDeck(0, 0);
		return c;
	}
   public CardDeck createTargetPack(Card.Color color)
   {
   	  MyCardDeck c;
   	  c = new MyCardDeck(1,0);
   	  c.target = color;
   	  return c;
   }
   public CardStack createWorkingPack()
   {
   	  CardStack c = new MyCardDeck(1,1);
   	  return c;
   }
} 
