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
import src.MyCard;
import src.Card;
import src.CardDeck;
import src.MyCardDeck;
import src.CardStack;
public abstract class AbstractFactorySolitaire 
{

	public abstract Card createCard(Card.Color color, int value);
	public abstract CardDeck createCardDeck();
	public abstract CardDeck createTargetPack(Card.Color color);
	public abstract CardStack createWorkingPack();
}

