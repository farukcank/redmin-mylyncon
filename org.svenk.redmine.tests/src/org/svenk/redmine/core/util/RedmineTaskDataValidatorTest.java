package org.svenk.redmine.core.util;

import java.lang.reflect.Constructor;

import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineCustomField.FieldType;
import org.svenk.redmine.core.util.RedmineTaskDataValidator.RedmineTaskDataValidatorResult;

import junit.framework.TestCase;

public class RedmineTaskDataValidatorTest extends TestCase {

	private RedmineTaskDataValidator validator;
	private RedmineTaskDataValidatorResult result;
	private RedmineCustomField stringField, intField, floatField;
	
	protected void setUp() throws Exception {
		validator = new RedmineTaskDataValidator(null);
		
		Constructor<RedmineTaskDataValidatorResult> constr = RedmineTaskDataValidatorResult.class.getConstructor(RedmineTaskDataValidator.class);
		constr.setAccessible(true);
		result = constr.newInstance(validator);
		
		stringField = new RedmineCustomField(1, FieldType.STRING.name());
		stringField.setName("stringField");
		stringField.setMin(2);
		stringField.setMax(2);
		stringField.setValidationRegex("^[A-Z]\\w+$");
		stringField.setRequired(true);
		
		intField = new RedmineCustomField(1, FieldType.INT.name());
		intField.setName("intField");

		floatField = new RedmineCustomField(1, FieldType.FLOAT.name());
		floatField.setName("floatField");
	}

	public void testRedmineTaskDataValidator() {
		fail("Not yet implemented");
	}

	public void testValidate() {
		fail("Not yet implemented");
	}

	public void testGetFirstErrorMessage() {
		fail("Not yet implemented");
	}

	public void testGetErrorMessages() {
		fail("Not yet implemented");
	}

	public void testValidateDefaultAttributes() {
		fail("Not yet implemented");
	}

	public void testValidateCustomAttributes() {
		fail("Not yet implemented");
	}

	public void testValidateRequiredDefaultAttributes() {
		fail("Not yet implemented");
	}

	public void testValidateRequiredCustomAttribute() {
		validator.validateRequiredCustomAttribute("Abc", intField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateRequiredCustomAttribute("", intField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateRequiredCustomAttribute("Abc", stringField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateRequiredCustomAttribute("", stringField, result);
		assertEquals(1, result.getErrorMessages().size());
	}

	public void testValidateCustomAttributeMinLength() {
		validator.validateCustomAttributeMinLength("", intField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateCustomAttributeMinLength("abc", intField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeMinLength("ab", stringField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeMinLength("abc", stringField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeMinLength("a", stringField, result);
		assertEquals(1, result.getErrorMessages().size());
	}

	public void testValidateCustomAttributeMaxLength() {
		validator.validateCustomAttributeMaxLength("", intField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateCustomAttributeMaxLength("ab", intField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeMaxLength("a", stringField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeMaxLength("ab", stringField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeMaxLength("abc", stringField, result);
		assertEquals(1, result.getErrorMessages().size());
	}

	public void testValidateCustomAttributePattern() {
		validator.validateCustomAttributePattern("", intField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateCustomAttributePattern("abc", intField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateCustomAttributePattern("Abc", stringField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributePattern("abc", stringField, result);
		assertEquals(1, result.getErrorMessages().size());
	}

	public void testValidateCustomAttributeType() {
		validator.validateCustomAttributeType("123", intField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeType("123", floatField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeType("123.123", floatField, result);
		assertEquals(0, result.getErrorMessages().size());
		
		validator.validateCustomAttributeType("abc", stringField, result);
		assertEquals(0, result.getErrorMessages().size());

		validator.validateCustomAttributeType("abc", intField, result);
		assertEquals(1, result.getErrorMessages().size());
		
		validator.validateCustomAttributeType("1.2", intField, result);
		assertEquals(2, result.getErrorMessages().size());
		
		validator.validateCustomAttributeType("abc", floatField, result);
		assertEquals(3, result.getErrorMessages().size());
		
		validator.validateCustomAttributeType("1,2", floatField, result);
		assertEquals(4, result.getErrorMessages().size());
	}

}
