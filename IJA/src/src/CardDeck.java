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
import  src.MyCard;
import  src.MyCardDeck;
public interface CardDeck
{
	//public Card get();
	//public Card get(int index);
	//public boolean isEmpty();
	//public Card pop();
	public int size();
	public boolean put(Card card);
        public boolean put_work(Card card);
	public Card pop();
	public Card get();
	public Card get(int index);
	public boolean isEmpty();
        public boolean put2(CardStack stack);
        public void push(Card c);
}