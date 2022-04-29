package utils;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleUtils {

	private static Scanner scanner;

	public static void logServer(String comment) {
		String message = "(<- OUT) " + comment + "\r\n";
		System.out.print(message);
	}

	public static void logClient(String comment) {
		String message = "[IN  ->] " + comment;

		if (comment.charAt(0) == '@') { // if is error return it as is
			message = "INFO: " + comment;
			Utils.sleep(500);
		}

		System.out.print(message);
	}

	public static void sendMessage(int code, String comment) {
		String message = code + " " + comment + "\r\n";

		System.out.print(message);
	}

	public static String waitMessage() {
		scanner = new Scanner(System.in);
		String message = scanner.nextLine();

		return message;
	}

	public static String waitMessageRegex(String pattern) {
		scanner = new Scanner(System.in);
		String message = scanner.nextLine();

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(message);

		if (m.matches()) {
			return m.group(2); // 0: whole matched expression, 1: first expression in brackets, 2: second exp,
								// ...
		} else {
			System.out.println("Error: Incorrect message for client");
			return null;
		}

	}

	public static String waitMultilineMessage() {
		scanner = new Scanner(System.in);
		String message = "";

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			message = message.concat(line + "\r\n");
			if (message.contains("\r\n.\r\n"))
				break;
		}

		return message;
	}

}
