package org.alvindimas05.lagassist.updater;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

	public boolean equals(String version1, String version2) {
		return compare(version1, version2) == 0;
	}

	@Override
	public int compare(String version1, String version2) {

		VersionTokenizer tokenizer1 = new VersionTokenizer(version1);
		VersionTokenizer tokenizer2 = new VersionTokenizer(version2);

		int number1 = 0, number2 = 0;
		String suffix1 = "", suffix2 = "";

		while (tokenizer1.MoveNext()) {
			if (!tokenizer2.MoveNext()) {
				do {
					number1 = tokenizer1.getNumber();
					suffix1 = tokenizer1.getSuffix();
					if (number1 != 0 || suffix1.length() != 0) {
						// Version one is longer than number two, and non-zero
						return 1;
					}
				} while (tokenizer1.MoveNext());

				// Version one is longer than version two, but zero
				return 0;
			}

			number1 = tokenizer1.getNumber();
			suffix1 = tokenizer1.getSuffix();
			number2 = tokenizer2.getNumber();
			suffix2 = tokenizer2.getSuffix();

			if (number1 < number2) {
				// Number one is less than number two
				return -1;
			}
			if (number1 > number2) {
				// Number one is greater than number two
				return 1;
			}

			boolean empty1 = suffix1.length() == 0;
			boolean empty2 = suffix2.length() == 0;

			if (empty1 && empty2)
				continue; // No suffixes
			if (empty1)
				return 1; // First suffix is empty (1.2 > 1.2b)
			if (empty2)
				return -1; // Second suffix is empty (1.2a < 1.2)

			// Lexical comparison of suffixes
			int result = suffix1.compareTo(suffix2);
			if (result != 0)
				return result;

		}
		if (tokenizer2.MoveNext()) {
			do {
				number2 = tokenizer2.getNumber();
				suffix2 = tokenizer2.getSuffix();
				if (number2 != 0 || suffix2.length() != 0) {
					// Version one is longer than version two, and non-zero
					return -1;
				}
			} while (tokenizer2.MoveNext());

			// Version two is longer than version one, but zero
			return 0;
		}
		return 0;
	}

	private class VersionTokenizer {
		private final String _versionString;
		private final int _length;

		private int _position;
		private int _number;
		private String _suffix;

		public int getNumber() {
			return _number;
		}

		public String getSuffix() {
			return _suffix;
		}

		public VersionTokenizer(String versionString) {
			if (versionString == null)
				throw new IllegalArgumentException("versionString is null");

			_versionString = versionString;
			_length = versionString.length();
		}

		public boolean MoveNext() {
			_number = 0;
			_suffix = "";
			// No more characters
			if (_position >= _length)
				return false;

			while (_position < _length) {
				char c = _versionString.charAt(_position);
				if (c < '0' || c > '9')
					break;
				_number = _number * 10 + (c - '0');
				_position++;
			}

			int suffixStart = _position;

			while (_position < _length) {
				char c = _versionString.charAt(_position);
				if (c == '.')
					break;
				_position++;
			}

			_suffix = _versionString.substring(suffixStart, _position);

			if (_position < _length)
				_position++;

			return true;
		}
	}
}
