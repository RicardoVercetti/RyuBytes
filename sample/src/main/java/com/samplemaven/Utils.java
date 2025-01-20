package com.samplemaven;

public class Utils {
    public static String formatData(byte[] data) {
		StringBuffer sb = new StringBuffer();
		int pos = 0;
		int lines = data.length / 16;

		for (int j = 0; j <= lines; j++) {
			int start_len = sb.length();
			String line_no = "00000000" + Integer.toHexString(pos);
			int len = line_no.length();
			line_no = line_no.substring(len - 8, len - 4) + ":" + line_no.substring(len - 4, len);

			sb.append(line_no);
			sb.append("  ");

			for (int k = 0; pos + k < data.length && k < 16; k++) {
				int i = Byte.toUnsignedInt(data[pos + k]);
				if (i < 16) {
					sb.append("0");
				}

				sb.append(Integer.toHexString(i).toUpperCase());
				if (k == 7) {
					sb.append("  ");
				} else {
					sb.append(" ");
				}
			}

			int line_len = sb.length() - start_len;

			int no_spaces = 61 - line_len;

			for (int k = 0; k < no_spaces; k++) {
				sb.append(" ");
			}

			for (int k = 0; pos + k < data.length && k < 16; k++) {
				if (k == 8) {
					sb.append(" ");
				}

				int i = Byte.toUnsignedInt(data[pos + k]);
				if (i < 32 || i > 126) {
					sb.append(".");
				} else {
					char c = (char) i;
					sb.append(c);
				}
			}

			if (j != lines) {
				sb.append(System.lineSeparator());
			}

			pos += 16;
		}

		return sb.toString();
	}
}
