/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2008 Sven Krzyzak
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sven Krzyzak - adapted Trac implementation for Redmine
 *******************************************************************************/
package org.svenk.redmine.core.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedmineSearchFilter {

	public enum CompareOperator {
		CONTAINS("~"), CONTAINS_NOT("!~"), IS("="), IS_NOT("!"), ALL("*"), NONE(
				"!*"), OPEN("o"), CLOSED("c"), GTE(">="), LTE("<="), DAY_AGO_MORE_THEN(
				"<t-"), DAY_AGO_LESS_THEN(">t-"), DAY_AGO("t-"), TODAY("t"), CURRENT_WEEK(
				"w"), DAY_LATER("t+"), DAY_LATER_LESS_THEN("<t+"), DAY_LATER_MORE_THEN(
				">t+");

		public static CompareOperator fromQueryValue(String value) {
			for (CompareOperator operator : values()) {
				// if (operator != IS && operator != IS_NOT
				// && value.startsWith(operator.queryValue)) {
				if (value.equals(operator.queryValue)) {
					return operator;
				}
			}
			// if (value.startsWith(IS_NOT.queryValue)) {
			// return IS_NOT;
			// }
			return IS;
		}

		public static CompareOperator fromString(String value) {
			for (CompareOperator operator : values()) {
				// if (operator != IS && operator != IS_NOT
				// && value.startsWith(operator.queryValue)) {
				if (value.equals(operator.toString())) {
					return operator;
				}
			}
			// if (value.startsWith(IS_NOT.queryValue)) {
			// return IS_NOT;
			// }
			return IS;
		}
		
		public static CompareOperator fromName(String name) {
			for (CompareOperator operator : values()) {
				if (operator.name().equals(name)) {
					return operator;
				}
			}
			return IS;
		}
		
		private String queryValue;

		CompareOperator(String queryValue) {
			this.queryValue = queryValue;
		}

		public String getQueryValue() {
			return queryValue;
		}

		@Override
		public String toString() {
			switch (this) {
			case CONTAINS:
				return "contains";
			case CONTAINS_NOT:
				return "does not contain";
			case IS:
				return "is";
			case IS_NOT:
				return "is not";
			case ALL:
				return "all";
			case NONE:
				return "none";
			case OPEN:
				return "open";
			case CLOSED:
				return "closed";
			case GTE:
				return "greater then";
			case LTE:
				return "less then";
			case DAY_AGO_MORE_THEN:
				return "more then (days) ago";
			case DAY_AGO_LESS_THEN:
				return "less then (days) ago";
			case DAY_AGO:
				return "day ago";
			case TODAY:
				return "today";
			case CURRENT_WEEK:
				return "curent week";
			case DAY_LATER:
				return "day later";
			case DAY_LATER_LESS_THEN:
				return "less then (days) later";
			case DAY_LATER_MORE_THEN:
				return "more then (days) later";
			default:
				return queryValue;
			}
		}

		public boolean useValue() {
			switch (this) {
			case ALL:
			case NONE:
			case OPEN:
			case CLOSED:
			case TODAY:
			case CURRENT_WEEK:
				return false;
			default:
				return true;
			}
		}

	}

	public enum SearchField {

		STATUS("status_id", true, CompareOperator.OPEN, CompareOperator.IS,
				RedmineSearchFilter.CompareOperator.IS_NOT,
				CompareOperator.CLOSED, CompareOperator.ALL), TRACKER(
				"tracker_id", CompareOperator.IS, CompareOperator.IS_NOT), PRIORITY(
				"priority_id", CompareOperator.IS, CompareOperator.IS_NOT), ASSIGNED_TO(
				"assigned_to_id", CompareOperator.IS, CompareOperator.IS_NOT,
				CompareOperator.NONE, CompareOperator.ALL), AUTHOR("author_id",
				CompareOperator.IS, CompareOperator.IS_NOT), FIXED_VERSION(
				"fixed_version_id", CompareOperator.IS, CompareOperator.IS_NOT,
				CompareOperator.NONE, CompareOperator.ALL),CATEGORY("category_id",
				CompareOperator.IS,	CompareOperator.IS_NOT, CompareOperator.ALL, 
				CompareOperator.NONE),SUBJECT("subject",
				CompareOperator.CONTAINS, CompareOperator.CONTAINS_NOT), DATE_CREATED(
				"created_on", CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK), DATE_UPDATED(
				"updated_on", CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK), DATE_START(
				"start_date", CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK,
				CompareOperator.DAY_LATER, CompareOperator.DAY_LATER_LESS_THEN,
				CompareOperator.DAY_LATER_MORE_THEN), DATE_DUE("start_date",
				CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK,
				CompareOperator.DAY_LATER, CompareOperator.DAY_LATER_LESS_THEN,
				CompareOperator.DAY_LATER_MORE_THEN), DONE_RATIO("done_ratio",
				CompareOperator.GTE, CompareOperator.LTE);

		public static SearchField fromString(String name) {
			for (SearchField field : values()) {
				if (field.toString().equals(name)) {
					return field;
				}
			}
			return null;
		}

		public static SearchField fromName(String name) {
			for (SearchField field : values()) {
				if (field.name().equals(name)) {
					return field;
				}
			}
			return null;
		}
		
		private String fieldName;
		
		private boolean required;

		private List<CompareOperator> operators;

		SearchField(String fieldName, boolean required, CompareOperator... operators) {
			this.fieldName = fieldName;
			this.required = required;
			this.operators = new ArrayList<CompareOperator>(operators.length);
			for (CompareOperator compareOperator : operators) {
				this.operators.add(compareOperator);
			}
		}

		SearchField(String fieldName, CompareOperator... operators) {
			this(fieldName, false, operators);
		}
		
		public boolean containsOperator(CompareOperator operator) {
			return operators.contains(operator);
		}

		public List<CompareOperator> getCompareOperators() {
			return operators;
		}

		public boolean isRequired() {
			return required;
		}

		public String getQueryValue() {
			return fieldName;
		}

		public String toString() {
			return fieldName;
		}
	}

	private SearchField searchField;

	private CompareOperator operator = CompareOperator.IS;

	private List<String> values = new ArrayList<String>();

	public RedmineSearchFilter(SearchField field) {
		this.searchField = field;
	}

	public void addValue(String value) {
		if (operator.useValue()) {
			values.add(value);
		}
	}

	public CompareOperator getOperator() {
		return operator;
	}

	public SearchField getSearchField() {
		return searchField;
	}
	
	public List<String> getValues() {
		return values;
	}

	public void setOperator(CompareOperator operator) {
		if (searchField.containsOperator(operator)) {
			this.operator = operator;
		} else {
			this.operator = null;
		}
	}
	
	StringBuffer appendUrlPart(StringBuffer sb) {
		int length = sb.length();
		try {
			if (searchField != null && operator != null) {
				if (!operator.useValue()) {
					values.clear();
				}
				switch (searchField) {
				case STATUS:
				case TRACKER:
				case AUTHOR:
				case PRIORITY:
				case CATEGORY:
				case FIXED_VERSION:
				case ASSIGNED_TO: {
					if (searchField.containsOperator(operator)) {
						if (!operator.useValue() || values.size()>0) {
							appendFieldAndOperator(sb);
							appendValues(sb);
						}
					}
					break;
				}
				case SUBJECT: {
					if (searchField.containsOperator(operator)
							&& values.size() == 1) {
						appendFieldAndOperator(sb);
						appendValues(sb);
					}
					break;
				}
				case DONE_RATIO: {
					if (searchField.containsOperator(operator)
							&& values.size() == 1) {
						try {
							int v = Integer.parseInt(values.get(0));
							if (0 <= v && v <= 100) {
								appendFieldAndOperator(sb);
								appendValues(sb);
							}
						} catch (NumberFormatException ex) {
							;
						}
					}
					break;
				}
				case DATE_START:
				case DATE_DUE:
				case DATE_CREATED:
				case DATE_UPDATED: {
					if (operator == CompareOperator.TODAY
							|| operator == CompareOperator.CURRENT_WEEK) {
						appendFieldAndOperator(sb);
						appendValues(sb);
					} else if (searchField.containsOperator(operator)
							&& values.size() == 1) {
						try {
							int v = Integer.parseInt(values.get(0));
							if (v > 0) {
								appendFieldAndOperator(sb);
								appendValues(sb);
							}
						} catch (NumberFormatException ex) {
							;
						}
					}
					break;
				}
				}
			}
		} catch (UnsupportedEncodingException ex) {
			sb.setLength(length);
		}
		return sb;
	}

	private void appendFieldAndOperator(StringBuffer sb)
			throws UnsupportedEncodingException {
		sb.append("&fields[]=").append(
				URLEncoder.encode(searchField.getQueryValue(), "UTF-8"));
		sb.append("&operators[").append(searchField.getQueryValue()).append(
				"]=").append(
				URLEncoder.encode(operator.getQueryValue(), "UTF-8"));
	}

	private void appendValues(StringBuffer sb)
			throws UnsupportedEncodingException {
		if (values.size() > 0) {
			for (String value : values) {
				sb.append("&values[").append(searchField.getQueryValue())
						.append("]");
				sb.append("[]=").append(URLEncoder.encode(value, "UTF-8"));
			}
		} else {
			sb.append("&values[").append(searchField.getQueryValue()).append(
					"][]");
		}
	}
	
	public static List<SearchField> findFieldsFromSearchQueryParam(String param) {
		List<SearchField> fields = new ArrayList<SearchField>();
		Matcher matcher = Pattern.compile("&fields\\[\\]=(\\w+)").matcher(param);
		while (matcher.find()) {
			SearchField field = SearchField.fromString(matcher.group(1));
			if (field != null) {
				fields.add(field);
			}
		}
		return fields;
	}
	
	public static CompareOperator findOperatorFromSearchQueryParam(String param, SearchField searchField) {
		StringBuffer pattern = new StringBuffer(".*&operators\\[");
		pattern.append(searchField.getQueryValue());
		pattern.append("\\]=([^&]+).*");
		
		Matcher matcher = Pattern.compile(pattern.toString()).matcher(param);
		if (matcher.matches()) {
			try {
				return CompareOperator.fromQueryValue(URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {;}
		}
		return null;
	}
	
	public static List<String> findValuesFromSearchQueryParam(String param, SearchField searchField) {
		StringBuffer pattern = new StringBuffer("&values\\[");
		pattern.append(searchField.getQueryValue());
		pattern.append("\\]\\[\\]=([^&]+)");
		
		List<String> values = new ArrayList<String>();
		Matcher matcher = Pattern.compile(pattern.toString()).matcher(param);
		while (matcher.find()) {
			try {
				values.add(URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {;}
		}
		
		return values;
	}
}
