package org.svenk.redmine.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedmineTicketProgress extends RedmineTicketAttribute {

	private static List<RedmineTicketProgress> availableValues;
	
	private RedmineTicketProgress(int value) {
		super(value + " %", value);
	}

	private static final long serialVersionUID = 1L;

	public static List<RedmineTicketProgress> availableValues() {
		if (availableValues==null) {
			availableValues = new ArrayList<RedmineTicketProgress>(10);
			for(int i=0; i<=10; i++) {
				availableValues.add(new RedmineTicketProgress(i*10));
			}
			availableValues = Collections.unmodifiableList(availableValues);
		}
		return availableValues;
	}
	
	public static String getDefaultValue() {
		return availableValues.get(0).getValue() + "";
	}
	
}
