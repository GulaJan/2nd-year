DROP TABLE zamestnanec CASCADE CONSTRAINTS;
DROP TABLE typuctu CASCADE CONSTRAINTS;
DROP TABLE pobocka CASCADE CONSTRAINTS;
DROP TABLE ucet CASCADE CONSTRAINTS;
DROP TABLE karta CASCADE CONSTRAINTS;
DROP TABLE klient CASCADE CONSTRAINTS;
DROP TABLE operace CASCADE CONSTRAINTS;
DROP TABLE klientDisponuje CASCADE CONSTRAINTS;
DROP SEQUENCE klient_seq;
DROP MATERIALIZED VIEW typUctuView;

CREATE SEQUENCE klient_seq START WITH 1 INCREMENT BY 1 NOCYCLE;


CREATE TABLE zamestnanec(
                          id_Zamestnanec NUMBER NOT NULL,
                          PriezviskoZamestnanca VARCHAR2(30) NOT NULL,
                          MenoZamestnanca VARCHAR2(30) NOT NULL,
                          AdresaZamestnanca VARCHAR2(50) NOT NULL,
                          id_Pob NUMBER NOT NULL);
CREATE TABLE typuctu(id_Typ NUMBER NOT NULL,
                      Sluzby VARCHAR2(60) NOT NULL);
CREATE TABLE pobocka(id_Pobocka NUMBER NOT NULL,
                     Adresa VARCHAR2(60) NOT NULL
                    );
CREATE TABLE ucet(id_cislouctu NUMBER NOT NULL,
                  IBAN VARCHAR2(26) NOT NULL UNIQUE,
                  AktivovaneSluzby VARCHAR2(60) NOT NULL,
                  id_type NUMBER NOT NULL,
                  id_Klient NUMBER NOT NULL);
 CREATE OR REPLACE TRIGGER ibanTrig BEFORE INSERT OR UPDATE OF IBAN ON ucet FOR EACH ROW 
  DECLARE 
    fullIb VARCHAR2(26);
    stateCode VARCHAR2(20);
    controlCode VARCHAR2(20);
    bankCode VARCHAR2(20);
    foreNum VARCHAR2(20);
    basicNum VARCHAR2(20);
    sumNum NUMBER;
  BEGIN
    fullIb := :NEW.IBAN;
    IF (LENGTH(TRIM(fullIb)) != 24) THEN
      Raise_Application_Error(-20000, 'IBAN has to contain 24 letters!');
    END IF;
    stateCode := SUBSTR(fullIb, 1, 2);
    IF (LENGTH(TRIM(TRANSLATE(stateCode, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', ''))) != NULL) THEN
      Raise_Application_Error(-20001, 'IBAN state code has to be two A-Z characters!');
    END IF;
    controlCode := SUBSTR(fullIb, 3, 4);
    IF (LENGTH(TRIM(TRANSLATE(controlCode, '123456789', ''))) != NULL) THEN
      Raise_Application_Error(-20002, 'IBAN control code has to contain numbers from 0-9!');
    END IF;
    bankCode := SUBSTR(fullIb, 5, 8);
    IF (LENGTH(TRIM(TRANSLATE(bankCode, '123456789', ''))) != NULL) THEN
      Raise_Application_Error(-20003, 'IBAN bank code has to contain numbers from 0-9!');
    END IF;
    foreNum := SUBSTR(fullIb, 9, 14);
    IF (LENGTH(TRIM(TRANSLATE(foreNum, '123456789', ''))) != NULL) THEN
      Raise_Application_Error(-20004, 'IBAN forenumber has to contain numbers from 0-9!');
    END IF;
    basicNum := SUBSTR(fullIb, 15, 24);
    IF (LENGTH(TRIM(TRANSLATE(basicNum, '123456789', ''))) != NULL) THEN
      Raise_Application_Error(-20006, 'IBAN basic number has to contain numbers from 0-9!');
    END IF;
    --zprava 1, 2, 4, 8, 5, 10, 9, 7, 3, 6
    sumNum := TO_NUMBER(SUBSTR(basicNum, 1, 1),'9') * 6 + TO_NUMBER(SUBSTR(basicNum, 2, 1),'9') * 3 + TO_NUMBER(SUBSTR(basicNum, 3 , 1),'9') * 7 + TO_NUMBER(SUBSTR(basicNum, 4, 1),'9') * 9 + TO_NUMBER(SUBSTR(basicNum, 5, 1),'9') * 10 + TO_NUMBER(SUBSTR(basicNum, 6, 1),'9') * 5 + TO_NUMBER(SUBSTR(basicNum, 7, 1),'9') * 8 + TO_NUMBER(SUBSTR(basicNum, 8, 1),'9') * 4 + TO_NUMBER(SUBSTR(basicNum, 9, 1),'9') * 2 + TO_NUMBER(SUBSTR(basicNum, 10, 1),'9') * 1;
    IF(MOD(sumNum, 11) != 0) THEN
      Raise_Application_Error(-7, 'IBAN basic number has to follow a certain formula!');
  END IF; 
  END ibanTrig;
/                  
CREATE TABLE karta(id_CisloKarty NUMBER NOT NULL,
                   id_Klient NUMBER NOT NULL,
                   id_ucet NUMBER NOT NULL,
                    VerifikacnyKod NUMBER NOT NULL,
                    DobaPlatnosti DATE NOT NULL,
                    DrzitelKarty VARCHAR2(60) NOT NULL);
CREATE TABLE klient(id_K NUMBER NOT NULL,
                    MenoKlienta VARCHAR2(30) NOT NULL,
                    PriezviskoKlienta VARCHAR2(40) NOT NULL,
                    AdresaKlienta VARCHAR2(60) NOT NULL,
                    Telefon VARCHAR2(60) NOT NULL,
                    Email VARCHAR2(80) NOT NULL);
CREATE OR REPLACE TRIGGER klientTrig BEFORE
  INSERT ON klient
  FOR EACH ROW
    BEGIN
      SELECT klient_seq.NEXTVAL
      INTO : NEW.id_K
      FROM dual;
    END;
/
                    
CREATE TABLE operace(id_Operace NUMBER NOT NULL,
                     id_Klient NUMBER NOT NULL,
                      DatumVykonania DATE NOT NULL,
                      MenoVykonavatela VARCHAR2(60) NOT NULL,
                      VRamciBanky NUMBER,
                      MimoBanky NUMBER,
                      PrevadzanaCiastka NUMBER,
                      PrevadzanaMena VARCHAR2(20),
                      CisloUctuPrijemcu NUMBER,
                      VyberovaCiastka NUMBER,
                      MiestoVyberu VARCHAR2(50),
                      PoplatokZaVyber NUMBER,
                      VyberovaMena VARCHAR(30),
                      VkladanaCiastka NUMBER ,
                      MiestoVkladu VARCHAR2(60),
                      VkladanaMena VARCHAR2(20));
CREATE TABLE klientDisponuje(obmedzenie NUMBER NOT NULL,
                             id_Klient NUMBER NOT NULL,
                             id_cislouctu NUMBER NOT NULL);
                              
ALTER TABLE zamestnanec ADD CONSTRAINT PK_zamestnanec PRIMARY KEY (id_Zamestnanec);
ALTER TABLE operace ADD CONSTRAINT PK_operace PRIMARY KEY (id_Operace);
ALTER TABLE klient ADD CONSTRAINT PK_klient PRIMARY KEY (id_K);
ALTER TABLE ucet ADD CONSTRAINT PK_ucet PRIMARY KEY (id_cislouctu);
ALTER TABLE karta ADD CONSTRAINT PK_karta PRIMARY KEY (id_CisloKarty);
ALTER TABLE pobocka ADD CONSTRAINT PK_pobocka PRIMARY KEY (id_Pobocka);
ALTER TABLE typuctu ADD CONSTRAINT PK_typuctu PRIMARY KEY (id_Typ);
ALTER TABLE  zamestnanec ADD CONSTRAINT FK_pobocka FOREIGN KEY(id_Pob) REFERENCES pobocka ;
ALTER TABLE  ucet ADD CONSTRAINT FK_uklient FOREIGN KEY(id_Klient) REFERENCES klient ;
ALTER TABLE  operace ADD CONSTRAINT FK_oklient FOREIGN KEY(id_Klient) REFERENCES klient ;
ALTER TABLE  karta ADD CONSTRAINT FK_cklient FOREIGN KEY(id_Klient) REFERENCES klient ;
ALTER TABLE  ucet ADD CONSTRAINT FK_typuctu FOREIGN KEY(id_type) REFERENCES typuctu ;
ALTER TABLE  karta ADD CONSTRAINT FK_cucet FOREIGN KEY(id_ucet) REFERENCES ucet ;
INSERT INTO pobocka(id_Pobocka, Adresa) VALUES ('001','Brno');
INSERT INTO pobocka(id_Pobocka, Adresa) VALUES ('002','Olomouc');
INSERT INTO pobocka(id_Pobocka, Adresa) VALUES ('003','Praha');
INSERT INTO zamestnanec (id_Zamestnanec, PriezviskoZamestnanca, MenoZamestnanca, AdresaZamestnanca,id_Pob) VALUES( '001','Veres','Marcel','Brno','001');
INSERT INTO zamestnanec (id_Zamestnanec, PriezviskoZamestnanca, MenoZamestnanca, AdresaZamestnanca,id_Pob) VALUES( '002','Sevcik','Peter','Brno','001');
INSERT INTO zamestnanec (id_Zamestnanec, PriezviskoZamestnanca, MenoZamestnanca, AdresaZamestnanca,id_Pob) VALUES( '003','Blasko','Peter','Ostrava','003');
INSERT INTO zamestnanec (id_Zamestnanec, PriezviskoZamestnanca, MenoZamestnanca, AdresaZamestnanca,id_Pob) VALUES( '004','Mokos','Andrej','Olomouc','002');
INSERT INTO zamestnanec (id_Zamestnanec, PriezviskoZamestnanca, MenoZamestnanca, AdresaZamestnanca,id_Pob) VALUES( '005','Petroff','Adam','Kyjov','001');
INSERT INTO zamestnanec (id_Zamestnanec, PriezviskoZamestnanca, MenoZamestnanca, AdresaZamestnanca,id_Pob) VALUES( '006','Novak' ,'Jan','Hradec Kralove','002');
INSERT INTO typuctu (id_Typ, Sluzby) VALUES('001','Povolene precerpanie,BezplatnyVyber,StudentskyUcet');
INSERT INTO typuctu (id_Typ, Sluzby) VALUES('002','Povolene precerpanie,SporiaciUrok');
INSERT INTO typuctu (id_Typ, Sluzby) VALUES('003','Povolene precerpanie');
INSERT INTO klient (id_K,MenoKlienta, PriezviskoKlienta, AdresaKlienta, Telefon, Email) VALUES(NULL,'Martin','Nizner','Hlohovecka 395/51 Sokolovce 92231','+421915565026','niznermartin96@gmail.com');
INSERT INTO klient (id_K,MenoKlienta, PriezviskoKlienta, AdresaKlienta, Telefon, Email) VALUES(NULL,'Jan','Gula','Skrivanova 327 Brno 62800','+421955423421','jangula@gmail.com');
INSERT INTO klient (id_K,MenoKlienta, PriezviskoKlienta, AdresaKlienta, Telefon, Email) VALUES(NULL,'Robert','Misura','Piestanska 451/87 Povazany 91626','+421940453781','misurarobert77@gmail.com');
INSERT INTO klient (id_K,MenoKlienta, PriezviskoKlienta, AdresaKlienta, Telefon, Email) VALUES(NULL,'Novak','Michal',' 215 Praha 14000','+420925544007','mnovak45@gmail.com');
INSERT INTO klient (id_K,MenoKlienta, PriezviskoKlienta, AdresaKlienta, Telefon, Email) VALUES(NULL,'Hurtik','Lukas','A.Trajana 25 Piestany 92101','+421907541782','hurtiklukas@gmail.com');
INSERT INTO klient (id_K,MenoKlienta, PriezviskoKlienta, AdresaKlienta, Telefon, Email) VALUES(NULL,'Bednar','Pavel','Druzebni 542/14 Ostrava 72526','+420902413714','pbednar@seznam.cz');
INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '77889653','CZ6508000000192000145399' , 'PovolenePrecerpanie','001','1');
INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '77889785','CZ3155000000001043006511' , 'StudentskyUcet, BezplatnyVyber','001','2');
INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '65627653','CZ0201000000199216760237' , 'PovolenePrecerpanie, SporiaciUrok','002','3');
INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '74129921','CZ2306000000000176039275' , 'PovolenePrecerpanie','002','1');
INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '70214589','CZ1520100000002500218857' , 'StudentskyUcet','001','4');
INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '74245658','CZ4301000000000085636621' , 'PovolenePrecerpanie','003','6');
INSERT INTO karta (id_CisloKarty,VerifikacnyKod,DobaPlatnosti,DrzitelKarty, id_Klient, id_ucet) VALUES('23468745','5588','25.03.2018','MartinNizner','1','77889653');
INSERT INTO karta (id_CisloKarty,VerifikacnyKod,DobaPlatnosti,DrzitelKarty, id_Klient, id_ucet) VALUES('58964323','1622','25.04.2019','JanGula','2','77889785');
INSERT INTO karta (id_CisloKarty,VerifikacnyKod,DobaPlatnosti,DrzitelKarty, id_Klient, id_ucet) VALUES('56879795','4872','01.09.2020','RobertMisura','3','65627653');
INSERT INTO karta (id_CisloKarty,VerifikacnyKod,DobaPlatnosti,DrzitelKarty, id_Klient, id_ucet) VALUES('25123456','5412','25.07.2019','MichalNovak','4','70214589');
INSERT INTO karta (id_CisloKarty,VerifikacnyKod,DobaPlatnosti,DrzitelKarty, id_Klient, id_ucet) VALUES('77121565','1245','12.01.2020','PavelBednar','6','74245658');
INSERT INTO karta (id_CisloKarty,VerifikacnyKod,DobaPlatnosti,DrzitelKarty, id_Klient, id_ucet) VALUES('85412135','1478','04.08.2021','MartinNizner','1','74129921');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, VyberovaCiastka, VyberovaMena, PoplatokZaVyber, MiestoVyberu,id_Klient) VALUES ('000100', '03.04.2017' , 'MartinNizner' , '500' , 'EUR', '0', 'Brno','1');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, CisloUctuPrijemcu, VRamciBanky, MimoBanky, PrevadzanaCiastka, PrevadzanaMena, id_Klient) VALUES ('002158', '02.01.2016' , 'JanGula', '4556785235' , '1','0','2000', 'Kc','2');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, VkladanaCiastka, MiestoVkladu, VkladanaMena, id_Klient) VALUES ('606652', '24.12.2014' , 'RobertMisura' , '1500','Praha','USD','3');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, VkladanaCiastka, MiestoVkladu, VkladanaMena, id_Klient) VALUES ('815557', '01.09.2017' , 'MartinNisura' , '2000','Olomouc','EUR','1');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, VyberovaCiastka, VyberovaMena, PoplatokZaVyber, MiestoVyberu, id_Klient) VALUES ('025100', '04.05.2017' , 'MartinNizner' , '100' , 'EUR', '10', 'Praha','1');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, CisloUctuPrijemcu, VRamciBanky, MimoBanky, PrevadzanaCiastka, PrevadzanaMena, id_Klient) VALUES ('002178', '12.11.2015' , 'JanGula', '7666785235' , '1','0','10000', 'Kc','2');
INSERT INTO operace (id_Operace, DatumVykonania, MenoVykonavatela, CisloUctuPrijemcu, VRamciBanky, MimoBanky, PrevadzanaCiastka, PrevadzanaMena, id_Klient) VALUES ('004120', '08.05.2016' , 'MartinNizner', '5626785235' , '1','0','20', 'EUR','1');
INSERT INTO klientDisponuje(obmedzenie, id_Klient, id_cislouctu) VALUES ('1', '0214', '77889653');
INSERT INTO klientDisponuje(obmedzenie, id_Klient, id_cislouctu) VALUES ('0', '0478', '77889653');
INSERT INTO klientDisponuje(obmedzenie, id_Klient, id_cislouctu) VALUES ('1', '8742', '65627653');


SET serveroutput ON;
CREATE OR REPLACE PROCEDURE typUctuPercentualne (id_TypUctu NUMBER) AS 
cursor allUcty IS SELECT * FROM ucet;
vsetkyUcty NUMBER;
mojTyp NUMBER;
allStlpce allUcty%ROWTYPE;
BEGIN
  vsetkyUcty := 0;
  mojTyp := 0;
  OPEN allUcty;
  LOOP
    FETCH allUcty INTO allStlpce;
    IF allStlpce.id_type = id_TypUctu THEN
      mojTyp := mojTyp + 1;
    END IF;
    EXIT WHEN allUcty%NOTFOUND;
    vsetkyUcty := vsetkyUcty + 1;
  END LOOP;
  CLOSE allUcty;
  dbms_output.put_line('There is '||ROUND(mojTyp/vsetkyUcty * 100, 2)||'% of this type of an account.');
  EXCEPTION
    WHEN ZERO_DIVIDE THEN
      dbms_output.put_line('No valid accounts!');
    WHEN OTHERS THEN
      Raise_Application_Error(-20010, 'Other error!');
  END;
  /
SET serveroutput ON;
CREATE OR REPLACE PROCEDURE pocZamVPob (id_Pobocky NUMBER) AS 
cursor allZam IS SELECT * FROM zamestnanec;
operaceRow allZam%ROWTYPE;
vsetkyOp NUMBER;
zamVPob NUMBER;
BEGIN
  zamVPob := 0;
  vsetkyOp := 0;
  OPEN allZam;
  LOOP
    FETCH allZam INTO operaceRow;
    IF operaceRow.id_Pob = id_Pobocky THEN
      zamVPob := zamVPob + 1;
    END IF;
    EXIT WHEN allZam%NOTFOUND;        
    vsetkyOp := vsetkyOp + 1;
  END LOOP;    
  CLOSE allZam;
  dbms_output.put_line('This bank has '||zamVPob||' employees.');
  EXCEPTION
    WHEN ZERO_DIVIDE THEN
      dbms_output.put_line('There are no employees in this bank!');
    WHEN OTHERS THEN
      Raise_Application_Error(-20010, 'Other error!');
END;    
/
-- Vyberie meno zamestnanca a adresu pobocky v ktorej pracuje
SELECT
  zamestnanec.MenoZamestnanca,
  pobocka.Adresa
FROM
  zamestnanec,
  pobocka
WHERE
  zamestnanec.id_Pob = pobocka.id_Pobocka;
  
  
 
  
   -- Vyberie cislo uctu IBAN a typ sluzieb pre vsetky ucty
  SELECT
  ucet.id_cislouctu,
  ucet.IBAN,
  typuctu.sluzby
FROM
  ucet,
  typuctu
WHERE
  ucet.id_type = typuctu.id_typ;
  
  
   -- Vyberie Meno a email klienta , IBAN jeho uctu a dobu platnosti jeho karty
  SELECT
  klient.MenoKlienta,
  klient.Email,
  ucet.IBAN,
  karta.DrzitelKarty,
  karta.DobaPlatnosti
FROM
  klient,
  ucet,
  karta
WHERE
  ucet.id_Klient = klient.id_K
AND
  karta.id_Klient = klient.id_K;

-- Vyberie meno klienta a datum vykonanie operacie  a usporiada ich podla minima vkladanej ciastky
SELECT
  klient.MenoKlienta,
  operace.DatumVykonania,
  MIN(operace.VkladanaCiastka)
FROM
  klient,
  operace
WHERE
  klient.id_K = operace.id_Klient
AND
  operace.VkladanaCiastka IS NOT NULL
GROUP BY klient.MenoKlienta, operace.DatumVykonania
ORDER BY MIN (operace.VkladanaCiastka) asc;

-- Vyberie priezvisko klienta a to ci je operacia v ramci banky a usporiada ich podla maxima vkladanej ciastky
SELECT
  klient.PriezviskoKlienta,
  operace.VRamciBanky,
  MAX(operace.PrevadzanaCiastka)
FROM
  klient,
  operace
WHERE
  klient.id_K = operace.id_Klient
AND operace.PrevadzanaCiastka IS NOT NULL
GROUP BY klient.PriezviskoKlienta, operace.VRamciBanky
ORDER BY MAX (operace.PrevadzanaCiastka) desc;

-- Vyberie meno zamestnanca ktoreho priezvisko je Gula
SELECT
  zamestnanec.MenoZamestnanca
FROM
  zamestnanec
WHERE zamestnanec.id_Zamestnanec IN (
  SELECT zamestnanec.id_Zamestnanec
  FROM zamestnanec
  WHERE zamestnanec.PriezviskoZamestnanca = 'Gula');

-- Vyberie meno priezvisko a email klienta ktory je drzitelom karty s ID 001
SELECT
  klient.MenoKlienta,
  klient.PriezviskoKlienta,
  klient.Email
FROM
  klient
WHERE EXISTS (
  SELECT
    karta.DrzitelKarty
  FROM
    karta
  WHERE
    klient.id_K= '0001');
  
 
    EXPLAIN PLAN FOR
    SELECT MenoKlienta, PriezviskoKlienta, count(*)
    FROM
        ucet natural join klient
    GROUP BY MenoKlienta, PriezviskoKlienta;
    SELECT * FROM TABLE(DBMS_XPLAN.display);
    CREATE INDEX indexExplain ON klient (id_K, MenoKlienta, PriezviskoKlienta);
    EXPLAIN PLAN FOR
    SELECT MenoKlienta, PriezviskoKlienta, count(*)
    FROM
        ucet natural join klient
    GROUP BY MenoKlienta, PriezviskoKlienta;
    SELECT * FROM TABLE(DBMS_XPLAN.display);
    exec typUctuPercentualne(001);
    exec pocZamVPob(1);
    GRANT ALL ON ucet TO xgulaj00;
    GRANT ALL ON operace TO xgulaj00;
    GRANT ALL ON typUctu TO xgulaj00;
    GRANT ALL ON klientDisponuje TO xgulaj00;
    GRANT ALL ON klient TO xgulaj00;
    GRANT ALL ON karta TO xgulaj00;
    GRANT EXECUTE ON typUctuPercentualne TO xgulaj00;
    
    CREATE MATERIALIZED VIEW LOG ON ucet WITH PRIMARY KEY, ROWID(id_type) INCLUDING NEW VALUES;
    CREATE MATERIALIZED VIEW typUctuView
    CACHE BUILD IMMEDIATE REFRESH FAST ON COMMIT ENABLE QUERY REWRITE AS SELECT id_type as "Typ Uctu", count(id_type) as "Pocet uctov tohto typu" FROM ucet GROUP BY id_type;
    GRANT ALL ON typUctuView TO xgulaj00;
    
    SELECT * FROM typUctuView;
    INSERT INTO ucet (id_CisloUctu, IBAN, AktivovaneSluzby,id_type,id_Klient) VALUES( '77889657','CZ5903000000000157857408' , 'StudenstkyUcet','002','1');
    COMMIT;
    SELECT * FROM typUctuView;
    
    