/*
 * Copyright 2007-2107 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ymate.platform.base.YMP;
import net.ymate.platform.commons.i18n.I18N;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.validation.annotation.Validate;
import net.ymate.platform.validation.annotation.ValidateRule;
import net.ymate.platform.validation.annotation.Validation;
import net.ymate.platform.validation.impl.CompareValidator;
import net.ymate.platform.validation.impl.DateValidator;
import net.ymate.platform.validation.impl.EmailValidator;
import net.ymate.platform.validation.impl.LengthValidator;
import net.ymate.platform.validation.impl.NumericValidator;
import net.ymate.platform.validation.impl.RegexValidator;
import net.ymate.platform.validation.impl.RequriedValidator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Validates
 * </p>
 * <p>
 * 验证器管理类；
 * </p>
 * 
 * @author 刘镇(suninformation@163.com)
 * @version 0.0.0
 *          <table style="border:1px solid gray;">
 *          <tr>
 *          <th width="100px">版本号</th><th width="100px">动作</th><th
 *          width="100px">修改人</th><th width="100px">修改时间</th>
 *          </tr>
 *          <!-- 以 Table 方式书写修改历史 -->
 *          <tr>
 *          <td>0.0.0</td>
 *          <td>创建类</td>
 *          <td>刘镇</td>
 *          <td>2013-4-7下午4:43:48</td>
 *          </tr>
 *          </table>
 */
public class Validates {

	private static final Log _LOG = LogFactory.getLog(Validates.class);

	/**
	 * 注册的验证器映射
	 */
	protected static Map<String, IValidator> __VALIDATOR_MAPS;

	static {
		__VALIDATOR_MAPS = new HashMap<String, IValidator>();
		registerValidatorClass(RequriedValidator.class);
		registerValidatorClass(RegexValidator.class);
		registerValidatorClass(EmailValidator.class);
		registerValidatorClass(LengthValidator.class);
		registerValidatorClass(DateValidator.class);
		registerValidatorClass(NumericValidator.class);
		registerValidatorClass(CompareValidator.class);
	}

	/**
	 * 注册验证器
	 * 
	 * @param validatorClass 目标验证器类对象
	 */
	public static void registerValidatorClass(Class<? extends IValidator> validatorClass) throws ValidationException {
		IValidator _targetObj = null;
		try {
			_LOG.info(I18N.formatMessage(YMP.__LSTRING_FILE, null, null, "ymp.validation.register_validator", validatorClass.getName()));
			_targetObj = validatorClass.newInstance();
			__VALIDATOR_MAPS.put(_targetObj.getName(), _targetObj);
		} catch (Exception e) {
			throw new ValidationException(RuntimeUtils.unwrapThrow(e));
		}
	}

	/**
	 * @param targetClass 目标验证对象
	 * @return 返回目标对象的成员验证规则
	 */
	public static PairObject<Validation, Map<String, ValidateRule[]>> loadValidateRule(Class<?> targetClass) {
		Map<String, ValidateRule[]> _returnValue = null;
		Validation _validation = targetClass.getAnnotation(Validation.class);
		if (_validation != null) {
			_returnValue = new HashMap<String, ValidateRule[]>();
			List<PairObject<Field, Validate>> _fieldAnnotations = ClassUtils.getFieldAnnotations(targetClass, Validate.class, false);
			for (PairObject<Field, Validate> _fieldAnno : _fieldAnnotations) {
				if (_fieldAnno.getValue().isModel() && _fieldAnno.getValue().value().length > 0) {
					Map<String, ValidateRule[]> _modelVMap = loadValidateRule(_fieldAnno.getKey().getType()).getValue();
					if (_modelVMap != null) {
						_returnValue.putAll(_modelVMap);
					}
				} else {
					_returnValue.put(StringUtils.defaultIfEmpty(_fieldAnno.getValue().name(), _fieldAnno.getKey().getName()), _fieldAnno.getValue().value());
				}
			}
		}
		return new PairObject<Validation, Map<String, ValidateRule[]>>(_validation, _returnValue);
	}

	/**
	 * @param targetMethod 目标验证方法对象
	 * @param fieldNames 指定方法参数名称集合
	 * @return 返回目标方法的参数验证规则
	 */
	public static PairObject<Validation, Map<String, ValidateRule[]>> loadValidateRule(Method targetMethod, String[] fieldNames) {
		Map<String, ValidateRule[]> _returnValue = null;
		Validation _validation = targetMethod.getAnnotation(Validation.class);
		if (_validation != null) {
			_returnValue = new HashMap<String, ValidateRule[]>();
			Annotation[][] _paramAnnotations = targetMethod.getParameterAnnotations();
			for (int _idx = 0; _idx < targetMethod.getParameterTypes().length; _idx++) {
				Annotation[] _annotations = _paramAnnotations[_idx];
				for (Annotation _annotation : _annotations) {
					if (_annotation instanceof Validate) {
						Validate _validate = (Validate) _annotation;
						if (_validate.isModel()) {
							Map<String, ValidateRule[]> _modelVMap = loadValidateRule(targetMethod.getParameterTypes()[_idx]).getValue();
							if (_modelVMap != null) {
								_returnValue.putAll(_modelVMap);
							}
						} else {
							_returnValue.put(StringUtils.defaultIfEmpty(_validate.name(), fieldNames[_idx]), _validate.value());
						}
						break;
					}
				}
			}
		}
		return new PairObject<Validation, Map<String, ValidateRule[]>>(_validation, _returnValue);
	}

	/**
	 * @param validation 验证注解对象，用于提取配置
	 * @param ruleMap 验证规则配置，根根fieldName匹配
	 * @param fieldValues 字段名称与值对象映射
	 * @return 执行验证并返回验证结果集合
	 */
	public static Set<ValidateResult> execute(final Validation validation, Map<String, ValidateRule[]> ruleMap, final Map<String, Object> fieldValues) {
		Set<ValidateResult> _resultValue = new HashSet<ValidateResult>();
		for (final String _fieldName : fieldValues.keySet()) {
			ValidateRule[] _rules = ruleMap.get(_fieldName);
			if (_rules != null && _rules.length > 0) {
				for (final ValidateRule _rule : _rules) {
					IValidator _validator = __VALIDATOR_MAPS.get(_rule.value());
					if (_validator != null) {
						String _result = _validator.validate(new IValidateContext() {
	
							public String getFieldName() {
								return _fieldName;
							}
	
							public Object getFieldValue() {
								return fieldValues.get(_fieldName);
							}
	
							public String[] getParams() {
								return _rule.params();
							}
	
							public Object getFieldValue(String fieldName) {
								return fieldValues.get(fieldName);
							}
	
							public String getMessage() {
								return _rule.message();
							}
							
						});
						_LOG.info(I18N.formatMessage(YMP.__LSTRING_FILE, null, null, "ymp.validation.execute_validator", _validator.getName(), _fieldName, StringUtils.isBlank(_result)));
						if (StringUtils.isNotBlank(_result)) {
							_resultValue.add(new ValidateResult(_fieldName, _result));
							break;
						}
					}
				}
			}
			if (!validation.fullMode()) {
				break;
			}
		}
		return _resultValue;
	}

}
