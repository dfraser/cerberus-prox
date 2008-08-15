package com.onestopmediagroup.doorsecurity;

public class AccessInfo {

	private final boolean allowed;
	private final boolean magic;

	public AccessInfo(boolean allowed, boolean magic) {
		this.allowed = allowed;
		this.magic = magic;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public boolean isMagic() {
		return magic;
	}

}
