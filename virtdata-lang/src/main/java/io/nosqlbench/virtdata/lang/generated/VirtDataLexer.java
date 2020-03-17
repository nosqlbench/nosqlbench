// Generated from VirtData.g4 by ANTLR 4.8
package io.nosqlbench.virtdata.lang.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VirtDataLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, LONG=7, DOUBLE=8, INTEGER=9, 
		FLOAT=10, BOOLEAN=11, NEWLINE=12, COMPOSE=13, TYPEARROW=14, ASSIGN=15, 
		SSTRING_LITERAL=16, DSTRING_LITERAL=17, ID=18, IDPART=19, WS=20;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "LONG", "DOUBLE", "INTEGER", 
			"FLOAT", "BOOLEAN", "INT", "ZINT", "EXP", "NEWLINE", "COMPOSE", "TYPEARROW", 
			"ASSIGN", "SSTRING_LITERAL", "DSTRING_LITERAL", "ID", "IDPART", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'('", "','", "')'", "'$'", "';;'", null, null, null, null, 
			null, null, "'compose'", "'->'", "'='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, "LONG", "DOUBLE", "INTEGER", 
			"FLOAT", "BOOLEAN", "NEWLINE", "COMPOSE", "TYPEARROW", "ASSIGN", "SSTRING_LITERAL", 
			"DSTRING_LITERAL", "ID", "IDPART", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\26\u00eb\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\5\b@\n\b\3\b\3\b"+
		"\3\b\3\t\5\tF\n\t\3\t\3\t\3\t\7\tK\n\t\f\t\16\tN\13\t\3\t\3\t\5\tR\n\t"+
		"\3\t\5\tU\n\t\3\t\3\t\3\t\3\t\5\t[\n\t\3\t\5\t^\n\t\3\t\3\t\3\n\5\nc\n"+
		"\n\3\n\3\n\3\13\5\13h\n\13\3\13\3\13\3\13\3\13\5\13n\n\13\3\13\5\13q\n"+
		"\13\3\13\3\13\3\13\3\13\5\13w\n\13\3\13\5\13z\n\13\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\5\f\u0085\n\f\3\r\3\r\3\r\7\r\u008a\n\r\f\r\16\r\u008d"+
		"\13\r\5\r\u008f\n\r\3\16\7\16\u0092\n\16\f\16\16\16\u0095\13\16\3\17\3"+
		"\17\5\17\u0099\n\17\3\17\3\17\3\20\3\20\3\20\5\20\u00a0\n\20\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24"+
		"\3\24\3\24\5\24\u00b4\n\24\7\24\u00b6\n\24\f\24\16\24\u00b9\13\24\3\24"+
		"\3\24\3\25\3\25\3\25\3\25\3\25\5\25\u00c2\n\25\7\25\u00c4\n\25\f\25\16"+
		"\25\u00c7\13\25\3\25\3\25\3\26\3\26\3\26\7\26\u00ce\n\26\f\26\16\26\u00d1"+
		"\13\26\3\27\3\27\7\27\u00d5\n\27\f\27\16\27\u00d8\13\27\3\27\3\27\7\27"+
		"\u00dc\n\27\f\27\16\27\u00df\13\27\3\27\3\27\5\27\u00e3\n\27\3\30\6\30"+
		"\u00e6\n\30\r\30\16\30\u00e7\3\30\3\30\2\2\31\3\3\5\4\7\5\t\6\13\7\r\b"+
		"\17\t\21\n\23\13\25\f\27\r\31\2\33\2\35\2\37\16!\17#\20%\21\'\22)\23+"+
		"\24-\25/\26\3\2\20\4\2NNnn\4\2FFff\3\2\63;\3\2\62;\4\2GGgg\4\2--//\4\2"+
		"\f\f\17\17\6\2\f\f\17\17))^^\4\2))^^\6\2\f\f\17\17$$^^\4\2$$^^\4\2C\\"+
		"c|\6\2\62;C\\aac|\5\2\13\f\16\16\"\"\2\u0107\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2"+
		"#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3"+
		"\2\2\2\3\61\3\2\2\2\5\63\3\2\2\2\7\65\3\2\2\2\t\67\3\2\2\2\139\3\2\2\2"+
		"\r;\3\2\2\2\17?\3\2\2\2\21]\3\2\2\2\23b\3\2\2\2\25y\3\2\2\2\27\u0084\3"+
		"\2\2\2\31\u008e\3\2\2\2\33\u0093\3\2\2\2\35\u0096\3\2\2\2\37\u009f\3\2"+
		"\2\2!\u00a1\3\2\2\2#\u00a9\3\2\2\2%\u00ac\3\2\2\2\'\u00ae\3\2\2\2)\u00bc"+
		"\3\2\2\2+\u00ca\3\2\2\2-\u00e2\3\2\2\2/\u00e5\3\2\2\2\61\62\7=\2\2\62"+
		"\4\3\2\2\2\63\64\7*\2\2\64\6\3\2\2\2\65\66\7.\2\2\66\b\3\2\2\2\678\7+"+
		"\2\28\n\3\2\2\29:\7&\2\2:\f\3\2\2\2;<\7=\2\2<=\7=\2\2=\16\3\2\2\2>@\7"+
		"/\2\2?>\3\2\2\2?@\3\2\2\2@A\3\2\2\2AB\5\31\r\2BC\t\2\2\2C\20\3\2\2\2D"+
		"F\7/\2\2ED\3\2\2\2EF\3\2\2\2FG\3\2\2\2GH\5\31\r\2HL\7\60\2\2IK\7\62\2"+
		"\2JI\3\2\2\2KN\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MO\3\2\2\2NL\3\2\2\2OQ\5\31"+
		"\r\2PR\5\35\17\2QP\3\2\2\2QR\3\2\2\2R^\3\2\2\2SU\7/\2\2TS\3\2\2\2TU\3"+
		"\2\2\2UV\3\2\2\2VW\5\31\r\2WX\5\35\17\2X^\3\2\2\2Y[\7/\2\2ZY\3\2\2\2Z"+
		"[\3\2\2\2[\\\3\2\2\2\\^\5\31\r\2]E\3\2\2\2]T\3\2\2\2]Z\3\2\2\2^_\3\2\2"+
		"\2_`\t\3\2\2`\22\3\2\2\2ac\7/\2\2ba\3\2\2\2bc\3\2\2\2cd\3\2\2\2de\5\31"+
		"\r\2e\24\3\2\2\2fh\7/\2\2gf\3\2\2\2gh\3\2\2\2hi\3\2\2\2ij\5\31\r\2jk\7"+
		"\60\2\2km\5\33\16\2ln\5\35\17\2ml\3\2\2\2mn\3\2\2\2nz\3\2\2\2oq\7/\2\2"+
		"po\3\2\2\2pq\3\2\2\2qr\3\2\2\2rs\5\31\r\2st\5\35\17\2tz\3\2\2\2uw\7/\2"+
		"\2vu\3\2\2\2vw\3\2\2\2wx\3\2\2\2xz\5\31\r\2yg\3\2\2\2yp\3\2\2\2yv\3\2"+
		"\2\2z\26\3\2\2\2{|\7v\2\2|}\7t\2\2}~\7w\2\2~\u0085\7g\2\2\177\u0080\7"+
		"h\2\2\u0080\u0081\7c\2\2\u0081\u0082\7n\2\2\u0082\u0083\7u\2\2\u0083\u0085"+
		"\7g\2\2\u0084{\3\2\2\2\u0084\177\3\2\2\2\u0085\30\3\2\2\2\u0086\u008f"+
		"\7\62\2\2\u0087\u008b\t\4\2\2\u0088\u008a\t\5\2\2\u0089\u0088\3\2\2\2"+
		"\u008a\u008d\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u008f"+
		"\3\2\2\2\u008d\u008b\3\2\2\2\u008e\u0086\3\2\2\2\u008e\u0087\3\2\2\2\u008f"+
		"\32\3\2\2\2\u0090\u0092\t\5\2\2\u0091\u0090\3\2\2\2\u0092\u0095\3\2\2"+
		"\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094\34\3\2\2\2\u0095\u0093"+
		"\3\2\2\2\u0096\u0098\t\6\2\2\u0097\u0099\t\7\2\2\u0098\u0097\3\2\2\2\u0098"+
		"\u0099\3\2\2\2\u0099\u009a\3\2\2\2\u009a\u009b\5\31\r\2\u009b\36\3\2\2"+
		"\2\u009c\u009d\7\17\2\2\u009d\u00a0\7\f\2\2\u009e\u00a0\t\b\2\2\u009f"+
		"\u009c\3\2\2\2\u009f\u009e\3\2\2\2\u00a0 \3\2\2\2\u00a1\u00a2\7e\2\2\u00a2"+
		"\u00a3\7q\2\2\u00a3\u00a4\7o\2\2\u00a4\u00a5\7r\2\2\u00a5\u00a6\7q\2\2"+
		"\u00a6\u00a7\7u\2\2\u00a7\u00a8\7g\2\2\u00a8\"\3\2\2\2\u00a9\u00aa\7/"+
		"\2\2\u00aa\u00ab\7@\2\2\u00ab$\3\2\2\2\u00ac\u00ad\7?\2\2\u00ad&\3\2\2"+
		"\2\u00ae\u00b7\7)\2\2\u00af\u00b6\n\t\2\2\u00b0\u00b3\7^\2\2\u00b1\u00b4"+
		"\t\n\2\2\u00b2\u00b4\13\2\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b2\3\2\2\2"+
		"\u00b4\u00b6\3\2\2\2\u00b5\u00af\3\2\2\2\u00b5\u00b0\3\2\2\2\u00b6\u00b9"+
		"\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8\u00ba\3\2\2\2\u00b9"+
		"\u00b7\3\2\2\2\u00ba\u00bb\7)\2\2\u00bb(\3\2\2\2\u00bc\u00c5\7$\2\2\u00bd"+
		"\u00c4\n\13\2\2\u00be\u00c1\7^\2\2\u00bf\u00c2\t\f\2\2\u00c0\u00c2\13"+
		"\2\2\2\u00c1\u00bf\3\2\2\2\u00c1\u00c0\3\2\2\2\u00c2\u00c4\3\2\2\2\u00c3"+
		"\u00bd\3\2\2\2\u00c3\u00be\3\2\2\2\u00c4\u00c7\3\2\2\2\u00c5\u00c3\3\2"+
		"\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c8\3\2\2\2\u00c7\u00c5\3\2\2\2\u00c8"+
		"\u00c9\7$\2\2\u00c9*\3\2\2\2\u00ca\u00cf\5-\27\2\u00cb\u00cc\7\60\2\2"+
		"\u00cc\u00ce\5-\27\2\u00cd\u00cb\3\2\2\2\u00ce\u00d1\3\2\2\2\u00cf\u00cd"+
		"\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0,\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d2"+
		"\u00d6\t\r\2\2\u00d3\u00d5\t\16\2\2\u00d4\u00d3\3\2\2\2\u00d5\u00d8\3"+
		"\2\2\2\u00d6\u00d4\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00e3\3\2\2\2\u00d8"+
		"\u00d6\3\2\2\2\u00d9\u00dd\t\r\2\2\u00da\u00dc\t\16\2\2\u00db\u00da\3"+
		"\2\2\2\u00dc\u00df\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd\u00de\3\2\2\2\u00de"+
		"\u00e0\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0\u00e1\7/\2\2\u00e1\u00e3\t\16"+
		"\2\2\u00e2\u00d2\3\2\2\2\u00e2\u00d9\3\2\2\2\u00e3.\3\2\2\2\u00e4\u00e6"+
		"\t\17\2\2\u00e5\u00e4\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e5\3\2\2\2"+
		"\u00e7\u00e8\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00ea\b\30\2\2\u00ea\60"+
		"\3\2\2\2!\2?ELQTZ]bgmpvy\u0084\u008b\u008e\u0093\u0098\u009f\u00b3\u00b5"+
		"\u00b7\u00c1\u00c3\u00c5\u00cf\u00d6\u00dd\u00e2\u00e7\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}