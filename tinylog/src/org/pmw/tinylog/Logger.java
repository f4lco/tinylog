/*
 * Copyright 2012 Martin Winandy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.pmw.tinylog;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import org.pmw.tinylog.writers.LogEntry;
import org.pmw.tinylog.writers.LogEntryValue;
import org.pmw.tinylog.writers.LoggingWriter;

/**
 * Static class to create log entries.
 * 
 * The default logging level is {@link org.pmw.tinylog.LoggingLevel#INFO LoggingLevel.INFO}, which ignores trace and debug log entries.
 */
public final class Logger {

	/**
	 * Default deep in stack trace to find the needed stack trace element.
	 */
	static final int DEEP_OF_STACK_TRACE = 3;

	private static final String NEW_LINE = EnvironmentHelper.getNewLine();

	private static volatile Configuration configuration;

	private static Method stackTraceMethod;
	private static Method callerClassMethod;

	static {
		Configurator.init().activate();

		try {
			stackTraceMethod = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
			stackTraceMethod.setAccessible(true);
			StackTraceElement stackTraceElement = (StackTraceElement) stackTraceMethod.invoke(new Throwable(), 0);
			if (!Logger.class.getName().equals(stackTraceElement.getClassName())) {
				stackTraceMethod = null;
			}
		} catch (Exception ex) {
			stackTraceMethod = null;
		}

		try {
			Class<?> reflectionClass = Class.forName("sun.reflect.Reflection");
			callerClassMethod = reflectionClass.getDeclaredMethod("getCallerClass", int.class);
			callerClassMethod.setAccessible(true);
			Class<?> callerClass = (Class<?>) callerClassMethod.invoke(null, 1);
			if (!Logger.class.getName().equals(callerClass.getName())) {
				callerClassMethod = null;
			}
		} catch (Exception ex) {
			callerClassMethod = null;
		}
	}

	private Logger() {
	}

	/**
	 * Get the current global logging level.
	 * 
	 * @return Global logging level
	 */
	public static LoggingLevel getLoggingLevel() {
		return configuration.getLevel();
	}

	/**
	 * Get the current logging level for a particular package.
	 * 
	 * @param packageName
	 *            Name of the package
	 * 
	 * @return Logging level for package
	 */
	public static LoggingLevel getLoggingLevel(final String packageName) {
		return configuration.getLevelOfPackage(packageName);
	}

	/**
	 * Get the current locale, which is used in format patterns for log entries.
	 * 
	 * @return Locale for format patterns
	 */
	public static Locale getLocale() {
		return configuration.getLocale();
	}

	/**
	 * Create a trace log entry.
	 * 
	 * @param obj
	 *            The result of the <code>toString()</code> method will be logged
	 */
	public static void trace(final Object obj) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.TRACE, null, obj, null);
	}

	/**
	 * Create a trace log entry.
	 * 
	 * @param message
	 *            Text message to log
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void trace(final String message) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.TRACE, null, message, null);
	}

	/**
	 * Create a trace log entry.
	 * 
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void trace(final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.TRACE, null, message, arguments);
	}

	/**
	 * Create a trace log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void trace(final Throwable exception, final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.TRACE, exception, message, arguments);
	}

	/**
	 * Create a trace log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 */
	public static void trace(final Throwable exception) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.TRACE, exception, null, null);
	}

	/**
	 * Create a debug log entry.
	 * 
	 * @param obj
	 *            The result of the <code>toString()</code> method will be logged
	 */
	public static void debug(final Object obj) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.DEBUG, null, obj, null);
	}

	/**
	 * Create a debug log entry.
	 * 
	 * @param message
	 *            Text message to log
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void debug(final String message) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.DEBUG, null, message, null);
	}

	/**
	 * Create a debug log entry.
	 * 
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void debug(final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.DEBUG, null, message, arguments);
	}

	/**
	 * Create a debug log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void debug(final Throwable exception, final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.DEBUG, exception, message, arguments);
	}

	/**
	 * Create a debug log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 */
	public static void debug(final Throwable exception) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.DEBUG, exception, null, null);
	}

	/**
	 * Create an info log entry.
	 * 
	 * @param obj
	 *            The result of the <code>toString()</code> method will be logged
	 */
	public static void info(final Object obj) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.INFO, null, obj, null);
	}

	/**
	 * Create an info log entry.
	 * 
	 * @param message
	 *            Text message to log
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void info(final String message) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.INFO, null, message, null);
	}

	/**
	 * Create an info log entry.
	 * 
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void info(final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.INFO, null, message, arguments);
	}

	/**
	 * Create an info log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void info(final Throwable exception, final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.INFO, exception, message, arguments);
	}

	/**
	 * Create an info log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 */
	public static void info(final Throwable exception) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.INFO, exception, null, null);
	}

	/**
	 * Create a warning log entry.
	 * 
	 * @param obj
	 *            The result of the <code>toString()</code> method will be logged
	 */
	public static void warn(final Object obj) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.WARNING, null, obj, null);
	}

	/**
	 * Create a warning log entry.
	 * 
	 * @param message
	 *            Text message to log
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void warn(final String message) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.WARNING, null, message, null);
	}

	/**
	 * Create a warning log entry.
	 * 
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void warn(final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.WARNING, null, message, arguments);
	}

	/**
	 * Create a warning log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void warn(final Throwable exception, final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.WARNING, exception, message, arguments);
	}

	/**
	 * Create a warning log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 */
	public static void warn(final Throwable exception) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.WARNING, exception, null, null);
	}

	/**
	 * Create an error log entry.
	 * 
	 * @param obj
	 *            The result of the <code>toString()</code> method will be logged
	 */
	public static void error(final Object obj) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.ERROR, null, obj, null);
	}

	/**
	 * Create an error log entry.
	 * 
	 * @param message
	 *            Text message to log
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void error(final String message) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.ERROR, null, message, null);
	}

	/**
	 * Create an error log entry.
	 * 
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void error(final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.ERROR, null, message, arguments);
	}

	/**
	 * Create an error log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 * @param message
	 *            Formated text for the log entry
	 * @param arguments
	 *            Arguments for the text message
	 * 
	 * @see MessageFormat#format(String, Object...)
	 */
	public static void error(final Throwable exception, final String message, final Object... arguments) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.ERROR, exception, message, arguments);
	}

	/**
	 * Create an error log entry.
	 * 
	 * @param exception
	 *            Exception to log
	 */
	public static void error(final Throwable exception) {
		output(DEEP_OF_STACK_TRACE, LoggingLevel.ERROR, exception, null, null);
	}

	/**
	 * Get a copy of the current configuration.
	 * 
	 * @return A copy of the current configuration
	 */
	public static Configurator getConfiguration() {
		return configuration.copy();
	}

	/**
	 * Set a new configuration.
	 * 
	 * @param configuration
	 *            New configuration
	 * 
	 * @throws Exception
	 *             Failed to initialize the writer
	 */
	static void setConfirguration(final Configuration configuration) throws Exception {
		if (configuration != null) {
			LoggingWriter writer = configuration.getWriter();
			LoggingWriter oldWriter = Logger.configuration == null ? null : Logger.configuration.getWriter();
			if (writer != null && writer != oldWriter) {
				writer.init();
			}
		}

		Logger.configuration = configuration;
	}

	/**
	 * Add a log entry. This method is helpful for adding log entries form logger bridges.
	 * 
	 * @param strackTraceDeep
	 *            Deep of stack trace for finding the class, source line etc.
	 * @param level
	 *            Logging level of the log entry
	 * @param exception
	 *            Exception to log (can be <code>null</code> if there is no exception to log)
	 * @param message
	 *            Formated text or a object to log
	 * @param arguments
	 *            Arguments for the text message
	 */
	static void output(final int strackTraceDeep, final LoggingLevel level, final Throwable exception, final Object message, final Object[] arguments) {
		Configuration currentConfiguration = configuration;
		if (currentConfiguration.getWriter() != null) {
			StackTraceElement stackTraceElement = null;
			LoggingLevel activeLoggingLevel = currentConfiguration.getLevel();

			if (currentConfiguration.hasCustomLoggingLevelsForPackages()) {
				stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
				activeLoggingLevel = currentConfiguration.getLevelOfClass(stackTraceElement.getClassName());
			}

			if (activeLoggingLevel.ordinal() <= level.ordinal()) {
				try {
					LogEntry logEntry = createLogEntry(currentConfiguration, strackTraceDeep + 1, level, stackTraceElement, exception, message, arguments);
					if (currentConfiguration.getWritingThread() == null) {
						currentConfiguration.getWriter().write(logEntry);
					} else {
						currentConfiguration.getWritingThread().putLogEntry(currentConfiguration.getWriter(), logEntry);
					}
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		}
	}

	/**
	 * Add a log entry. This method is helpful for adding log entries form logger bridges.
	 * 
	 * @param stackTraceElement
	 *            Created stack trace element with class, source line etc.
	 * @param level
	 *            Logging level of the log entry
	 * @param exception
	 *            Exception to log (can be <code>null</code> if there is no exception to log)
	 * @param message
	 *            Formated text or a object to log
	 * @param arguments
	 *            Arguments for the text message
	 */
	static void output(final StackTraceElement stackTraceElement, final LoggingLevel level, final Throwable exception, final Object message,
			final Object[] arguments) {
		Configuration currentConfiguration = configuration;
		if (currentConfiguration.getWriter() != null) {
			LoggingLevel activeLoggingLevel = currentConfiguration.getLevel();

			if (currentConfiguration.hasCustomLoggingLevelsForPackages()) {
				activeLoggingLevel = currentConfiguration.getLevelOfClass(stackTraceElement.getClassName());
			}

			if (activeLoggingLevel.ordinal() <= level.ordinal()) {
				try {
					LogEntry logEntry = createLogEntry(currentConfiguration, -1, level, stackTraceElement, exception, message, arguments);
					if (currentConfiguration.getWritingThread() == null) {
						currentConfiguration.getWriter().write(logEntry);
					} else {
						currentConfiguration.getWritingThread().putLogEntry(currentConfiguration.getWriter(), logEntry);
					}
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		}
	}

	/* SUPPRESS CHECKSTYLE MethodLength */
	private static LogEntry createLogEntry(final Configuration currentConfiguration, final int strackTraceDeep, final LoggingLevel level,
			final StackTraceElement createdStackTraceElement, final Throwable exception, final Object message, final Object[] arguments) {
		Date now = null;
		String processId = null;
		Thread thread = null;
		StackTraceElement stackTraceElement = createdStackTraceElement;
		String fullyQualifiedClassName = null;
		String methodName = null;
		String fileName = null;
		int lineNumber = -1;
		String renderedMessage = null;

		for (LogEntryValue logEntryValue : currentConfiguration.getRequiredLogEntryValues()) {
			switch (logEntryValue) {
				case DATE:
					now = new Date();
					break;

				case PROCESS_ID:
					processId = EnvironmentHelper.getProcessId().toString();
					break;

				case THREAD:
					thread = Thread.currentThread();
					break;

				case CLASS:
					if (stackTraceElement == null) {
						stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
					}
					fullyQualifiedClassName = stackTraceElement.getClassName();
					break;

				case METHOD:
					if (stackTraceElement == null) {
						stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
					}
					methodName = stackTraceElement.getMethodName();
					break;

				case FILE:
					if (stackTraceElement == null) {
						stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
					}
					fileName = stackTraceElement.getFileName();
					break;

				case LINE_NUMBER:
					if (stackTraceElement == null) {
						stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
					}
					lineNumber = stackTraceElement.getLineNumber();
					break;

				case MESSAGE:
					if (message != null) {
						if (arguments == null || arguments.length == 0) {
							renderedMessage = message.toString();
						} else {
							renderedMessage = new MessageFormat((String) message, currentConfiguration.getLocale()).format(arguments);
						}
					}
					break;

				default:
					break;
			}
		}

		String renderedLogEntry;
		if (currentConfiguration.getRequiredLogEntryValues().contains(LogEntryValue.RENDERED_LOG_ENTRY)) {
			int dotIndex;
			StringBuilder builder = new StringBuilder();
			for (Token token : currentConfiguration.getFormatTokens()) {
				switch (token.getType()) {
					case THREAD:
						if (thread == null) {
							thread = Thread.currentThread();
						}
						builder.append(thread.getName());
						break;

					case THREAD_ID:
						if (thread == null) {
							thread = Thread.currentThread();
						}
						builder.append(thread.getId());
						break;

					case CLASS:
						if (fullyQualifiedClassName == null) {
							if (stackTraceElement == null) {
								stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
							}
							fullyQualifiedClassName = stackTraceElement.getClassName();
						}
						builder.append(fullyQualifiedClassName);
						break;

					case CLASS_NAME:
						if (fullyQualifiedClassName == null) {
							if (stackTraceElement == null) {
								stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
							}
							fullyQualifiedClassName = stackTraceElement.getClassName();
						}

						// determine the index of last . in the fully qualified class name
						// for my.example.ClassName this would be 10
						dotIndex = fullyQualifiedClassName.lastIndexOf('.');

						// defaults to no (default) package
						String className = fullyQualifiedClassName;
						if (dotIndex != -1) {
							className = fullyQualifiedClassName.substring(dotIndex + 1);
						}

						builder.append(className);
						break;

					case PACKAGE:
						if (fullyQualifiedClassName == null) {
							if (stackTraceElement == null) {
								stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
							}
							fullyQualifiedClassName = stackTraceElement.getClassName();
						}

						// determine the index of last . in the fully qualified class name
						// for my.example.ClassName this would be 10
						dotIndex = fullyQualifiedClassName.lastIndexOf('.');

						// defaults to no (default) package
						String packageName = "";
						if (dotIndex != -1) {
							packageName = fullyQualifiedClassName.substring(0, dotIndex);
						}

						builder.append(packageName);
						break;

					case METHOD:
						if (methodName == null) {
							if (stackTraceElement == null) {
								stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
							}
							methodName = stackTraceElement.getMethodName();
						}
						builder.append(methodName);
						break;

					case FILE:
						if (fileName == null) {
							if (stackTraceElement == null) {
								stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
							}
							fileName = stackTraceElement.getFileName();
						}
						builder.append(fileName);
						break;

					case LINE_NUMBER:
						if (lineNumber < 0) {
							if (stackTraceElement == null) {
								stackTraceElement = getStackTraceElement(currentConfiguration, strackTraceDeep);
							}
							lineNumber = stackTraceElement.getLineNumber();
						}
						builder.append(lineNumber);
						break;

					case LOGGING_LEVEL:
						builder.append(level);
						break;

					case DATE:
						if (now == null) {
							now = new Date();
						}
						DateFormat formatter = (DateFormat) token.getData();
						String format;
						synchronized (formatter) {
							format = formatter.format(now);
						}
						builder.append(format);
						break;

					case MESSAGE:
						if (message != null) {
							if (renderedMessage == null) {
								if (arguments == null || arguments.length == 0) {
									renderedMessage = message.toString();
								} else {
									renderedMessage = new MessageFormat((String) message, currentConfiguration.getLocale()).format(arguments);
								}
							}
							builder.append(renderedMessage);
						}
						if (exception != null) {
							if (message != null) {
								builder.append(": ");
							}
							if (currentConfiguration.getMaxStackTraceElements() == 0) {
								builder.append(exception.getClass().getName());
								String exceptionMessage = exception.getMessage();
								if (exceptionMessage != null) {
									builder.append(": ");
									builder.append(exceptionMessage);
								}
							} else {
								builder.append(getPrintedException(exception, currentConfiguration.getMaxStackTraceElements()));
							}
						}
						break;

					default:
						builder.append(token.getData());
						break;
				}
			}
			builder.append(NEW_LINE);
			renderedLogEntry = builder.toString();
		} else {
			renderedLogEntry = null;
		}

		return new LogEntry(now, processId, thread, fullyQualifiedClassName, methodName, fileName, lineNumber, level, renderedMessage, exception,
				renderedLogEntry);
	}

	private static StackTraceElement getStackTraceElement(final Configuration currentConfiguration, final int deep) {
		if (!currentConfiguration.isFullStackTraceElemetRequired() && callerClassMethod != null) {
			try {
				Class<?> callerClass = (Class<?>) callerClassMethod.invoke(null, deep + 1);
				return new StackTraceElement(callerClass.getName(), "<unknown>", "<unknown>", -1);
			} catch (Exception ex) {
				// Fallback
			}
		}

		if (stackTraceMethod != null) {
			try {
				return (StackTraceElement) stackTraceMethod.invoke(new Throwable(), deep);
			} catch (Exception ex) {
				// Fallback
			}
		}

		StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
		if (stackTraceElements.length > deep) {
			return stackTraceElements[deep];
		} else {
			return new StackTraceElement("<unknown>", "<unknown>", "<unknown>", -1);
		}
	}

	private static String getPrintedException(final Throwable exception, final int countStackTraceElements) {
		StringBuilder builder = new StringBuilder();
		builder.append(exception.getClass().getName());

		String message = exception.getMessage();
		if (message != null) {
			builder.append(": ");
			builder.append(message);
		}

		StackTraceElement[] stackTrace = exception.getStackTrace();
		int length = Math.max(1, Math.min(stackTrace.length, countStackTraceElements));
		for (int i = 0; i < length; ++i) {
			builder.append(NEW_LINE);
			builder.append('\t');
			builder.append("at ");
			builder.append(stackTrace[i]);
		}

		if (stackTrace.length > length) {
			builder.append(NEW_LINE);
			builder.append('\t');
			builder.append("...");
			return builder.toString();
		}

		Throwable cause = exception.getCause();
		if (cause != null) {
			builder.append(NEW_LINE);
			builder.append("Caused by: ");
			builder.append(getPrintedException(cause, countStackTraceElements - length));
		}

		return builder.toString();
	}

}
