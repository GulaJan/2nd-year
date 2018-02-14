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
import src.Card;
public class MyCard implements Card, Serializable
{
	Card.Color farba;
	int value;
	boolean faceup;
	public MyCard(Card.Color c, int value)
	{
		this.value = value;
		this.farba = c;
    this.faceup = false;
	}
	public Card.Color color()
	{
		return this.farba;
	}
	
	public int value()
	{
		return this.value;
	}
	public boolean isTurnedFaceUp()
	{
		if (this.faceup == true)
			return true;
		else
			return false;
	}
	public boolean turnFaceUp()
	{
		if (faceup == false)
		{
			faceup = true;
			return true;
		}
		else
			return false;
	}
	public boolean similarColorTo(Card c)
	{
		if (c.color().equals(this.color()))
			return true;
    else if ((c.color().toString().equals("S")) && (this.color().toString().equals("C")))
      return true;
    else if ((c.color().toString().equals("C")) && (this.color().toString().equals("S")))
      return true;
    else if ((c.color().toString().equals("D")) && (this.color().toString().equals("H")))
      return true;
    else if ((c.color().toString().equals("H")) && (this.color().toString().equals("D")))
      return true;

		else 
			return false;
	}
	public int compareValue(Card c)
	{
       if (c.value() == this.value())
          return 0;
       else if (c.value() > this.value())
          return c.value() - this.value();
       else
          return this.value() - c.value();         
	}
	@Override
   public String toString()
   {
      String v2 = "N"; 
      if ((this.value() < 2) || (this.value() > 10))
      {
      if (this.value() == 1)
        v2 = "A";
      if (this.value() == 11)
        v2 = "J";
      if (this.value() == 12) 
        v2 = "Q";
      if (this.value() == 13)
        v2 = "K";
      return (v2 + "(" + this.color().toString() + ")");
      }
      else
      {
        v2 = Integer.toString(value);
        return (value + "(" + this.color().toString() + ")");
      }
   }
  @Override
  public int hashCode()
  {
    return this.value() *
    this.color().hashCode();
  }
  @Override
  public boolean equals(Object o)
  {
    if (o == null)
      return false;
    if (!(o instanceof Card))
      return false;
    Card test = (Card) o;
    if (this.value() != test.value())
      return false;
    if (this.color() != test.color())
      return false;
    String s1 = this.toString();
    String s2 = test.toString();
    if (!(s1.equals(s2)))
      return false;

    return true;
   }
  public String cardPath()
    {
            if(!this.isTurnedFaceUp()) return "./lib/back.gif";
            else if(value == 1)  return "./lib/A"+farba.toString()+".gif";
            else if(value == 11) return "./lib/J"+farba.toString()+".gif";
            else if(value == 12) return "./lib/Q"+farba.toString()+".gif";
            else if(value == 13) return "./lib/K"+farba.toString()+".gif";
            else return "./lib/"+value+farba.toString()+".gif";
        
    }
  public boolean turnFaceDown() {
            if(faceup == true) {
                faceup = false;
                return true;
            } else
                return false;
        }


  
}