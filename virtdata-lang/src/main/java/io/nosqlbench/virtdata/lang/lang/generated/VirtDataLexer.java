/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Generated from VirtData.g4 by ANTLR 4.7.1
package io.nosqlbench.virtdata.lang.lang.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VirtDataLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, LONG=7, DOUBLE=8, INTEGER=9,
		FLOAT=10, NEWLINE=11, COMPOSE=12, TYPEARROW=13, ASSIGN=14, SSTRING_LITERAL=15,
		DSTRING_LITERAL=16, ID=17, IDPART=18, WS=19;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "LONG", "DOUBLE", "INTEGER",
		"FLOAT", "INT", "EXP", "NEWLINE", "COMPOSE", "TYPEARROW", "ASSIGN", "SSTRING_LITERAL",
		"DSTRING_LITERAL", "ID", "IDPART", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'('", "','", "')'", "'$'", "';;'", null, null, null, null,
		null, "'compose'", "'->'", "'='"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "LONG", "DOUBLE", "INTEGER",
		"FLOAT", "NEWLINE", "COMPOSE", "TYPEARROW", "ASSIGN", "SSTRING_LITERAL",
		"DSTRING_LITERAL", "ID", "IDPART", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public VirtDataLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "VirtData.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\25\u00ca\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\3\3\3\3\3\4\3\4"+
		"\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\5\b<\n\b\3\b\3\b\3\b\3\t\5\tB\n\t\3\t"+
		"\3\t\3\t\3\t\5\tH\n\t\3\t\5\tK\n\t\3\t\3\t\3\t\3\t\5\tQ\n\t\3\t\5\tT\n"+
		"\t\3\t\3\t\3\n\5\nY\n\n\3\n\3\n\3\13\5\13^\n\13\3\13\3\13\3\13\3\13\5"+
		"\13d\n\13\3\13\5\13g\n\13\3\13\3\13\3\13\3\13\5\13m\n\13\3\13\5\13p\n"+
		"\13\3\f\3\f\3\f\7\fu\n\f\f\f\16\fx\13\f\5\fz\n\f\3\r\3\r\5\r~\n\r\3\r"+
		"\3\r\3\16\3\16\3\16\5\16\u0085\n\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\22\3\22\7\22\u0098\n\22\f\22"+
		"\16\22\u009b\13\22\3\22\3\22\3\23\3\23\3\23\3\23\7\23\u00a3\n\23\f\23"+
		"\16\23\u00a6\13\23\3\23\3\23\3\24\3\24\3\24\7\24\u00ad\n\24\f\24\16\24"+
		"\u00b0\13\24\3\25\3\25\7\25\u00b4\n\25\f\25\16\25\u00b7\13\25\3\25\3\25"+
		"\7\25\u00bb\n\25\f\25\16\25\u00be\13\25\3\25\3\25\5\25\u00c2\n\25\3\26"+
		"\6\26\u00c5\n\26\r\26\16\26\u00c6\3\26\3\26\2\2\27\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\n\23\13\25\f\27\2\31\2\33\r\35\16\37\17!\20#\21%\22\'\23"+
		")\24+\25\3\2\20\4\2NNnn\4\2FFff\3\2\63;\3\2\62;\4\2GGgg\4\2--//\4\2\f"+
		"\f\17\17\6\2\f\f\17\17))^^\4\2))^^\6\2\f\f\17\17$$^^\4\2$$^^\4\2C\\c|"+
		"\6\2\62;C\\aac|\5\2\13\13\16\16\"\"\2\u00e2\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2"+
		"\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\3"+
		"-\3\2\2\2\5/\3\2\2\2\7\61\3\2\2\2\t\63\3\2\2\2\13\65\3\2\2\2\r\67\3\2"+
		"\2\2\17;\3\2\2\2\21S\3\2\2\2\23X\3\2\2\2\25o\3\2\2\2\27y\3\2\2\2\31{\3"+
		"\2\2\2\33\u0084\3\2\2\2\35\u0086\3\2\2\2\37\u008e\3\2\2\2!\u0091\3\2\2"+
		"\2#\u0093\3\2\2\2%\u009e\3\2\2\2\'\u00a9\3\2\2\2)\u00c1\3\2\2\2+\u00c4"+
		"\3\2\2\2-.\7=\2\2.\4\3\2\2\2/\60\7*\2\2\60\6\3\2\2\2\61\62\7.\2\2\62\b"+
		"\3\2\2\2\63\64\7+\2\2\64\n\3\2\2\2\65\66\7&\2\2\66\f\3\2\2\2\678\7=\2"+
		"\289\7=\2\29\16\3\2\2\2:<\7/\2\2;:\3\2\2\2;<\3\2\2\2<=\3\2\2\2=>\5\27"+
		"\f\2>?\t\2\2\2?\20\3\2\2\2@B\7/\2\2A@\3\2\2\2AB\3\2\2\2BC\3\2\2\2CD\5"+
		"\27\f\2DE\7\60\2\2EG\5\27\f\2FH\5\31\r\2GF\3\2\2\2GH\3\2\2\2HT\3\2\2\2"+
		"IK\7/\2\2JI\3\2\2\2JK\3\2\2\2KL\3\2\2\2LM\5\27\f\2MN\5\31\r\2NT\3\2\2"+
		"\2OQ\7/\2\2PO\3\2\2\2PQ\3\2\2\2QR\3\2\2\2RT\5\27\f\2SA\3\2\2\2SJ\3\2\2"+
		"\2SP\3\2\2\2TU\3\2\2\2UV\t\3\2\2V\22\3\2\2\2WY\7/\2\2XW\3\2\2\2XY\3\2"+
		"\2\2YZ\3\2\2\2Z[\5\27\f\2[\24\3\2\2\2\\^\7/\2\2]\\\3\2\2\2]^\3\2\2\2^"+
		"_\3\2\2\2_`\5\27\f\2`a\7\60\2\2ac\5\27\f\2bd\5\31\r\2cb\3\2\2\2cd\3\2"+
		"\2\2dp\3\2\2\2eg\7/\2\2fe\3\2\2\2fg\3\2\2\2gh\3\2\2\2hi\5\27\f\2ij\5\31"+
		"\r\2jp\3\2\2\2km\7/\2\2lk\3\2\2\2lm\3\2\2\2mn\3\2\2\2np\5\27\f\2o]\3\2"+
		"\2\2of\3\2\2\2ol\3\2\2\2p\26\3\2\2\2qz\7\62\2\2rv\t\4\2\2su\t\5\2\2ts"+
		"\3\2\2\2ux\3\2\2\2vt\3\2\2\2vw\3\2\2\2wz\3\2\2\2xv\3\2\2\2yq\3\2\2\2y"+
		"r\3\2\2\2z\30\3\2\2\2{}\t\6\2\2|~\t\7\2\2}|\3\2\2\2}~\3\2\2\2~\177\3\2"+
		"\2\2\177\u0080\5\27\f\2\u0080\32\3\2\2\2\u0081\u0082\7\17\2\2\u0082\u0085"+
		"\7\f\2\2\u0083\u0085\t\b\2\2\u0084\u0081\3\2\2\2\u0084\u0083\3\2\2\2\u0085"+
		"\34\3\2\2\2\u0086\u0087\7e\2\2\u0087\u0088\7q\2\2\u0088\u0089\7o\2\2\u0089"+
		"\u008a\7r\2\2\u008a\u008b\7q\2\2\u008b\u008c\7u\2\2\u008c\u008d\7g\2\2"+
		"\u008d\36\3\2\2\2\u008e\u008f\7/\2\2\u008f\u0090\7@\2\2\u0090 \3\2\2\2"+
		"\u0091\u0092\7?\2\2\u0092\"\3\2\2\2\u0093\u0099\7)\2\2\u0094\u0098\n\t"+
		"\2\2\u0095\u0096\7^\2\2\u0096\u0098\t\n\2\2\u0097\u0094\3\2\2\2\u0097"+
		"\u0095\3\2\2\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2\2\2\u0099\u009a\3\2"+
		"\2\2\u009a\u009c\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u009d\7)\2\2\u009d"+
		"$\3\2\2\2\u009e\u00a4\7$\2\2\u009f\u00a3\n\13\2\2\u00a0\u00a1\7^\2\2\u00a1"+
		"\u00a3\t\f\2\2\u00a2\u009f\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a3\u00a6\3\2"+
		"\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a7\3\2\2\2\u00a6"+
		"\u00a4\3\2\2\2\u00a7\u00a8\7$\2\2\u00a8&\3\2\2\2\u00a9\u00ae\5)\25\2\u00aa"+
		"\u00ab\7\60\2\2\u00ab\u00ad\5)\25\2\u00ac\u00aa\3\2\2\2\u00ad\u00b0\3"+
		"\2\2\2\u00ae\u00ac\3\2\2\2\u00ae\u00af\3\2\2\2\u00af(\3\2\2\2\u00b0\u00ae"+
		"\3\2\2\2\u00b1\u00b5\t\r\2\2\u00b2\u00b4\t\16\2\2\u00b3\u00b2\3\2\2\2"+
		"\u00b4\u00b7\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00c2"+
		"\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b8\u00bc\t\r\2\2\u00b9\u00bb\t\16\2\2"+
		"\u00ba\u00b9\3\2\2\2\u00bb\u00be\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bd"+
		"\3\2\2\2\u00bd\u00bf\3\2\2\2\u00be\u00bc\3\2\2\2\u00bf\u00c0\7/\2\2\u00c0"+
		"\u00c2\t\16\2\2\u00c1\u00b1\3\2\2\2\u00c1\u00b8\3\2\2\2\u00c2*\3\2\2\2"+
		"\u00c3\u00c5\t\17\2\2\u00c4\u00c3\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c4"+
		"\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\u00c9\b\26\2\2"+
		"\u00c9,\3\2\2\2\34\2;AGJPSX]cflovy}\u0084\u0097\u0099\u00a2\u00a4\u00ae"+
		"\u00b5\u00bc\u00c1\u00c6\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
