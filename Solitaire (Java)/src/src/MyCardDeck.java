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
    public final class MyCardDeck implements CardDeck, CardStack, Serializable
{
	int size;
	int stacksize;
	int work;
	public Card.Color target;
        Card Card_Array[];
	public MyCardDeck(int empty, int working)
	{
		this.size = 0;
		this.stacksize = -1;
		this.target = null;
        this.work = 0;
		this.Card_Array = new Card[52];
		if (empty != 1)
		{
		for (Card.Color c : Card.Color.values())
		{
			for (int i = 0; i<13;i++)
			{
                 Card karta = new MyCard(c, i+1);
                 this.put(karta);
                  
			}
		}
	   }
	   if (working != 0)
	   {
	   	 this.work = 1;
	   }
	}
	
	public int size()
	{
		return this.size;
	}
	public void push(Card c) {
            Card_Array[this.size()] = (Card) c;
        }
	public boolean put(Card card)
	{
		if (card == null)
			return false;
		else if ((this.work == 1) && (card.isTurnedFaceUp() == true))
		{
		    if ((this.size() == 0))
		{
			    this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;
			
			
		}
		    else if ((this.Card_Array[this.stacksize].color().toString().equals("H")) || (this.Card_Array[this.stacksize].color().toString().equals("D")))
		    {
		    	if ((card.color().toString().equals("D")) || (card.color().toString().equals("H")))
		    		return false;
		    	else if (card.value() != (this.Card_Array[this.stacksize].value() - 1))
		    	    return false;
		    	else
		    	{
		    	this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;
		    	}    	
		    }
		    else if  ((this.Card_Array[this.stacksize].color().toString().equals("S")) || (this.Card_Array[this.stacksize].color().toString().equals("C")))
		    {
		    	if ((card.color().toString().equals("S")) || (card.color().toString().equals("C")))
		    		return false;
		    	else if (card.value() != (this.Card_Array[this.stacksize].value() - 1))
		    	    return false;
		    	else
		    	{
		    	this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;
		    	}    	
		    } 
		  return false;
		}

		else if ((this.target != null) && (card.isTurnedFaceUp() == true))
		{
			if (card.color().toString().equals(this.target.toString()))
			{
				if ((this.size() > 0) && (this.Card_Array[this.stacksize].value() != card.value() - 1))
					return false;
				if ((this.size() == 0) && (card.value() != 1))
				return false;
				this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;
			}
			else if ((card.color().toString().equals("S")) && (this.target.toString().equals("C")))
			{
				if ((this.size() > 0) && (this.Card_Array[this.stacksize].value() != card.value() - 1))
					return false;
				if ((this.size() == 0) && (card.value() != 1))
				return false;
				this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;
			}
			else if ((card.color().toString().equals("C")) && (this.target.toString().equals("S")))
			{
				if ((this.size() > 0) && (this.Card_Array[this.stacksize].value() != card.value() - 1))
					return false;
				if ((this.size() == 0) && (card.value() != 1))
				return false;
				this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;	
			}
			else if ((card.color().toString().equals("D")) && (this.target.toString().equals("H")))
			{
				if ((this.size() > 0) && (this.Card_Array[this.stacksize].value() != card.value() - 1))
					return false;
				if ((this.size() == 0) && (card.value() != 1))
				return false;
				this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;	
			}
			else if ((card.color().toString().equals("H")) && (this.target.toString().equals("D")))
		    {
		    	if ((this.size() > 0) && (this.Card_Array[this.stacksize].value() != card.value() - 1))
					return false;
				if ((this.size() == 0) && (card.value() != 1))
				return false;
		    	this.size = this.size + 1;
				this.stacksize = this.stacksize + 1;
				this.Card_Array[this.stacksize] = card;
			    return true;	
		    }
			else
				return false;
		}
		else
		{
			this.size = this.size + 1;
			this.stacksize = this.stacksize + 1;
			this.Card_Array[this.stacksize] = card;
			return true;
		}
	}
   public boolean put_work(Card card)
   {
                        this.size = this.size + 1;
			this.stacksize = this.stacksize + 1;
			this.Card_Array[this.stacksize] = card;
			return true;
   }
   public Card pop()
   {
   	  Card c;
   	  if (this.stacksize == -1)
   	  	return null;
   	  else
   	  {
   	  c = this.Card_Array[this.stacksize];
   	  this.stacksize = this.stacksize - 1;
   	  this.size = this.size - 1;
      return c;
      }
   }  


public Card get()
{
	if (this.stacksize == -1)
		return null;
	else
		return this.Card_Array[this.stacksize];
}

public Card get(int index)
{
	if (this.stacksize == -1)
		return null;
	else
		return this.Card_Array[index];

}

public boolean isEmpty()
{
	if (this.stacksize == -1)
		return true;
	else 
		return false;
}

public boolean put2(CardStack stack)
	{
		Card k;
		int i = 0;
		if ((stack.size() == 0) || (stack == null))
			return false;
		else
		{

		for (i=0; i<stack.size();i++)
		{
		   k = stack.get(i);
		   this.put(k);
	    }
	    if (this.size() != 0)
	    return true;
	    else
	    	return false;
	}
   }
    public boolean put(CardStack stack)
	{
		Card k;
		int i = 0;
		if ((stack.size() == 0) || (stack == null))
			return false;
		else
		{

		for (i=0; i<stack.size();i++)
		{
		   k = stack.get(i);
		   this.put(k);
	    }
	    if (this.size() != 0)
	    return true;
	    else
	    	return false;
	}
   }
   public CardStack pop(Card card)
   {
      int i = 0;
      int j = 0;
      Card k;
      CardStack stack = new MyCardDeck(1,0);
      for (i = 0; i<this.size();i++)
      {
      	 if (this.get(i).equals(card))
      	 {
      	 	for (j = i; j<this.size;j++)
      	 	{
      	 		stack.put(this.get(j));
      	 		this.stacksize = this.stacksize - 1;
      	 		i++;
      	 	}
      	 }
      }
      this.size = this.stacksize + 1;
      return stack;
   }	
}

