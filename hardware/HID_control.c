/*
 * Cerberus-Prox HID/Strike Board Firmware
 * 
 * Copyright 2008 Andrew Kilpatrick
 * Copyright 2009 William Lewis
 *
 * This file is part of Cerberus-Prox.
 *
 * Cerberus-Prox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus-Prox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus-Prox.  If not, see <http://www.gnu.org/licenses/>.
 *
 * RS232 Protocol
 * --------------
 * Each command or response is a newline terminated text command
 * in the following format:
 *
 *   C=P\n
 *
 *   where: C is command byte
 *          P is parameter byte
 *
 * Control Commands: (sent to the controller)
 *  - B		- beep the beeper
 *			  param: beep time in seconds (0-9, 0 = off, L = latch on)
 *  - G	    - change the LED colour from red to green
 *			  param: LED green time in seconds (0-9, 0 = off, L = latch on) 
 *  - S     - control the door strike
 *			  param: strike open time in seconds (0-9, 0 = off, L = latch on) 
 *  - ? 	- request status
 *
 * Responses:
 *  - ?		- control status
 *			  param: ABC
 *             - A = beep status (1 = on, 0 = off)
 *			   - B = green LED change status (1 = on, 0 = off)
 *			   - C = strike status (1 = open, 0 = closed)
 *			   The status will also be followed by an iXX message
 *			   even if the inputs have not changed.
 *
 *  - H		- HID card data
 *			  - byte 0 = 'H'
 *			  - bytes 1-n = HID card data (B'001p dddd')
 *			    where: p = odd parity bit of dddd
 *				       dddd = 8, 4, 2, 1 card data
 *            - byte n + 1 = 0x0a (newline)
 *
 *  - i     - Input status
 *            - byte 0 = 'i'
 *            - byte 1 = pins (B'001d dddd')
 *            - byte 2 = pins, inverted (B'001d dddd')
 *            - byte 3 = 0x0a (newline)
 */
#include <system.h>

// clock setting for delays
#pragma CLOCK_FREQ 4000000

// fuses
#pragma	DATA	_CONFIG,	_BODEN_OFF & _BOREN_OFF & _CP_OFF & _DATA_CP_OFF \
	&_PWRTE_ON & _WDT_ON & _LVP_OFF & _MCLRE_ON & _INTOSC_OSC_NOCLKOUT

// HID reader pins
#define HID_DATA porta.0		// input
#define HID_CLOCK porta.1		// input
#define HID_GRN_LED porta.2		// output
#define HID_RED_LED porta.3		// output
#define HID_CARD porta.4		// input
// porta.5 is unused
#define HID_BEEP porta.6		// output
#define HID_HOLD porta.7		// input

// I/O pins
#define STRIKE portb.0
#define GPIO_0 portb.3
#define GPIO_1 portb.4
#define GPIO_2 portb.5
#define GPIO_3 portb.6
#define GPIO_4 portb.7
#define GPIO_MASK 0xF8

// receiver stuff
#define RX_CMD 0
#define RX_DATA 1
#define RX_NL 2
unsigned char rx_state;
unsigned char cmd;
unsigned char data;

// transmitter stuff
unsigned char tx_data[64];
unsigned char tx_in_p;
unsigned char tx_out_p;

// timer stuff
#define TICKH 0x0b
#define TICKL 0xdb
unsigned char beep_timeout;
unsigned char green_timeout;
unsigned char strike_timeout;

// HID stuff
#define MAX_HID 64
unsigned char hid_data[64];

// GPIO input debounce stuff
unsigned char gpio_debounced, debounce_A, debounce_B, debounce_C;
#define GPIO_DIRTY (1 << 0)  // Steal bit 0 for use as dirty flag

// function prototypes
void rs232_rx(unsigned char);
void rs232_tx(unsigned char);
void rs232_tx_task(void);
void process_rx_cmd(void);
void send_error(void);
void send_ok(void);
void read_hid(void);
void send_inputs(void);


/*
 * Main.
 */
void main() {
	// set up the WDT timeout (1:128 prescaler)
	// enable RBPU
	option_reg = 0x0f;

	// set up I/O
	porta = 0x00;
	cmcon = 0x07;
	trisa = 0xb3;
	porta = 0;
	trisb.0 = 0;
	portb = 0;
	HID_GRN_LED = 1;
	HID_BEEP = 1;
	
	// set up USART
	trisb.1 = 1;
	trisb.2 = 1;
	spbrg = 25;		// 9600 bps
	txsta.BRGH = 1;  // high speed
	txsta.SYNC = 0;  // async mode
	txsta.TX9 = 0;  // 8 bit transmission
	txsta.TXEN = 1;  // enable transmission
	rcsta.OERR = 0;  // clear overrun error
	rcsta.SPEN = 1;  // enable serial port
	rcsta.CREN = 1;  // continuous reception
	rx_state = RX_CMD;
	
	// set up the timer
	t1con = 0x01;
	tmr1h = TICKH;
	tmr1l = TICKL;
	beep_timeout = 0;
	green_timeout = 0;
	strike_timeout = 0;
	
	// reset the transmitter
	tx_in_p = 0;
	tx_out_p = 0;
	
	// debounce state
	gpio_debounced = portb & GPIO_MASK;
	gpio_debounced |= GPIO_DIRTY;
	debounce_A = 0;
	debounce_B = 0;
	debounce_C = 0;
	
	// bootup message
	rs232_tx('M');
	rs232_tx('o');
	rs232_tx('o');
	rs232_tx('!');
	rs232_tx('\n');
	
	while(1) {
		// get RS232 bytes
		if(pir1.RCIF) {
			rs232_rx(rcreg);
		}
		// clear potential errors on the USART
		if(rcsta.OERR) {
			rcsta.CREN = 0;
			rcsta.CREN = 1;
		}

		// strike
		if(strike_timeout) STRIKE = 1;
		else STRIKE = 0;
		// green LED
		if(green_timeout) HID_GRN_LED = 0;
		else HID_GRN_LED = 1;
		// beep
		if(beep_timeout) HID_BEEP = 0;
		else HID_BEEP = 1;

		// timer task - goes off 16 times per second
		if(pir1.TMR1IF) {
			pir1.TMR1IF = 0;
			tmr1h = TICKH;
			tmr1l = TICKL;
			// strike timeout
			if(strike_timeout && strike_timeout != 0xff) {
				strike_timeout --;
			}
			// green LED timeout
			if(green_timeout && green_timeout != 0xff) {
				green_timeout --;
			}
			// beep timeout
			if(beep_timeout && beep_timeout != 0xff) {
				beep_timeout --;
			}
		}
		
		// HID card read
		if(HID_DATA == 0) {
			read_hid();
		}
		
		// general purpose inputs
		{
            // This debounce algorithm is based on Scott Dattalo's clever
            // "vertical counter" bitslice saturating arithmetic scheme
            // http://www.dattalo.com/technical/software/pic/vertcnt.html
		
		    // Increment all counters
		    // (to understand this, look at Dattalo's Karnaugh maps & algebra)
		    // (this is his counter_6t translated back to C code)
		    debounce_B ^= debounce_C;
		    debounce_B |= debounce_A;
		    debounce_C = debounce_A | ~( debounce_C );
		    debounce_A |= ~(debounce_B | debounce_C);
		    
            unsigned char changes = ( portb ^ gpio_debounced ) & GPIO_MASK;

            // For any pins that are not currently different from their
            // debounced state, reset their counters to 0
            debounce_A &= changes;
            debounce_B &= changes;
            debounce_C &= changes;
		    
		    // Any counters which have reached their maximum value
		    // indicate a pin which has settled down to a state
		    // other than its debounced state
		    changes = (debounce_A&debounce_B&debounce_C);
		    
		    if (changes) {
		        // Incorporate the changed bits in our debounced state
		        // (note that it is impossible for the dirty-flag bit of changes
		        // to be 1 here because we masked it off when reading from portb)
		        gpio_debounced ^= changes;
		        // Set the dirty flag
		        gpio_debounced |= GPIO_DIRTY;
		    }
        }
		    
		if ((gpio_debounced & GPIO_DIRTY) &&
            // Only transmit gpio changes if the tx buffer is empty - this
            // will keep us from overfilling our buffer if one of the
            // inputs starts chattering.
            tx_in_p == tx_out_p) {
                send_inputs();
        }
		
		// try to send bytes that are waiting to send
		rs232_tx_task();
		clear_wdt();
	}
}


	
/*
 * Receive bytes from the USART.
 */
void rs232_rx(unsigned char rx_byte) {
	if(rx_state == RX_NL) {
		if(rx_byte == 0x0a || rx_byte == 0x0d) {
			process_rx_cmd();
		}
		rx_state = RX_CMD;
		return;
	}
	// ignore some characters
	if(rx_byte < 0x21) {
		rx_state = RX_CMD;
		return;
	}
	if(rx_state == RX_CMD) {
		cmd = rx_byte;
		rx_state = RX_DATA;
		return;
	}
	if(rx_state == RX_DATA) {
		data = rx_byte;
		rx_state = RX_NL;
		return;
	}
}


/*
 * Queue a byte for transmitting.
 */
void rs232_tx(unsigned char byte) {
	tx_in_p ++;
	tx_in_p &= 0x3f;
	tx_data[tx_in_p] = byte;
}


/*
 * Transmit a byte from the buffer if possible.
 */
void rs232_tx_task() {
	if(tx_in_p == tx_out_p) return;
	if(!txsta.TRMT) return;
	tx_out_p ++;
	tx_out_p &= 0x3f;
	txreg = tx_data[tx_out_p];
}


/*
 * Process a command received from the host.
 */
void process_rx_cmd(void) {
	// beep
	if(cmd == 'b' || cmd == 'B') {
		if(data == 'L' || data == 'l') {
			beep_timeout = 0xff;
			return;
		}
		if(data < 0x30 || data > 0x39) return;
		beep_timeout = (data & 0x0f) << 4;
	}
	// green LED
	if(cmd == 'g' || cmd == 'G') {
		if(data == 'L' || data == 'l') {
			green_timeout = 0xff;
			return;
		}
		if(data < 0x30 || data > 0x39) return;
		green_timeout = (data & 0x0f) << 4;
	}
	// strike
	if(cmd == 's' || cmd == 'S') {
		if(data == 'L' || data == 'l') {
			strike_timeout = 0xff;
			return;
		}
		if(data < 0x30 || data > 0x39) return;
		strike_timeout = (data & 0x0f) << 4;
	}	
	// status
	if(cmd == '?') {
		rs232_tx('?');
		if(beep_timeout) rs232_tx('1');
		else rs232_tx('0');
		if(green_timeout) rs232_tx('1');
		else rs232_tx('0');
		if(strike_timeout) rs232_tx('1');
		else rs232_tx('0');
		rs232_tx('\n');
		send_inputs();
	}
}


/*
 * Read the HID card.
 */
void read_hid(void) {
	unsigned char i, j;
	int timeout;
	
	// read each word from the card into a byte
	for(j = 0; j < (MAX_HID - 1); j ++) {
		hid_data[j] = 0;
		// read each byte, LSB first
		for(int i = 1; i < 0x20; i = (i << 1)) {
			timeout = 65535;
			while(HID_CLOCK == 1) {
				clear_wdt();  // wait for the negative edge
				timeout --;
				if(timeout == 0) return;  // timed out
			}
			delay_us(10);
			if(HID_DATA == 0) hid_data[j] |= i;  // save the inverted bit
			timeout = 65535;
			while(HID_CLOCK == 0) {
				clear_wdt();  // wait for the positive edge
				timeout --;
				if(timeout == 0) return;  // timed out
			}
			delay_us(10);
		}
		if(hid_data[j] == 0) break;
		hid_data[j] |= 0x20;
	}
	
	// send the card number
	rs232_tx('H');
	for(i = 0; i < j; i ++) {
		rs232_tx(hid_data[i]);
	}
 	rs232_tx('\n');
}

/*
 * Report the state of the GPIO pins
 */
void send_inputs(void) {
    unsigned char ch = ( gpio_debounced >> 3 ) | 0x20;
	    
 	rs232_tx('i');
 	rs232_tx(ch);
    rs232_tx(ch ^ 0x1F);
    rs232_tx('\n');
        
    gpio_debounced &= ~ GPIO_DIRTY;
}


