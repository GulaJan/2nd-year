	
/* c206.c **********************************************************}
{* Téma: Dvousměrně vázaný lineární seznam
**
**                   Návrh a referenční implementace: Bohuslav Křena, říjen 2001
**                            Přepracované do jazyka C: Martin Tuček, říjen 2004
**                                            Úpravy: Bohuslav Křena, říjen 2016
**
** Implementujte abstraktní datový typ dvousměrně vázaný lineární seznam.
** Užitečným obsahem prvku seznamu je hodnota typu int.
** Seznam bude jako datová abstrakce reprezentován proměnnou
** typu tDLList (DL znamená Double-Linked a slouží pro odlišení
** jmen konstant, typů a funkcí od jmen u jednosměrně vázaného lineárního
** seznamu). Definici konstant a typů naleznete v hlavičkovém souboru c206.h.
**
** Vaším úkolem je implementovat následující operace, které spolu
** s výše uvedenou datovou částí abstrakce tvoří abstraktní datový typ
** obousměrně vázaný lineární seznam:
**
**      DLInitList ...... inicializace seznamu před prvním použitím,
**      DLDisposeList ... zrušení všech prvků seznamu,
**      DLInsertFirst ... vložení prvku na začátek seznamu,
**      DLInsertLast .... vložení prvku na konec seznamu, 
**      DLFirst ......... nastavení aktivity na první prvek,
**      DLLast .......... nastavení aktivity na poslední prvek, 
**      DLCopyFirst ..... vrací hodnotu prvního prvku,
**      DLCopyLast ...... vrací hodnotu posledního prvku, 
**      DLDeleteFirst ... zruší první prvek seznamu,
**      DLDeleteLast .... zruší poslední prvek seznamu, 
**      DLPostDelete .... ruší prvek za aktivním prvkem,
**      DLPreDelete ..... ruší prvek před aktivním prvkem, 
**      DLPostInsert .... vloží nový prvek za aktivní prvek seznamu,
**      DLPreInsert ..... vloží nový prvek před aktivní prvek seznamu,
**      DLCopy .......... vrací hodnotu aktivního prvku,
**      DLActualize ..... přepíše obsah aktivního prvku novou hodnotou,
**      DLSucc .......... posune aktivitu na další prvek seznamu,
**      DLPred .......... posune aktivitu na předchozí prvek seznamu, 
**      DLActive ........ zjišťuje aktivitu seznamu.
**
** Při implementaci jednotlivých funkcí nevolejte žádnou z funkcí
** implementovaných v rámci tohoto příkladu, není-li u funkce
** explicitně uvedeno něco jiného.
**
** Nemusíte ošetřovat situaci, kdy místo legálního ukazatele na seznam 
** předá někdo jako parametr hodnotu NULL.
**
** Svou implementaci vhodně komentujte!
**
** Terminologická poznámka: Jazyk C nepoužívá pojem procedura.
** Proto zde používáme pojem funkce i pro operace, které by byly
** v algoritmickém jazyce Pascalovského typu implemenovány jako
** procedury (v jazyce C procedurám odpovídají funkce vracející typ void).
**/

#include "c206.h"

int solved;
int errflg;

void DLError() {
/*
** Vytiskne upozornění na to, že došlo k chybě.
** Tato funkce bude volána z některých dále implementovaných operací.
**/	
    printf ("*ERROR* The program has performed an illegal operation.\n");
    errflg = TRUE;             /* globální proměnná -- příznak ošetření chyby */
    return;
}

void DLInitList (tDLList *L) {
/*
** Provede inicializaci seznamu L před jeho prvním použitím (tzn. žádná
** z následujících funkcí nebude volána nad neinicializovaným seznamem).
** Tato inicializace se nikdy nebude provádět nad již inicializovaným
** seznamem, a proto tuto možnost neošetřujte. Vždy předpokládejte,
** že neinicializované proměnné mají nedefinovanou hodnotu.
**/
	L->First = NULL; // initializing all variables being used
	L->Act = NULL;
	L->Last = NULL; 
}
void DLDisposeList (tDLList *L) {
/*
** Zruší všechny prvky seznamu L a uvede seznam do stavu, v jakém
** se nacházel po inicializaci. Rušené prvky seznamu budou korektně
** uvolněny voláním operace free. 
**/
	for(L->Act = L->First; L->Act != NULL; L->Act = L->First){ // cycling through the list
		L->First = L->Act->rptr; 
		free(L->Act);
	}
	L->Last = NULL;
}

void DLInsertFirst (tDLList *L, int val) {
/*
** Vloží nový prvek na začátek seznamu L.
** V případě, že není dostatek paměti pro nový prvek při operaci malloc,
** volá funkci DLError().
**/
	tDLElemPtr temp; // temporary
 	if((temp = (tDLElemPtr) malloc(sizeof(tDLElemPtr))) == NULL) { // if there's no memory left call DLError
  		DLError();
	} else {
		temp->data = val; // if there is enough memory fill our temporary pointer
		temp->lptr = NULL;
		temp->rptr = L->First;
		if (L->Last == NULL) { // if the list isn't full set the last element to temp
			L->Last = temp;
		} else {
			L->First->lptr = temp; 
		}
	L->First = temp; 
	}
}

void DLInsertLast(tDLList *L, int val) {
/*
** Vloží nový prvek na konec seznamu L (symetrická operace k DLInsertFirst).
** V případě, že není dostatek paměti pro nový prvek při operaci malloc,
** volá funkci DLError().
**/ 	
	tDLElemPtr temp;
 	if((temp = (tDLElemPtr) malloc(sizeof(tDLElemPtr))) == NULL) { // if there's no memory left call DLError
 		DLError();
	} else {
		temp->data = val; // if there is enough memory fill our temporary pointer
		temp->lptr = L->Last;
		temp->rptr = NULL;
		if (L->Last == NULL) {
			L->First = temp;
		} else {
			L->Last->rptr = temp;
		}
	L->Last = temp;
	}
}

void DLFirst (tDLList *L) {
/*
** Nastaví aktivitu na první prvek seznamu L.
** Funkci implementujte jako jediný příkaz (nepočítáme-li return),
** aniž byste testovali, zda je seznam L prázdný.
**/
	L->Act = L->First;
}

void DLLast (tDLList *L) {
/*
** Nastaví aktivitu na poslední prvek seznamu L.
** Funkci implementujte jako jediný příkaz (nepočítáme-li return),
** aniž byste testovali, zda je seznam L prázdný.
**/
	L->Act = L->Last;	
}

void DLCopyFirst (tDLList *L, int *val) {
/*
** Prostřednictvím parametru val vrátí hodnotu prvního prvku seznamu L.
** Pokud je seznam L prázdný, volá funkci DLError().
**/
	if(L->First == NULL){
		DLError();
	} else {
		*val = L->First->data;
	}
}

void DLCopyLast (tDLList *L, int *val) {
/*
** Prostřednictvím parametru val vrátí hodnotu posledního prvku seznamu L.
** Pokud je seznam L prázdný, volá funkci DLError().
**/
	if(L->First == NULL){
		DLError();
	} else {
		*val = L->Last->data;
	}
}

void DLDeleteFirst (tDLList *L) {
/*
** Zruší první prvek seznamu L. Pokud byl první prvek aktivní, aktivita 
** se ztrácí. Pokud byl seznam L prázdný, nic se neděje.
**/
	tDLElemPtr temp;
	if(L->First != NULL) { // if the list is empty do nothing
		temp = L->First;
		if(L->Last == L->First){ // if there was only one element in the list set all variables to null
			L->First = NULL;
			L->Act = NULL;
			L->Last = NULL;
		} else {
			if(L->Act == L->First){ // if first was active it looses its activity
				L->Act = NULL;
			}
		L->First = temp->rptr;
		L->First->lptr = NULL;
		}
	free(temp);
	}
}	

void DLDeleteLast (tDLList *L) {
/*
** Zruší poslední prvek seznamu L. Pokud byl poslední prvek aktivní,
** aktivita seznamu se ztrácí. Pokud byl seznam L prázdný, nic se neděje.
**/ 
	tDLElemPtr temp;
	if(L->First != NULL) { // if the list is empty do nothing
		temp = L->Last;
		if(L->Last == L->First){ // if there was only one element set all variables to null
			L->First = NULL;
			L->Act = NULL;
			L->Last = NULL;
		} else {
			if(L->Act == L->Last){ // if active is last set active to null
				L->Act = NULL; 
			}
		L->Last = temp->lptr;
		L->Last->rptr = NULL;
		}
	free(temp);
	}
}

void DLPostDelete (tDLList *L) {
/*
** Zruší prvek seznamu L za aktivním prvkem.
** Pokud je seznam L neaktivní nebo pokud je aktivní prvek
** posledním prvkem seznamu, nic se neděje.
**/
	tDLElemPtr temp;
	if(L->Act != NULL){
		if(L->Act->rptr != NULL){
			temp = L->Act->rptr;
			L->Act->rptr = temp->rptr; 
			if(temp == L->Last){ // if the deleted element was last set last to active element
				L->Last = L->Act;
			} else {
				temp->rptr->lptr = L->Act;
			}
		free(temp);	
		}
	}
}

void DLPreDelete (tDLList *L) {
/*
** Zruší prvek před aktivním prvkem seznamu L .
** Pokud je seznam L neaktivní nebo pokud je aktivní prvek
** prvním prvkem seznamu, nic se neděje.
**/
	tDLElemPtr temp;
	if(L->Act != NULL){ // if the list is not active do nothing
		if(L->Act->lptr != NULL){ 
			temp = L->Act->lptr; // save the left side of our active element to temp
			L->Act->lptr = temp->lptr; // delete the element before active
			if(temp == L->First) { 
				L->First = L->Act; // if we deleted the first element select active as first
			} else {
				temp->lptr->rptr = L->Act;
			}
			free(temp);
		}
	}
}

void DLPostInsert (tDLList *L, int val) {
/*
** Vloží prvek za aktivní prvek seznamu L.
** Pokud nebyl seznam L aktivní, nic se neděje.
** V případě, že není dostatek paměti pro nový prvek při operaci malloc,
** volá funkci DLError().
**/
	tDLElemPtr temp;
	if(L->Act != NULL){
		if((temp = (tDLElemPtr) malloc(sizeof(tDLElemPtr))) == NULL) { // if there's no memory left call DLError
			DLError();
		} else {
			temp->data = val; // if there is enough memory fill our temporary pointer
			temp->lptr = L->Act;
			temp->rptr = L->Act->rptr;
			L->Act->rptr = temp; // set the right side of active element to temp
			if(L->Act == L->Last) { // if the active element is also the last one set last to temporary
				L->Last = temp;
			} else {
				temp->rptr->lptr = temp;
			}
		}
	}
}
void DLPreInsert (tDLList *L, int val) {
/*
** Vloží prvek před aktivní prvek seznamu L.
** Pokud nebyl seznam L aktivní, nic se neděje.
** V případě, že není dostatek paměti pro nový prvek při operaci malloc,
** volá funkci DLError().
**/
	tDLElemPtr temp; // temporary
	if(L->Act != NULL){
		if((temp = (tDLElemPtr) malloc(sizeof(tDLElemPtr))) == NULL) { // if there's no memory left call DLError
			DLError();
		} else {
			temp->data = val; // if there is enough memory fill our temporary pointer
			temp->lptr = L->Act->lptr;
			temp->rptr = L->Act;
			L->Act->lptr = temp; // inserts the new element on the left side of the active element
			if(L->Act != L->First) { // if active element is also the first one set set the first one to temp
				temp->lptr->rptr = temp; 
			} else {
				L->First = temp; 
			}
		}
	}
}

void DLCopy (tDLList *L, int *val) {
/*
** Prostřednictvím parametru val vrátí hodnotu aktivního prvku seznamu L.
** Pokud seznam L není aktivní, volá funkci DLError ().
**/
	if(L->Act != NULL){ // if the list is active fill value with active element's data if it's not call DLError
		*val = L->Act->data;
	} else {
		DLError();
	}

}

void DLActualize (tDLList *L, int val) {
/*
** Přepíše obsah aktivního prvku seznamu L.
** Pokud seznam L není aktivní, nedělá nic.
**/
	if(L->Act != NULL){ // if the list is not active do nothing
		L->Act->data = val; // else fill data of the active element with the given value
	}
}

void DLSucc (tDLList *L) {
/*
** Posune aktivitu na následující prvek seznamu L.
** Není-li seznam aktivní, nedělá nic.
** Všimněte si, že při aktivitě na posledním prvku se seznam stane neaktivním.
**/
	if(L->Act != NULL) { // if the list is not active do nothing
		if(L->Act == L->Last) { // if active is the last element the list becomes inactive
			L->Act = NULL;
		} else {
		L->Act = L->Act->rptr; // if it isn't set the element on the right side as active
		}	
	}
}


void DLPred (tDLList *L) {
/*
** Posune aktivitu na předchozí prvek seznamu L.
** Není-li seznam aktivní, nedělá nic.
** Všimněte si, že při aktivitě na prvním prvku se seznam stane neaktivním.
**/
	if(L->Act != NULL) { // if the list is not active do nothing
		if(L->Act == L->First) { // if active is first the list becomes inactive
			L->Act = NULL;
		} else {
			L->Act = L->Act->lptr; // if it's not set the element on the left side as active
		}
	}
}

int DLActive (tDLList *L) {
/*
** Je-li seznam L aktivní, vrací nenulovou hodnotu, jinak vrací 0.
** Funkci je vhodné implementovat jedním příkazem return.
**/
	return (L->Act != NULL); // is true if list is not active is false if it's not
}

/* Konec c206.c*/
