% 1. uloha
s = audioread('xgulaj00.wav'); %nacitame signal
plot(s);%vykreslime

%2. uloha
x = s(1:100); %mierka dielikov pre ISS
X = fft(x); %rychla furierova transformacia
xShow = abs(X(1:51)); %len polovicu vzoriek
k = 0:50; 
f = k / 100 * 16000; %x-ovu os v Hz
plot(f,xShow);

%3. uloha
[g,y] = max(xShow); 
fncMax = f(y); %fncMax teraz obsahuje maximum modulu spektra

%4. uloha
a = [0.2289, 0.4662]; % a koeficienty filtru 
b = [0.2324, -0.4112, 0.2324]; % b koeficienty filtru
zplane(b,a); %vykreslenie kruznice s polmi

%5. uloha
Hfreqz = abs(freqz(b,a,8000)); %modul kmitoctovej charakteristiky
plot(Hfreqz); %vykresli ho

%6. uloha
H = filter(b,a,x); %filtrujeme signal
spect = H(1:100);
SPECT = fft(spect);%fourierova transformacia
hShow = abs(SPECT(1:51));%polovica vzorkov
k = 0:50;
f = k / 100 * 16000;%premena na herze
plot(f,hShow);
	
%7. uloha
[j,z] = max(SPECT);
maxSeven = f(z); %maxSeven teraz obsahuje maximum modulu spektra

%9. uloha
NineAns = xcorr(x,'biased'); %x - cross corr - correlation
sample = 100;
z=-sample+1:sample-1;
plot(z,NineAns); % vykresli
xlim([-50 50]); % x-ova os od -50 po 50

%10. uloha
TenAns = NineAns(10);