//JNDIAT by Quentin HARDY
//quentin.hardy@protonmail.com

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyLogFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		return record.getLevel() + ":" + record.getMessage() + "\n";
	}
}
