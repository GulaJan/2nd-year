-- File: ledc8x8.vhd
-- Author: Jan Gula , xgulaj00
-- Project: INP Project1
-- ///////////////////////////

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;

entity ledc8x8 is
port (
  RESET: in std_logic;
  SMCLK: in std_logic;
  ROW: out std_logic_vector (0 to 7);
  LED: out std_logic_vector (0 to 7)
);
end entity ledc8x8;

architecture display of ledc8x8 is
	signal ce: std_logic; -- clock enable
	signal counter: std_logic_vector(21 downto 0); -- a counter for clock enable
	signal fleds: std_logic_vector(7 downto 0); -- first letter leds
	signal sleds: std_logic_vector(7 downto 0); -- second letter leds
	signal rows: std_logic_vector(7 downto 0);
	signal switch: std_logic; -- switching between the first and the second letter


begin
	
	lower_freq: process(SMCLK, RESET) -- incrementing the counter after one smclk event
	
	begin
		if RESET = '1' then 	-- asynchronous reset
			counter <= "0000000000000000000000"; -- setting the counter to zero for all 22 elements
		elsif SMCLK'event and SMCLK = '1' then 
			counter <= counter + 1;
			if counter(7 downto 0) = "11111111" then 
				ce <= '1';
			else 
				ce <= '0';
			end if;
		end if;
		switch <= counter(21);
	end process lower_freq;
	
	rotate_rows: process(RESET, SMCLK, ce) -- a function for row shifting acts as if the rows were triggered all at once
	begin
		if RESET = '1' then  -- asynchronous reset
			rows <= "10000000";
		elsif SMCLK'event and SMCLK = '1' then
			if ce = '1' then
				case rows is
					when "10000000" => rows <= "01000000";
			        when "01000000" => rows <= "00100000";
		            when "00100000" => rows <= "00010000";
		            when "00010000" => rows <= "00001000";
		            when "00001000" => rows <= "00000100";
		            when "00000100" => rows <= "00000010";
		            when "00000010" => rows <= "00000001";
		            when "00000001" => rows <= "10000000";
                    when others     => rows <= "00000000";
                end case;    
           end if;
        end if;   
	end process rotate_rows;
	
	decoding_lines: process(rows) -- fleds are G  sleds are J
	begin
		case rows is
          when "10000000" => fleds <= "11000000";
                             sleds <= "11111101";
          when "01000000" => fleds <= "10111111";
                             sleds <= "11111101";
          when "00100000" => fleds <= "01111111";
                             sleds <= "11111101";         
          when "00010000" => fleds <= "01111111";
                             sleds <= "11111101";
          when "00001000" => fleds <= "01100000";
                             sleds <= "11111101";
          when "00000100" => fleds <= "01111110";
                             sleds <= "11011101";
          when "00000010" => fleds <= "10111110";
                             sleds <= "11011011";
          when "00000001" => fleds <= "11000000";
                             sleds <= "11100111";
          when others     => fleds <= "11111111";
                             sleds <= "11111111";                  
        end case;
	end process decoding_lines;

	switching: process (rows) -- switching between fleds and sleds
 		begin
 			ROW <= rows;
 			if switch = '1' then
    			LED <= fleds;
 			else
    			LED <= sleds;
			 end if;      
	end process switching;

end architecture display;
