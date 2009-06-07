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

import org.svenk.redmine.core.client.RedmineProjectData;

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

	public enum SearchField implements IRedmineQueryField {

		LIST_BASED("LIST_BASED", true, false, true, CompareOperator.IS, CompareOperator.IS_NOT,
				CompareOperator.NONE, CompareOperator.ALL),
		TEXT_BASED("TEXT_BASED", true, false, false, CompareOperator.IS, CompareOperator.IS_NOT, 
				CompareOperator.CONTAINS, CompareOperator.CONTAINS_NOT),
		DATE_BASED("DATE_BASED", true, false, false, CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK,
				CompareOperator.DAY_LATER, CompareOperator.DAY_LATER_LESS_THEN,
				CompareOperator.DAY_LATER_MORE_THEN),
		BOOLEAN_BASED("BOOLEAN_BASED", true, false, false, CompareOperator.IS, CompareOperator.IS_NOT),
		STATUS("status_id", false, true, true, CompareOperator.OPEN, CompareOperator.IS,
				CompareOperator.IS_NOT,
				CompareOperator.CLOSED, CompareOperator.ALL),
		PRIORITY("priority_id", true, CompareOperator.IS, CompareOperator.IS_NOT),
		TRACKER("tracker_id", true, CompareOperator.IS, CompareOperator.IS_NOT),
		FIXED_VERSION("fixed_version_id", true, CompareOperator.IS, CompareOperator.IS_NOT,
				CompareOperator.NONE, CompareOperator.ALL),
		ASSIGNED_TO("assigned_to_id", true, CompareOperator.IS, CompareOperator.IS_NOT,
				CompareOperator.NONE, CompareOperator.ALL),
		AUTHOR("author_id", true, CompareOperator.IS, CompareOperator.IS_NOT),
		CATEGORY("category_id", true, CompareOperator.IS,	CompareOperator.IS_NOT, CompareOperator.ALL, 
				CompareOperator.NONE),
		SUBJECT("subject",CompareOperator.CONTAINS, CompareOperator.CONTAINS_NOT),
		DATE_CREATED("created_on", CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK),
		DATE_UPDATED("updated_on", CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK),
		DATE_START("start_date", CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK,
				CompareOperator.DAY_LATER, CompareOperator.DAY_LATER_LESS_THEN,
				CompareOperator.DAY_LATER_MORE_THEN),
		DATE_DUE("start_date",CompareOperator.DAY_AGO_MORE_THEN,
				CompareOperator.DAY_AGO_LESS_THEN, CompareOperator.DAY_AGO,
				CompareOperator.TODAY, CompareOperator.CURRENT_WEEK,
				CompareOperator.DAY_LATER, CompareOperator.DAY_LATER_LESS_THEN,
				CompareOperator.DAY_LATER_MORE_THEN),
		DONE_RATIO("done_ratio",CompareOperator.GTE, CompareOperator.LTE);

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
		
		private boolean listType;
		
		private boolean generic;

		private List<CompareOperator> operators;

		SearchField(String fieldName, CompareOperator... operators) {
			this(fieldName, false, false, false, operators);
		}

		SearchField(String fieldName, boolean listType, CompareOperator... operators) {
			this(fieldName, false, false, listType, operators);
		}

		SearchField(String fieldName, boolean generic, boolean required, boolean listType, CompareOperator... operators) {
			this.fieldName = fieldName;
			this.required = required;
			this.listType = listType;
			this.generic = generic;
			this.operators = new ArrayList<CompareOperator>(operators.length);
			for (CompareOperator compareOperator : operators) {
				this.operators.add(compareOperator);
			}
		}
		
		public static SearchField fromCustomTicketField(RedmineCustomTicketField field) {
			switch (field.getType()) {
				case LIST: return LIST_BASED;
				case DATE: return DATE_BASED;
				case BOOL: return BOOLEAN_BASED;
			}
			return TEXT_BASED;
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

		public boolean isGeneric() {
			return generic;
		}

		public boolean isListType() {
			return listType;
		}
		
		
		public String getQueryValue() {
			return fieldName;
		}

		public String getLabel() {
			return name();
		}
		
		public String toString() {
			return fieldName;
		}


	}

	private SearchField searchField;
	
	private IRedmineQueryField queryField;

	private CompareOperator operator = CompareOperator.IS;

	private List<String> values = new ArrayList<String>();

	public RedmineSearchFilter(SearchField field) {
		this.searchField = field;
		this.queryField = field;
	}

	public RedmineSearchFilter(RedmineCustomTicketField field) {
		this.queryField = field;
		this.searchField = SearchField.fromCustomTicketField(field);
	}
	
	public void addValue(String value) {
		if (operator!=null && operator.useValue()) {
			values.add(value);
		}
	}

	public CompareOperator getOperator() {
		return operator;
	}

	public RedmineCustomTicketField getCustomTicketField() {
		return (queryField instanceof RedmineCustomTicketField) ? 
				(RedmineCustomTicketField)queryField : null;
	}
	
	public IRedmineQueryField getQueryField() {
		return queryField;
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
		values.clear();
	}
	
	StringBuffer appendUrlPart(StringBuffer sb) {
		int length = sb.length();
		try {
			if (searchField != null && operator != null && searchField.containsOperator(operator)) {
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
				case ASSIGNED_TO: 
				case LIST_BASED: {
					if (!operator.useValue() || values.size()>0) {
						appendFieldAndOperator(sb);
						appendValues(sb);
					}
					break;
				}
				case SUBJECT: 
				case TEXT_BASED: {
					if (values.size() == 1) {
						appendFieldAndOperator(sb);
						appendValues(sb);
					}
					break;
				}
				case BOOLEAN_BASED: {
					if (values.size() == 1) {
						try {
							int v = Integer.parseInt(values.get(0));
							if (v==0 || v==1) {
								appendFieldAndOperator(sb);
								appendValues(sb);
							}
						} catch (NumberFormatException ex) {
							;
						}
					}
					break;
				}
				case DONE_RATIO: {
					if (values.size() == 1) {
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
				case DATE_BASED:
				case DATE_START:
				case DATE_DUE:
				case DATE_CREATED:
				case DATE_UPDATED: {
					if (operator == CompareOperator.TODAY
							|| operator == CompareOperator.CURRENT_WEEK) {
						appendFieldAndOperator(sb);
						appendValues(sb);
					} else if (values.size() == 1) {
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
				URLEncoder.encode(queryField.getQueryValue(), "UTF-8"));
		sb.append("&operators[").append(queryField.getQueryValue()).append(
				"]=").append(
				URLEncoder.encode(operator.getQueryValue(), "UTF-8"));
	}

	private void appendValues(StringBuffer sb)
			throws UnsupportedEncodingException {
		if (values.size() > 0) {
			for (String value : values) {
				sb.append("&values[").append(queryField.getQueryValue())
						.append("]");
				sb.append("[]=").append(URLEncoder.encode(value, "UTF-8"));
			}
		} else {
			sb.append("&values[").append(queryField.getQueryValue()).append(
					"][]");
		}
	}
	
	public static List<SearchField> findSearchFieldsFromSearchQueryParam(String param) {
		List<SearchField> fields = new ArrayList<SearchField>();
		Matcher matcher = Pattern.compile("&fields\\[\\]=([a-z_-]+)").matcher(param);
		while (matcher.find()) {
			SearchField field = SearchField.fromString(matcher.group(1));
			if (field != null) {
				fields.add(field);
			}
		}
		return fields;
	}
	
	public static List<RedmineCustomTicketField> findCustomTicketFieldsFromSearchQueryParam(RedmineProjectData projectData, String param) {
		List<RedmineCustomTicketField> fields = new ArrayList<RedmineCustomTicketField>();
		Matcher matcher = Pattern.compile("&fields\\[\\]=cf_(\\d+)").matcher(param);
		while (matcher.find()) {
			try {
				int id = Integer.parseInt(matcher.group(1));
				RedmineCustomTicketField customField = 
					projectData.getCustomTicketField(id);
				if (customField != null) {
					fields.add(customField);
				}
			} catch (NumberFormatException e) {
				;
			}
		}
		return fields;
	}
	
	public static CompareOperator findOperatorFromQueryFieldParam(String param, IRedmineQueryField queryField) {
		StringBuffer pattern = new StringBuffer(".*&operators\\[");
		pattern.append(queryField.getQueryValue());
		pattern.append("\\]=([^&]+).*");
		
		Matcher matcher = Pattern.compile(pattern.toString()).matcher(param);
		if (matcher.matches()) {
			try {
				return CompareOperator.fromQueryValue(URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {;}
		}
		return null;
	}
	
	public static List<String> findValuesFromQueryFieldParam(String param, IRedmineQueryField queryField) {
		StringBuffer pattern = new StringBuffer("&values\\[");
		pattern.append(queryField.getQueryValue());
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
