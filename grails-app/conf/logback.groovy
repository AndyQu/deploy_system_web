import grails.util.BuildSettings
import grails.util.Environment


// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} %-5level %logger{0} %class{0} %line - %msg%n"
    }
	filter(ch.qos.logback.classic.filter.ThresholdFilter) {
		level = INFO
	  }
}

appender("ROLLING", ch.qos.logback.core.rolling.RollingFileAppender) {
	encoder(PatternLayoutEncoder) {
//	  Pattern = "%d %level %thread %mdc %logger - %m%n"
		Pattern="%d{HH:mm:ss.SSS} %-5level %logger{0} %class{0} %line - %msg%n"
	}
	file="/tmp/docker-deploy/web.log"
	rollingPolicy(TimeBasedRollingPolicy) {
	  FileNamePattern = "/tmp/docker-deploy/web-%d{yyyy-MM-dd}.zip"
	}
	filter(ch.qos.logback.classic.filter.ThresholdFilter) {
		level = DEBUG
	  }
  }

root(DEBUG, ['STDOUT', 'ROLLING'])

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
