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
package net.ymate.platform.commons.logger.impl;

import java.io.File;
import java.util.Enumeration;

import net.ymate.platform.base.YMP;
import net.ymate.platform.commons.i18n.I18N;
import net.ymate.platform.commons.logger.AbstractLogger;
import net.ymate.platform.commons.logger.ILogger;
import net.ymate.platform.commons.logger.Logs;
import net.ymate.platform.commons.util.DateTimeUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * <p>
 * DefaultLog4JLogger
 * </p>
 * <p>
 * 标准日志记录器（基于Log4J实现）；
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
 *          <td>2012-12-21上午10:51:15</td>
 *          </tr>
 *          </table>
 */
public class DefaultLog4JLogger extends AbstractLogger implements ILogger {

    public static final String SELF_FQCN = DefaultLog4JLogger.class.getName();

    private Logger logger;

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#initialize(net.ymate.platform.commons.logger.ILogger.LogLevel, java.lang.String)
	 */
	public void initialize(LogLevel defaultLevel, String loggerName) {
		if (!__IS_INITED) {
			File cfgFile = new File(Logs.getConfig().getLogCfgFile());
			if (!cfgFile.exists()) {
				System.err.println(I18N.formatMessage(YMP.__LSTRING_FILE, null, null, "ymp.commons.logger_config_file_not_found", Logs.getConfig().getLogCfgFile()));
				return;
			}
			System.out.println(I18N.formatMessage(YMP.__LSTRING_FILE, null, null, "ymp.commons.logger_config_file_load", cfgFile.getPath()));
			// 设置Property属性，方使在log4j.xml文件中直接引用${LOGS_DIR}属性值
			System.setProperty("LOGS_DIR", Logs.getConfig().getLogOutputDir());
			DOMConfigurator.configure(cfgFile.getPath());
			__IS_INITED = true;
		}
		level = defaultLevel;
		this.loggerName = loggerName;
		isPrintConsole = Logs.getConfig().allowPrintConsole();
		System.out.println(I18N.formatMessage(YMP.__LSTRING_FILE, null, null, isPrintConsole ? "ymp.commons.logger_init_with_console" : "ymp.commons.logger_init_without_console", loggerName));
		logger = Logger.getLogger(loggerName);
//		logger.setLevel(defaultLevel.toLevel());
		logger.info(I18N.formatMessage(YMP.__LSTRING_FILE, null, null, "ymp.commons.logger_startup", DateTimeUtils.formatTime(DateTimeUtils.currentTimeMillis(), null)));
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#destroy()
	 */
	public void destroy() {
		logger = null;
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#has(java.lang.String)
	 */
	public boolean has(String loggerName) {
		if (__IS_INITED) {
			Logger l = Logger.getLogger(loggerName);
			if (l != null) {
				Enumeration<?> e = l.getAllAppenders();
				if (e != null && e.hasMoreElements()) {
					return true;
				}
			}
		}
		return false;
	}

	private void bindExpInfo(String info, Throwable e, LogLevel logLevel) {
		if (logLevel == null) {
			logLevel = LogLevel.ALL;
		}
		if (level.getValue() > logLevel.getValue()) {
			return;
		}
		StringBuilder sb = new StringBuilder(DateTimeUtils.formatTime(DateTimeUtils.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.SSS"));
		sb.append(logLevel.getDisplayName());
		if (enableCallerInfo) {
			sb.append('[').append(Thread.currentThread().getId()).append(':').append(makeCallerInfo(callerDeepth)).append(']');
		}
		sb.append(' ').append(info);
		if (e != null) {
//			sb.append("\r\n").append(toStacksString(e));
			sb.append("- ").append(toStacksString(e));
		}
		//
		logger.log(SELF_FQCN, logLevel.toLevel(), sb.toString(), null);
		// 判断是否输出到控制台
		if (isPrintConsole) {
			System.out.println(sb.toString());
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#log(java.lang.String, net.ymate.platform.commons.logger.ILogger.LogLevel)
	 */
	public void log(String info, LogLevel level) {
		bindExpInfo(info, null, level);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#log(java.lang.String, java.lang.Throwable, net.ymate.platform.commons.logger.ILogger.LogLevel)
	 */
	public void log(String info, Throwable e, LogLevel level) {
		bindExpInfo(info, e, level);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#trace(java.lang.String)
	 */
	public void trace(String info) {
		bindExpInfo(info, null, LogLevel.TRACE);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#trace(java.lang.String, java.lang.Throwable)
	 */
	public void trace(String info, Throwable e) {
		bindExpInfo(info, e, LogLevel.TRACE);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#debug(java.lang.String)
	 */
	public void debug(String info) {
		bindExpInfo(info, null, LogLevel.DEBUG);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#debug(java.lang.String, java.lang.Throwable)
	 */
	public void debug(String info, Throwable e) {
		bindExpInfo(info, e, LogLevel.DEBUG);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#info(java.lang.String)
	 */
	public void info(String info) {
		bindExpInfo(info, null, LogLevel.INFO);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#info(java.lang.String, java.lang.Throwable)
	 */
	public void info(String info, Throwable e) {
		bindExpInfo(info, e, LogLevel.INFO);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#warn(java.lang.String)
	 */
	public void warn(String info) {
		bindExpInfo(info, null, LogLevel.WARN);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#warn(java.lang.String, java.lang.Throwable)
	 */
	public void warn(String info, Throwable e) {
		bindExpInfo(info, e, LogLevel.WARN);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#error(java.lang.String)
	 */
	public void error(String info) {
		bindExpInfo(info, null, LogLevel.INFO);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#error(java.lang.String, java.lang.Throwable)
	 */
	public void error(String info, Throwable e) {
		bindExpInfo(info, e, LogLevel.INFO);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#fatal(java.lang.String)
	 */
	public void fatal(String info) {
		bindExpInfo(info, null, LogLevel.FATAL);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.commons.logger.ILogger#fatal(java.lang.String, java.lang.Throwable)
	 */
	public void fatal(String info, Throwable e) {
		bindExpInfo(info, e, LogLevel.FATAL);
	}

}
