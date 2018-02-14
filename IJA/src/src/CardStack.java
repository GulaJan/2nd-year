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
public interface CardStack extends CardDeck
{
	public boolean put(CardStack stack);
	CardStack pop(Card card);
}