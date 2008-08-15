/*
 * Copyright 2008 Dan Fraser
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
 */

package com.onestopmediagroup.doorsecurity;

import org.apache.log4j.Logger;
import java.util.*;

/**
 * Class to represent a HID proximity card.
 * Currently supports 26-bit format.
 * 
 */
public class HIDCard {

	private static Logger log = Logger.getLogger(HIDCard.class);

	public final int facility;
	public final int cardNumber;
	
	private final int facilityMask = 0x1FE0000;
	private final int cardIdMask = 0x1FFFE;
	
	/**
	 * Creates a new HIDCard object given the output from Andrew's HID Reader to Serial Converter.
	 * Scans the entire byte array to find the first thing that looks like valid card data.
	 * @param readerData the card data used to initialize the object
	 * 
	 * @throws IllegalArgumentException if the card data is somehow invalid
	 * 
	 */
	public HIDCard(byte[] readerData) {
		
		int startOffset = -1;
		int endOffset = -1;
		for (int i = 0; i < readerData.length; i++) {
			if ((readerData[i] & 0x0f) == 0x0b) {
				startOffset = i;
			}
			if ((readerData[i] & 0x0f) == 0x0f) {
				endOffset = i;
			}

		}
		
		if (startOffset == -1 || endOffset == -1 || endOffset < startOffset) {
			throw new IllegalArgumentException("invalid or missing sentinels in reader string");
		}
		
		if ((endOffset - startOffset-1) != 16) {
			log.debug(Arrays.toString(readerData));
			log.debug("startoffset: "+startOffset+" endoffset: "+endOffset);
			throw new IllegalArgumentException("only 26-bit wiegand data supported, data was incorrect length, expected 16, got "+(endOffset - startOffset));
		}
		
		// loop over the valid card data and pack it into an integer
		startOffset += 8; // ignore 7 leading zeros plus the sentinel
		
		// build a packed integer representing the remaining data
		int rawCard = 0;
		for (int i = startOffset; i < endOffset; i++) {
			int val = readerData[i] & 0xF;
			rawCard = rawCard << 3;
			rawCard |= val;
		}
		
		// mask out the relevant sections and shift.
		this.cardNumber = (rawCard & cardIdMask) >> 1;
		this.facility = (rawCard & facilityMask) >> 17;
		
	}
	
	/**
	 * Returns a stringified unique card ID, suitable for determining
	 * identity through a simple string comparison.
	 * 
	 * @return a string representing the content of the card
	 */
	public String getCardId() {
		return facility+"-"+cardNumber;
	}
	
	/**
	 * Returns just the facility ID from the card.
	 * @return the facility id
	 */
	public int getFacility() {
		return facility;
	}

	/**
	 * Returns just the card ID from the card.
	 * @return the card id
	 */
	public int getCardNumber() {
		return cardNumber;
	}
	
	@Override
	public String toString() {
		return getCardId();
	}
	
}
