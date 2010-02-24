package com.onestopmediagroup.doorsecurity;

public class WeigandFaker {
	public static String generateWeigand(int facilityId, int cardId)
	{
		String outFormatted = "";
		int outValue = (facilityId<<17) | (cardId << 1);
		
		for (int i=0; i<16; i++){
			outFormatted = ((outValue & 0x7)) + outFormatted;
			//System.out.println(outValue & 0x7);
			outValue = outValue >> 3;
		}
		
		return outFormatted;
	}
	public static void main(String[] args){
		if (args.length != 2)
		{
			System.out.println("Two arguments expected: Facility ID, Card ID\n");
			System.exit(0);
		}
		
		int facilityId = Integer.parseInt(args[0]);
		int cardId = Integer.parseInt(args[1]);
		
		System.out.println("H"+((char)0x0b)
				+generateWeigand(facilityId, cardId)
				+((char)0x0f));
		
	}

}
