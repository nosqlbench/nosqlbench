// Generated from CQL3.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('antlr4/index');
var CQL3Listener = require('./CQL3Listener').CQL3Listener;
var CQL3Visitor = require('./CQL3Visitor').CQL3Visitor;

var grammarFileName = "CQL3.g4";

var serializedATN = ["\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd",
    "\3`\u02f7\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4",
    "\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t",
    "\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27",
    "\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4",
    "\36\t\36\4\37\t\37\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t",
    "\'\4(\t(\4)\t)\4*\t*\4+\t+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t",
    "\61\4\62\t\62\4\63\t\63\4\64\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t",
    "8\49\t9\4:\t:\4;\t;\4<\t<\4=\t=\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC",
    "\4D\tD\4E\tE\4F\tF\3\2\3\2\6\2\u008f\n\2\r\2\16\2\u0090\3\2\7\2\u0094",
    "\n\2\f\2\16\2\u0097\13\2\3\2\6\2\u009a\n\2\r\2\16\2\u009b\3\3\3\3\3",
    "\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\u00ac\n\3\3\4\3\4",
    "\6\4\u00b0\n\4\r\4\16\4\u00b1\3\4\7\4\u00b5\n\4\f\4\16\4\u00b8\13\4",
    "\3\4\6\4\u00bb\n\4\r\4\16\4\u00bc\3\5\3\5\3\5\5\5\u00c2\n\5\3\6\3\6",
    "\3\6\5\6\u00c7\n\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3",
    "\b\5\b\u00d6\n\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\5\n\u00e0\n\n\3\n\3",
    "\n\3\n\3\n\5\n\u00e6\n\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3",
    "\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u00fa\n\f\3\r\3\r\3\r\5\r\u00ff",
    "\n\r\3\r\3\r\3\16\3\16\3\16\3\17\3\17\5\17\u0108\n\17\3\17\3\17\5\17",
    "\u010c\n\17\3\17\5\17\u010f\n\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17",
    "\3\17\3\17\5\17\u011a\n\17\5\17\u011c\n\17\3\20\3\20\3\20\5\20\u0121",
    "\n\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u012c\n\21\3",
    "\21\5\21\u012f\n\21\3\22\3\22\3\22\3\22\7\22\u0135\n\22\f\22\16\22\u0138",
    "\13\22\3\22\3\22\3\23\3\23\3\23\3\23\7\23\u0140\n\23\f\23\16\23\u0143",
    "\13\23\3\23\3\23\3\24\3\24\3\24\3\24\7\24\u014b\n\24\f\24\16\24\u014e",
    "\13\24\3\25\3\25\3\25\3\25\5\25\u0154\n\25\3\26\3\26\3\27\3\27\3\30",
    "\3\30\3\30\3\30\3\31\3\31\3\31\5\31\u0161\n\31\3\31\3\31\3\31\3\31\3",
    "\31\5\31\u0168\n\31\3\32\3\32\3\32\7\32\u016d\n\32\f\32\16\32\u0170",
    "\13\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u017d",
    "\n\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3",
    "\33\5\33\u018c\n\33\3\34\3\34\3\34\3\34\7\34\u0192\n\34\f\34\16\34\u0195",
    "\13\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\5\35\u01a1",
    "\n\35\3\36\3\36\3\36\7\36\u01a6\n\36\f\36\16\36\u01a9\13\36\3\37\3\37",
    "\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\7\37\u01b5\n\37\f\37\16\37",
    "\u01b8\13\37\5\37\u01ba\n\37\3\37\3\37\3\37\3\37\3\37\3\37\5\37\u01c2",
    "\n\37\3 \3 \5 \u01c6\n \3 \3 \3 \3 \3 \5 \u01cd\n \3 \3 \3 \5 \u01d2",
    "\n \3!\3!\3!\3!\3!\7!\u01d9\n!\f!\16!\u01dc\13!\5!\u01de\n!\3\"\3\"",
    "\3\"\3\"\3\"\5\"\u01e5\n\"\3\"\3\"\3\"\3#\3#\3#\7#\u01ed\n#\f#\16#\u01f0",
    "\13#\3$\3$\3$\3$\3$\5$\u01f7\n$\3%\3%\5%\u01fb\n%\3%\3%\5%\u01ff\n%",
    "\3%\3%\3%\3%\3&\3&\3&\3&\7&\u0209\n&\f&\16&\u020c\13&\3\'\3\'\3\'\3",
    "(\3(\3(\5(\u0214\n(\3(\3(\3)\3)\3*\3*\3+\3+\3+\7+\u021f\n+\f+\16+\u0222",
    "\13+\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\5,\u0233\n,\3-\3-",
    "\3.\3.\3.\3.\7.\u023b\n.\f.\16.\u023e\13.\3.\3.\3/\3/\3/\5/\u0245\n",
    "/\3/\3/\5/\u0249\n/\3/\3/\3/\5/\u024e\n/\3\60\3\60\3\61\3\61\3\61\3",
    "\61\7\61\u0256\n\61\f\61\16\61\u0259\13\61\3\61\3\61\3\62\3\62\3\62",
    "\3\62\3\62\7\62\u0262\n\62\f\62\16\62\u0265\13\62\3\62\3\62\5\62\u0269",
    "\n\62\3\63\3\63\3\64\3\64\3\65\3\65\3\65\3\65\3\66\3\66\3\66\3\67\3",
    "\67\3\67\3\67\3\67\3\67\5\67\u027c\n\67\38\38\38\58\u0281\n8\39\39\3",
    "9\39\59\u0287\n9\3:\3:\3:\5:\u028c\n:\3;\3;\3;\3;\3;\3;\3;\3;\3;\7;",
    "\u0297\n;\f;\16;\u029a\13;\5;\u029c\n;\3;\3;\3<\3<\3<\3<\7<\u02a4\n",
    "<\f<\16<\u02a7\13<\5<\u02a9\n<\3<\3<\3=\3=\3=\3=\7=\u02b1\n=\f=\16=",
    "\u02b4\13=\5=\u02b6\n=\3=\3=\3>\3>\3>\3>\3>\7>\u02bf\n>\f>\16>\u02c2",
    "\13>\5>\u02c4\n>\3>\3>\3?\3?\3?\7?\u02cb\n?\f?\16?\u02ce\13?\3@\3@\3",
    "@\3@\3A\3A\3B\3B\3B\5B\u02d9\nB\3C\3C\3C\5C\u02de\nC\3D\3D\3E\3E\3E",
    "\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E\5E\u02f3\nE\3F\3F\3F\2\2",
    "G\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@B",
    "DFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a",
    "\2\b\4\2\60\60KK\3\2\b\t\4\2\62\62QQ\3\2./\3\2\21!\4\288MM\u031a\2\u008c",
    "\3\2\2\2\4\u00ab\3\2\2\2\6\u00ad\3\2\2\2\b\u00c1\3\2\2\2\n\u00c3\3\2",
    "\2\2\f\u00cc\3\2\2\2\16\u00d2\3\2\2\2\20\u00d9\3\2\2\2\22\u00dc\3\2",
    "\2\2\24\u00e7\3\2\2\2\26\u00f9\3\2\2\2\30\u00fb\3\2\2\2\32\u0102\3\2",
    "\2\2\34\u0105\3\2\2\2\36\u011d\3\2\2\2 \u0124\3\2\2\2\"\u0130\3\2\2",
    "\2$\u013b\3\2\2\2&\u0146\3\2\2\2(\u0153\3\2\2\2*\u0155\3\2\2\2,\u0157",
    "\3\2\2\2.\u0159\3\2\2\2\60\u015d\3\2\2\2\62\u0169\3\2\2\2\64\u018b\3",
    "\2\2\2\66\u018d\3\2\2\28\u01a0\3\2\2\2:\u01a2\3\2\2\2<\u01c1\3\2\2\2",
    ">\u01c3\3\2\2\2@\u01d3\3\2\2\2B\u01df\3\2\2\2D\u01e9\3\2\2\2F\u01f1",
    "\3\2\2\2H\u01f8\3\2\2\2J\u0204\3\2\2\2L\u020d\3\2\2\2N\u0213\3\2\2\2",
    "P\u0217\3\2\2\2R\u0219\3\2\2\2T\u021b\3\2\2\2V\u0232\3\2\2\2X\u0234",
    "\3\2\2\2Z\u0236\3\2\2\2\\\u024d\3\2\2\2^\u024f\3\2\2\2`\u0251\3\2\2",
    "\2b\u0268\3\2\2\2d\u026a\3\2\2\2f\u026c\3\2\2\2h\u026e\3\2\2\2j\u0272",
    "\3\2\2\2l\u027b\3\2\2\2n\u0280\3\2\2\2p\u0286\3\2\2\2r\u028b\3\2\2\2",
    "t\u028d\3\2\2\2v\u029f\3\2\2\2x\u02ac\3\2\2\2z\u02b9\3\2\2\2|\u02c7",
    "\3\2\2\2~\u02cf\3\2\2\2\u0080\u02d3\3\2\2\2\u0082\u02d8\3\2\2\2\u0084",
    "\u02dd\3\2\2\2\u0086\u02df\3\2\2\2\u0088\u02f2\3\2\2\2\u008a\u02f4\3",
    "\2\2\2\u008c\u0095\5\4\3\2\u008d\u008f\7\3\2\2\u008e\u008d\3\2\2\2\u008f",
    "\u0090\3\2\2\2\u0090\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u0092\3",
    "\2\2\2\u0092\u0094\5\4\3\2\u0093\u008e\3\2\2\2\u0094\u0097\3\2\2\2\u0095",
    "\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096\u0099\3\2\2\2\u0097\u0095\3",
    "\2\2\2\u0098\u009a\7\3\2\2\u0099\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b",
    "\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c\3\3\2\2\2\u009d\u00ac\5\16",
    "\b\2\u009e\u00ac\5\n\6\2\u009f\u00ac\5\f\7\2\u00a0\u00ac\5\20\t\2\u00a1",
    "\u00ac\5\22\n\2\u00a2\u00ac\5\24\13\2\u00a3\u00ac\5\30\r\2\u00a4\u00ac",
    "\5\32\16\2\u00a5\u00ac\5\34\17\2\u00a6\u00ac\5\36\20\2\u00a7\u00ac\5",
    " \21\2\u00a8\u00ac\5\60\31\2\u00a9\u00ac\5> \2\u00aa\u00ac\5H%\2\u00ab",
    "\u009d\3\2\2\2\u00ab\u009e\3\2\2\2\u00ab\u009f\3\2\2\2\u00ab\u00a0\3",
    "\2\2\2\u00ab\u00a1\3\2\2\2\u00ab\u00a2\3\2\2\2\u00ab\u00a3\3\2\2\2\u00ab",
    "\u00a4\3\2\2\2\u00ab\u00a5\3\2\2\2\u00ab\u00a6\3\2\2\2\u00ab\u00a7\3",
    "\2\2\2\u00ab\u00a8\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00aa\3\2\2\2\u00ac",
    "\5\3\2\2\2\u00ad\u00b6\5\b\5\2\u00ae\u00b0\7\3\2\2\u00af\u00ae\3\2\2",
    "\2\u00b0\u00b1\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2",
    "\u00b3\3\2\2\2\u00b3\u00b5\5\b\5\2\u00b4\u00af\3\2\2\2\u00b5\u00b8\3",
    "\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00ba\3\2\2\2\u00b8",
    "\u00b6\3\2\2\2\u00b9\u00bb\7\3\2\2\u00ba\u00b9\3\2\2\2\u00bb\u00bc\3",
    "\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\7\3\2\2\2\u00be",
    "\u00c2\5 \21\2\u00bf\u00c2\5\60\31\2\u00c0\u00c2\5> \2\u00c1\u00be\3",
    "\2\2\2\u00c1\u00bf\3\2\2\2\u00c1\u00c0\3\2\2\2\u00c2\t\3\2\2\2\u00c3",
    "\u00c4\7\63\2\2\u00c4\u00c6\7@\2\2\u00c5\u00c7\5h\65\2\u00c6\u00c5\3",
    "\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\u00c9\5f\64\2\u00c9",
    "\u00ca\7W\2\2\u00ca\u00cb\5|?\2\u00cb\13\3\2\2\2\u00cc\u00cd\7(\2\2",
    "\u00cd\u00ce\7@\2\2\u00ce\u00cf\5f\64\2\u00cf\u00d0\7W\2\2\u00d0\u00d1",
    "\5|?\2\u00d1\r\3\2\2\2\u00d2\u00d3\7\66\2\2\u00d3\u00d5\7@\2\2\u00d4",
    "\u00d6\5j\66\2\u00d5\u00d4\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d7\3",
    "\2\2\2\u00d7\u00d8\5f\64\2\u00d8\17\3\2\2\2\u00d9\u00da\7S\2\2\u00da",
    "\u00db\5f\64\2\u00db\21\3\2\2\2\u00dc\u00dd\7\63\2\2\u00dd\u00df\t\2",
    "\2\2\u00de\u00e0\5h\65\2\u00df\u00de\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0",
    "\u00e1\3\2\2\2\u00e1\u00e2\5N(\2\u00e2\u00e5\5Z.\2\u00e3\u00e4\7W\2",
    "\2\u00e4\u00e6\5T+\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\23",
    "\3\2\2\2\u00e7\u00e8\7(\2\2\u00e8\u00e9\t\2\2\2\u00e9\u00ea\5N(\2\u00ea",
    "\u00eb\5\26\f\2\u00eb\25\3\2\2\2\u00ec\u00ed\7(\2\2\u00ed\u00ee\5R*",
    "\2\u00ee\u00ef\7P\2\2\u00ef\u00f0\5^\60\2\u00f0\u00fa\3\2\2\2\u00f1",
    "\u00f2\7\'\2\2\u00f2\u00f3\5R*\2\u00f3\u00f4\5^\60\2\u00f4\u00fa\3\2",
    "\2\2\u00f5\u00f6\7\66\2\2\u00f6\u00fa\5R*\2\u00f7\u00f8\7W\2\2\u00f8",
    "\u00fa\5T+\2\u00f9\u00ec\3\2\2\2\u00f9\u00f1\3\2\2\2\u00f9\u00f5\3\2",
    "\2\2\u00f9\u00f7\3\2\2\2\u00fa\27\3\2\2\2\u00fb\u00fc\7\66\2\2\u00fc",
    "\u00fe\7K\2\2\u00fd\u00ff\5j\66\2\u00fe\u00fd\3\2\2\2\u00fe\u00ff\3",
    "\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\5N(\2\u0101\31\3\2\2\2\u0102",
    "\u0103\7N\2\2\u0103\u0104\5N(\2\u0104\33\3\2\2\2\u0105\u0107\7\63\2",
    "\2\u0106\u0108\7\64\2\2\u0107\u0106\3\2\2\2\u0107\u0108\3\2\2\2\u0108",
    "\u0109\3\2\2\2\u0109\u010b\7<\2\2\u010a\u010c\5h\65\2\u010b\u010a\3",
    "\2\2\2\u010b\u010c\3\2\2\2\u010c\u010e\3\2\2\2\u010d\u010f\5*\26\2\u010e",
    "\u010d\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u0110\3\2\2\2\u0110\u0111\7",
    "B\2\2\u0111\u0112\5N(\2\u0112\u0113\7\4\2\2\u0113\u0114\5R*\2\u0114",
    "\u011b\7\5\2\2\u0115\u0116\7T\2\2\u0116\u0119\5,\27\2\u0117\u0118\7",
    "W\2\2\u0118\u011a\5.\30\2\u0119\u0117\3\2\2\2\u0119\u011a\3\2\2\2\u011a",
    "\u011c\3\2\2\2\u011b\u0115\3\2\2\2\u011b\u011c\3\2\2\2\u011c\35\3\2",
    "\2\2\u011d\u011e\7\66\2\2\u011e\u0120\7<\2\2\u011f\u0121\5j\66\2\u0120",
    "\u011f\3\2\2\2\u0120\u0121\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0123\5",
    "*\26\2\u0123\37\3\2\2\2\u0124\u0125\7=\2\2\u0125\u0126\7>\2\2\u0126",
    "\u0127\5N(\2\u0127\u0128\5\"\22\2\u0128\u0129\7U\2\2\u0129\u012b\5$",
    "\23\2\u012a\u012c\5h\65\2\u012b\u012a\3\2\2\2\u012b\u012c\3\2\2\2\u012c",
    "\u012e\3\2\2\2\u012d\u012f\5&\24\2\u012e\u012d\3\2\2\2\u012e\u012f\3",
    "\2\2\2\u012f!\3\2\2\2\u0130\u0131\7\4\2\2\u0131\u0136\5R*\2\u0132\u0133",
    "\7\6\2\2\u0133\u0135\5R*\2\u0134\u0132\3\2\2\2\u0135\u0138\3\2\2\2\u0136",
    "\u0134\3\2\2\2\u0136\u0137\3\2\2\2\u0137\u0139\3\2\2\2\u0138\u0136\3",
    "\2\2\2\u0139\u013a\7\5\2\2\u013a#\3\2\2\2\u013b\u013c\7\4\2\2\u013c",
    "\u0141\5p9\2\u013d\u013e\7\6\2\2\u013e\u0140\5p9\2\u013f\u013d\3\2\2",
    "\2\u0140\u0143\3\2\2\2\u0141\u013f\3\2\2\2\u0141\u0142\3\2\2\2\u0142",
    "\u0144\3\2\2\2\u0143\u0141\3\2\2\2\u0144\u0145\7\5\2\2\u0145%\3\2\2",
    "\2\u0146\u0147\7T\2\2\u0147\u014c\5(\25\2\u0148\u0149\7)\2\2\u0149\u014b",
    "\5(\25\2\u014a\u0148\3\2\2\2\u014b\u014e\3\2\2\2\u014c\u014a\3\2\2\2",
    "\u014c\u014d\3\2\2\2\u014d\'\3\2\2\2\u014e\u014c\3\2\2\2\u014f\u0150",
    "\7L\2\2\u0150\u0154\7Z\2\2\u0151\u0152\7O\2\2\u0152\u0154\7Z\2\2\u0153",
    "\u014f\3\2\2\2\u0153\u0151\3\2\2\2\u0154)\3\2\2\2\u0155\u0156\7X\2\2",
    "\u0156+\3\2\2\2\u0157\u0158\7Y\2\2\u0158-\3\2\2\2\u0159\u015a\7C\2\2",
    "\u015a\u015b\7\7\2\2\u015b\u015c\5t;\2\u015c/\3\2\2\2\u015d\u015e\7",
    "R\2\2\u015e\u0160\5N(\2\u015f\u0161\5&\24\2\u0160\u015f\3\2\2\2\u0160",
    "\u0161\3\2\2\2\u0161\u0162\3\2\2\2\u0162\u0163\7H\2\2\u0163\u0164\5",
    "\62\32\2\u0164\u0165\7V\2\2\u0165\u0167\5:\36\2\u0166\u0168\5\66\34",
    "\2\u0167\u0166\3\2\2\2\u0167\u0168\3\2\2\2\u0168\61\3\2\2\2\u0169\u016e",
    "\5\64\33\2\u016a\u016b\7\6\2\2\u016b\u016d\5\64\33\2\u016c\u016a\3\2",
    "\2\2\u016d\u0170\3\2\2\2\u016e\u016c\3\2\2\2\u016e\u016f\3\2\2\2\u016f",
    "\63\3\2\2\2\u0170\u016e\3\2\2\2\u0171\u0172\5R*\2\u0172\u0173\7\7\2",
    "\2\u0173\u0174\5p9\2\u0174\u018c\3\2\2\2\u0175\u0176\5R*\2\u0176\u0177",
    "\7\7\2\2\u0177\u0178\5R*\2\u0178\u017c\t\3\2\2\u0179\u017d\7Z\2\2\u017a",
    "\u017d\5v<\2\u017b\u017d\5x=\2\u017c\u0179\3\2\2\2\u017c\u017a\3\2\2",
    "\2\u017c\u017b\3\2\2\2\u017d\u018c\3\2\2\2\u017e\u017f\5R*\2\u017f\u0180",
    "\7\7\2\2\u0180\u0181\5R*\2\u0181\u0182\7\b\2\2\u0182\u0183\5t;\2\u0183",
    "\u018c\3\2\2\2\u0184\u0185\5R*\2\u0185\u0186\7\n\2\2\u0186\u0187\5p",
    "9\2\u0187\u0188\7\13\2\2\u0188\u0189\7\7\2\2\u0189\u018a\5p9\2\u018a",
    "\u018c\3\2\2\2\u018b\u0171\3\2\2\2\u018b\u0175\3\2\2\2\u018b\u017e\3",
    "\2\2\2\u018b\u0184\3\2\2\2\u018c\65\3\2\2\2\u018d\u018e\7:\2\2\u018e",
    "\u0193\58\35\2\u018f\u0190\7)\2\2\u0190\u0192\58\35\2\u0191\u018f\3",
    "\2\2\2\u0192\u0195\3\2\2\2\u0193\u0191\3\2\2\2\u0193\u0194\3\2\2\2\u0194",
    "\67\3\2\2\2\u0195\u0193\3\2\2\2\u0196\u0197\7X\2\2\u0197\u0198\7\7\2",
    "\2\u0198\u01a1\5p9\2\u0199\u019a\7X\2\2\u019a\u019b\7\n\2\2\u019b\u019c",
    "\5p9\2\u019c\u019d\7\13\2\2\u019d\u019e\7\7\2\2\u019e\u019f\5p9\2\u019f",
    "\u01a1\3\2\2\2\u01a0\u0196\3\2\2\2\u01a0\u0199\3\2\2\2\u01a19\3\2\2",
    "\2\u01a2\u01a7\5<\37\2\u01a3\u01a4\7)\2\2\u01a4\u01a6\5<\37\2\u01a5",
    "\u01a3\3\2\2\2\u01a6\u01a9\3\2\2\2\u01a7\u01a5\3\2\2\2\u01a7\u01a8\3",
    "\2\2\2\u01a8;\3\2\2\2\u01a9\u01a7\3\2\2\2\u01aa\u01ab\5R*\2\u01ab\u01ac",
    "\7\7\2\2\u01ac\u01ad\5p9\2\u01ad\u01c2\3\2\2\2\u01ae\u01af\5R*\2\u01af",
    "\u01b0\7;\2\2\u01b0\u01b9\7\4\2\2\u01b1\u01b6\5p9\2\u01b2\u01b3\7\6",
    "\2\2\u01b3\u01b5\5p9\2\u01b4\u01b2\3\2\2\2\u01b5\u01b8\3\2\2\2\u01b6",
    "\u01b4\3\2\2\2\u01b6\u01b7\3\2\2\2\u01b7\u01ba\3\2\2\2\u01b8\u01b6\3",
    "\2\2\2\u01b9\u01b1\3\2\2\2\u01b9\u01ba\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb",
    "\u01bc\7\5\2\2\u01bc\u01c2\3\2\2\2\u01bd\u01be\5R*\2\u01be\u01bf\7;",
    "\2\2\u01bf\u01c0\7\f\2\2\u01c0\u01c2\3\2\2\2\u01c1\u01aa\3\2\2\2\u01c1",
    "\u01ae\3\2\2\2\u01c1\u01bd\3\2\2\2\u01c2=\3\2\2\2\u01c3\u01c5\7\65\2",
    "\2\u01c4\u01c6\5D#\2\u01c5\u01c4\3\2\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c7",
    "\3\2\2\2\u01c7\u01c8\79\2\2\u01c8\u01cc\5N(\2\u01c9\u01ca\7T\2\2\u01ca",
    "\u01cb\7L\2\2\u01cb\u01cd\7Z\2\2\u01cc\u01c9\3\2\2\2\u01cc\u01cd\3\2",
    "\2\2\u01cd\u01ce\3\2\2\2\u01ce\u01cf\7V\2\2\u01cf\u01d1\5:\36\2\u01d0",
    "\u01d2\5@!\2\u01d1\u01d0\3\2\2\2\u01d1\u01d2\3\2\2\2\u01d2?\3\2\2\2",
    "\u01d3\u01dd\7:\2\2\u01d4\u01de\7\67\2\2\u01d5\u01da\5B\"\2\u01d6\u01d7",
    "\7)\2\2\u01d7\u01d9\5B\"\2\u01d8\u01d6\3\2\2\2\u01d9\u01dc\3\2\2\2\u01da",
    "\u01d8\3\2\2\2\u01da\u01db\3\2\2\2\u01db\u01de\3\2\2\2\u01dc\u01da\3",
    "\2\2\2\u01dd\u01d4\3\2\2\2\u01dd\u01d5\3\2\2\2\u01deA\3\2\2\2\u01df",
    "\u01e4\7X\2\2\u01e0\u01e1\7\n\2\2\u01e1\u01e2\5p9\2\u01e2\u01e3\7\13",
    "\2\2\u01e3\u01e5\3\2\2\2\u01e4\u01e0\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5",
    "\u01e6\3\2\2\2\u01e6\u01e7\7\7\2\2\u01e7\u01e8\5p9\2\u01e8C\3\2\2\2",
    "\u01e9\u01ee\5F$\2\u01ea\u01eb\7\6\2\2\u01eb\u01ed\5F$\2\u01ec\u01ea",
    "\3\2\2\2\u01ed\u01f0\3\2\2\2\u01ee\u01ec\3\2\2\2\u01ee\u01ef\3\2\2\2",
    "\u01efE\3\2\2\2\u01f0\u01ee\3\2\2\2\u01f1\u01f6\7X\2\2\u01f2\u01f3\7",
    "\n\2\2\u01f3\u01f4\5p9\2\u01f4\u01f5\7\13\2\2\u01f5\u01f7\3\2\2\2\u01f6",
    "\u01f2\3\2\2\2\u01f6\u01f7\3\2\2\2\u01f7G\3\2\2\2\u01f8\u01fa\7,\2\2",
    "\u01f9\u01fb\t\4\2\2\u01fa\u01f9\3\2\2\2\u01fa\u01fb\3\2\2\2\u01fb\u01fc",
    "\3\2\2\2\u01fc\u01fe\7+\2\2\u01fd\u01ff\5J&\2\u01fe\u01fd\3\2\2\2\u01fe",
    "\u01ff\3\2\2\2\u01ff\u0200\3\2\2\2\u0200\u0201\5\6\4\2\u0201\u0202\7",
    "*\2\2\u0202\u0203\7+\2\2\u0203I\3\2\2\2\u0204\u0205\7T\2\2\u0205\u020a",
    "\5L\'\2\u0206\u0207\7)\2\2\u0207\u0209\5L\'\2\u0208\u0206\3\2\2\2\u0209",
    "\u020c\3\2\2\2\u020a\u0208\3\2\2\2\u020a\u020b\3\2\2\2\u020bK\3\2\2",
    "\2\u020c\u020a\3\2\2\2\u020d\u020e\7L\2\2\u020e\u020f\7Z\2\2\u020fM",
    "\3\2\2\2\u0210\u0211\5f\64\2\u0211\u0212\7\r\2\2\u0212\u0214\3\2\2\2",
    "\u0213\u0210\3\2\2\2\u0213\u0214\3\2\2\2\u0214\u0215\3\2\2\2\u0215\u0216",
    "\5P)\2\u0216O\3\2\2\2\u0217\u0218\7X\2\2\u0218Q\3\2\2\2\u0219\u021a",
    "\7X\2\2\u021aS\3\2\2\2\u021b\u0220\5V,\2\u021c\u021d\7)\2\2\u021d\u021f",
    "\5V,\2\u021e\u021c\3\2\2\2\u021f\u0222\3\2\2\2\u0220\u021e\3\2\2\2\u0220",
    "\u0221\3\2\2\2\u0221U\3\2\2\2\u0222\u0220\3\2\2\2\u0223\u0233\5~@\2",
    "\u0224\u0225\7\61\2\2\u0225\u0233\7J\2\2\u0226\u0227\7-\2\2\u0227\u0228",
    "\7D\2\2\u0228\u0229\7E\2\2\u0229\u0233\7X\2\2\u022a\u022b\7-\2\2\u022b",
    "\u022c\7D\2\2\u022c\u022d\7E\2\2\u022d\u022e\7\4\2\2\u022e\u022f\7X",
    "\2\2\u022f\u0230\5X-\2\u0230\u0231\7\5\2\2\u0231\u0233\3\2\2\2\u0232",
    "\u0223\3\2\2\2\u0232\u0224\3\2\2\2\u0232\u0226\3\2\2\2\u0232\u022a\3",
    "\2\2\2\u0233W\3\2\2\2\u0234\u0235\t\5\2\2\u0235Y\3\2\2\2\u0236\u0237",
    "\7\4\2\2\u0237\u023c\5\\/\2\u0238\u0239\7\6\2\2\u0239\u023b\5\\/\2\u023a",
    "\u0238\3\2\2\2\u023b\u023e\3\2\2\2\u023c\u023a\3\2\2\2\u023c\u023d\3",
    "\2\2\2\u023d\u023f\3\2\2\2\u023e\u023c\3\2\2\2\u023f\u0240\7\5\2\2\u0240",
    "[\3\2\2\2\u0241\u0242\5R*\2\u0242\u0244\5^\60\2\u0243\u0245\7I\2\2\u0244",
    "\u0243\3\2\2\2\u0244\u0245\3\2\2\2\u0245\u0248\3\2\2\2\u0246\u0247\7",
    "F\2\2\u0247\u0249\7?\2\2\u0248\u0246\3\2\2\2\u0248\u0249\3\2\2\2\u0249",
    "\u024e\3\2\2\2\u024a\u024b\7F\2\2\u024b\u024c\7?\2\2\u024c\u024e\5`",
    "\61\2\u024d\u0241\3\2\2\2\u024d\u024a\3\2\2\2\u024e]\3\2\2\2\u024f\u0250",
    "\5\u0084C\2\u0250_\3\2\2\2\u0251\u0252\7\4\2\2\u0252\u0257\5b\62\2\u0253",
    "\u0254\7\6\2\2\u0254\u0256\5d\63\2\u0255\u0253\3\2\2\2\u0256\u0259\3",
    "\2\2\2\u0257\u0255\3\2\2\2\u0257\u0258\3\2\2\2\u0258\u025a\3\2\2\2\u0259",
    "\u0257\3\2\2\2\u025a\u025b\7\5\2\2\u025ba\3\2\2\2\u025c\u0269\5R*\2",
    "\u025d\u025e\7\4\2\2\u025e\u0263\5R*\2\u025f\u0260\7\6\2\2\u0260\u0262",
    "\5R*\2\u0261\u025f\3\2\2\2\u0262\u0265\3\2\2\2\u0263\u0261\3\2\2\2\u0263",
    "\u0264\3\2\2\2\u0264\u0266\3\2\2\2\u0265\u0263\3\2\2\2\u0266\u0267\7",
    "\5\2\2\u0267\u0269\3\2\2\2\u0268\u025c\3\2\2\2\u0268\u025d\3\2\2\2\u0269",
    "c\3\2\2\2\u026a\u026b\5R*\2\u026be\3\2\2\2\u026c\u026d\7X\2\2\u026d",
    "g\3\2\2\2\u026e\u026f\7:\2\2\u026f\u0270\7A\2\2\u0270\u0271\7\67\2\2",
    "\u0271i\3\2\2\2\u0272\u0273\7:\2\2\u0273\u0274\7\67\2\2\u0274k\3\2\2",
    "\2\u0275\u027c\7Y\2\2\u0276\u027c\7Z\2\2\u0277\u027c\7[\2\2\u0278\u027c",
    "\5\u008aF\2\u0279\u027c\7\\\2\2\u027a\u027c\7]\2\2\u027b\u0275\3\2\2",
    "\2\u027b\u0276\3\2\2\2\u027b\u0277\3\2\2\2\u027b\u0278\3\2\2\2\u027b",
    "\u0279\3\2\2\2\u027b\u027a\3\2\2\2\u027cm\3\2\2\2\u027d\u0281\7\f\2",
    "\2\u027e\u027f\7\16\2\2\u027f\u0281\7X\2\2\u0280\u027d\3\2\2\2\u0280",
    "\u027e\3\2\2\2\u0281o\3\2\2\2\u0282\u0287\5l\67\2\u0283\u0287\5r:\2",
    "\u0284\u0287\5n8\2\u0285\u0287\5z>\2\u0286\u0282\3\2\2\2\u0286\u0283",
    "\3\2\2\2\u0286\u0284\3\2\2\2\u0286\u0285\3\2\2\2\u0287q\3\2\2\2\u0288",
    "\u028c\5t;\2\u0289\u028c\5v<\2\u028a\u028c\5x=\2\u028b\u0288\3\2\2\2",
    "\u028b\u0289\3\2\2\2\u028b\u028a\3\2\2\2\u028cs\3\2\2\2\u028d\u029b",
    "\7\17\2\2\u028e\u028f\5p9\2\u028f\u0290\7\16\2\2\u0290\u0298\5p9\2\u0291",
    "\u0292\7\6\2\2\u0292\u0293\5p9\2\u0293\u0294\7\16\2\2\u0294\u0295\5",
    "p9\2\u0295\u0297\3\2\2\2\u0296\u0291\3\2\2\2\u0297\u029a\3\2\2\2\u0298",
    "\u0296\3\2\2\2\u0298\u0299\3\2\2\2\u0299\u029c\3\2\2\2\u029a\u0298\3",
    "\2\2\2\u029b\u028e\3\2\2\2\u029b\u029c\3\2\2\2\u029c\u029d\3\2\2\2\u029d",
    "\u029e\7\20\2\2\u029eu\3\2\2\2\u029f\u02a8\7\17\2\2\u02a0\u02a5\5p9",
    "\2\u02a1\u02a2\7\6\2\2\u02a2\u02a4\5p9\2\u02a3\u02a1\3\2\2\2\u02a4\u02a7",
    "\3\2\2\2\u02a5\u02a3\3\2\2\2\u02a5\u02a6\3\2\2\2\u02a6\u02a9\3\2\2\2",
    "\u02a7\u02a5\3\2\2\2\u02a8\u02a0\3\2\2\2\u02a8\u02a9\3\2\2\2\u02a9\u02aa",
    "\3\2\2\2\u02aa\u02ab\7\20\2\2\u02abw\3\2\2\2\u02ac\u02b5\7\n\2\2\u02ad",
    "\u02b2\5p9\2\u02ae\u02af\7\6\2\2\u02af\u02b1\5p9\2\u02b0\u02ae\3\2\2",
    "\2\u02b1\u02b4\3\2\2\2\u02b2\u02b0\3\2\2\2\u02b2\u02b3\3\2\2\2\u02b3",
    "\u02b6\3\2\2\2\u02b4\u02b2\3\2\2\2\u02b5\u02ad\3\2\2\2\u02b5\u02b6\3",
    "\2\2\2\u02b6\u02b7\3\2\2\2\u02b7\u02b8\7\13\2\2\u02b8y\3\2\2\2\u02b9",
    "\u02ba\7X\2\2\u02ba\u02c3\7\4\2\2\u02bb\u02c0\5p9\2\u02bc\u02bd\7\6",
    "\2\2\u02bd\u02bf\5p9\2\u02be\u02bc\3\2\2\2\u02bf\u02c2\3\2\2\2\u02c0",
    "\u02be\3\2\2\2\u02c0\u02c1\3\2\2\2\u02c1\u02c4\3\2\2\2\u02c2\u02c0\3",
    "\2\2\2\u02c3\u02bb\3\2\2\2\u02c3\u02c4\3\2\2\2\u02c4\u02c5\3\2\2\2\u02c5",
    "\u02c6\7\5\2\2\u02c6{\3\2\2\2\u02c7\u02cc\5~@\2\u02c8\u02c9\7)\2\2\u02c9",
    "\u02cb\5~@\2\u02ca\u02c8\3\2\2\2\u02cb\u02ce\3\2\2\2\u02cc\u02ca\3\2",
    "\2\2\u02cc\u02cd\3\2\2\2\u02cd}\3\2\2\2\u02ce\u02cc\3\2\2\2\u02cf\u02d0",
    "\5\u0080A\2\u02d0\u02d1\7\7\2\2\u02d1\u02d2\5\u0082B\2\u02d2\177\3\2",
    "\2\2\u02d3\u02d4\7X\2\2\u02d4\u0081\3\2\2\2\u02d5\u02d9\7X\2\2\u02d6",
    "\u02d9\5l\67\2\u02d7\u02d9\5t;\2\u02d8\u02d5\3\2\2\2\u02d8\u02d6\3\2",
    "\2\2\u02d8\u02d7\3\2\2\2\u02d9\u0083\3\2\2\2\u02da\u02de\5\u0086D\2",
    "\u02db\u02de\5\u0088E\2\u02dc\u02de\7Y\2\2\u02dd\u02da\3\2\2\2\u02dd",
    "\u02db\3\2\2\2\u02dd\u02dc\3\2\2\2\u02de\u0085\3\2\2\2\u02df\u02e0\t",
    "\6\2\2\u02e0\u0087\3\2\2\2\u02e1\u02e2\7\"\2\2\u02e2\u02e3\7#\2\2\u02e3",
    "\u02e4\5\u0086D\2\u02e4\u02e5\7$\2\2\u02e5\u02f3\3\2\2\2\u02e6\u02e7",
    "\7%\2\2\u02e7\u02e8\7#\2\2\u02e8\u02e9\5\u0086D\2\u02e9\u02ea\7$\2\2",
    "\u02ea\u02f3\3\2\2\2\u02eb\u02ec\7&\2\2\u02ec\u02ed\7#\2\2\u02ed\u02ee",
    "\5\u0086D\2\u02ee\u02ef\7\6\2\2\u02ef\u02f0\5\u0086D\2\u02f0\u02f1\7",
    "$\2\2\u02f1\u02f3\3\2\2\2\u02f2\u02e1\3\2\2\2\u02f2\u02e6\3\2\2\2\u02f2",
    "\u02eb\3\2\2\2\u02f3\u0089\3\2\2\2\u02f4\u02f5\t\7\2\2\u02f5\u008b\3",
    "\2\2\2L\u0090\u0095\u009b\u00ab\u00b1\u00b6\u00bc\u00c1\u00c6\u00d5",
    "\u00df\u00e5\u00f9\u00fe\u0107\u010b\u010e\u0119\u011b\u0120\u012b\u012e",
    "\u0136\u0141\u014c\u0153\u0160\u0167\u016e\u017c\u018b\u0193\u01a0\u01a7",
    "\u01b6\u01b9\u01c1\u01c5\u01cc\u01d1\u01da\u01dd\u01e4\u01ee\u01f6\u01fa",
    "\u01fe\u020a\u0213\u0220\u0232\u023c\u0244\u0248\u024d\u0257\u0263\u0268",
    "\u027b\u0280\u0286\u028b\u0298\u029b\u02a5\u02a8\u02b2\u02b5\u02c0\u02c3",
    "\u02cc\u02d8\u02dd\u02f2"].join("");


var atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

var decisionsToDFA = atn.decisionToState.map( function(ds, index) { return new antlr4.dfa.DFA(ds, index); });

var sharedContextCache = new antlr4.PredictionContextCache();

var literalNames = [ 'null', "';'", "'('", "')'", "','", "'='", "'+'", "'-'", 
                     "'['", "']'", "'?'", "'.'", "':'", "'{'", "'}'", "'ascii'", 
                     "'bigint'", "'blob'", "'boolean'", "'counter'", "'decimal'", 
                     "'double'", "'float'", "'inet'", "'int'", "'text'", 
                     "'tinyint'", "'timestamp'", "'timeuuid'", "'uuid'", 
                     "'varchar'", "'varint'", "'list'", "'<'", "'>'", "'set'", 
                     "'map'" ];

var symbolicNames = [ 'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', "K_ADD", "K_ALTER", "K_AND", "K_APPLY", 
                      "K_BATCH", "K_BEGIN", "K_CLUSTERING", "K_ASC", "K_DESC", 
                      "K_COLUMNFAMILY", "K_COMPACT", "K_COUNTER", "K_CREATE", 
                      "K_CUSTOM", "K_DELETE", "K_DROP", "K_EXISTS", "K_FALSE", 
                      "K_FROM", "K_IF", "K_IN", "K_INDEX", "K_INSERT", "K_INTO", 
                      "K_KEY", "K_KEYSPACE", "K_NOT", "K_ON", "K_OPTIONS", 
                      "K_ORDER", "K_BY", "K_PRIMARY", "K_SELECT", "K_SET", 
                      "K_STATIC", "K_STORAGE", "K_TABLE", "K_TIMESTAMP", 
                      "K_TRUE", "K_TRUNCATE", "K_TTL", "K_TYPE", "K_UNLOGGED", 
                      "K_UPDATE", "K_USE", "K_USING", "K_VALUES", "K_WHERE", 
                      "K_WITH", "IDENTIFIER", "STRING", "INTEGER", "FLOAT", 
                      "UUID", "BLOB", "SINGLE_LINE_COMMENT", "MULTILINE_COMMENT", 
                      "WS" ];

var ruleNames =  [ "statements", "statement", "dml_statements", "dml_statement", 
                   "create_keyspace_stmt", "alter_keyspace_stmt", "drop_keyspace_stmt", 
                   "use_stmt", "create_table_stmt", "alter_table_stmt", 
                   "alter_table_instruction", "drop_table_stmt", "truncate_table_stmt", 
                   "create_index_stmt", "drop_index_stmt", "insert_stmt", 
                   "column_names", "column_values", "upsert_options", "upsert_option", 
                   "index_name", "index_class", "index_options", "update_stmt", 
                   "update_assignments", "update_assignment", "update_conditions", 
                   "update_condition", "where_clause", "relation", "delete_stmt", 
                   "delete_conditions", "delete_condition", "delete_selections", 
                   "delete_selection", "batch_stmt", "batch_options", "batch_option", 
                   "table_name", "table_name_noks", "column_name", "table_options", 
                   "table_option", "asc_or_desc", "column_definitions", 
                   "column_definition", "column_type", "primary_key", "partition_key", 
                   "clustering_column", "keyspace_name", "if_not_exists", 
                   "if_exists", "constant", "variable", "term", "collection", 
                   "map", "set", "list", "function", "properties", "property", 
                   "property_name", "property_value", "data_type", "native_type", 
                   "collection_type", "bool" ];

function CQL3Parser (input) {
	antlr4.Parser.call(this, input);
    this._interp = new antlr4.atn.ParserATNSimulator(this, atn, decisionsToDFA, sharedContextCache);
    this.ruleNames = ruleNames;
    this.literalNames = literalNames;
    this.symbolicNames = symbolicNames;
    return this;
}

CQL3Parser.prototype = Object.create(antlr4.Parser.prototype);
CQL3Parser.prototype.constructor = CQL3Parser;

Object.defineProperty(CQL3Parser.prototype, "atn", {
	get : function() {
		return atn;
	}
});

CQL3Parser.EOF = antlr4.Token.EOF;
CQL3Parser.T__0 = 1;
CQL3Parser.T__1 = 2;
CQL3Parser.T__2 = 3;
CQL3Parser.T__3 = 4;
CQL3Parser.T__4 = 5;
CQL3Parser.T__5 = 6;
CQL3Parser.T__6 = 7;
CQL3Parser.T__7 = 8;
CQL3Parser.T__8 = 9;
CQL3Parser.T__9 = 10;
CQL3Parser.T__10 = 11;
CQL3Parser.T__11 = 12;
CQL3Parser.T__12 = 13;
CQL3Parser.T__13 = 14;
CQL3Parser.T__14 = 15;
CQL3Parser.T__15 = 16;
CQL3Parser.T__16 = 17;
CQL3Parser.T__17 = 18;
CQL3Parser.T__18 = 19;
CQL3Parser.T__19 = 20;
CQL3Parser.T__20 = 21;
CQL3Parser.T__21 = 22;
CQL3Parser.T__22 = 23;
CQL3Parser.T__23 = 24;
CQL3Parser.T__24 = 25;
CQL3Parser.T__25 = 26;
CQL3Parser.T__26 = 27;
CQL3Parser.T__27 = 28;
CQL3Parser.T__28 = 29;
CQL3Parser.T__29 = 30;
CQL3Parser.T__30 = 31;
CQL3Parser.T__31 = 32;
CQL3Parser.T__32 = 33;
CQL3Parser.T__33 = 34;
CQL3Parser.T__34 = 35;
CQL3Parser.T__35 = 36;
CQL3Parser.K_ADD = 37;
CQL3Parser.K_ALTER = 38;
CQL3Parser.K_AND = 39;
CQL3Parser.K_APPLY = 40;
CQL3Parser.K_BATCH = 41;
CQL3Parser.K_BEGIN = 42;
CQL3Parser.K_CLUSTERING = 43;
CQL3Parser.K_ASC = 44;
CQL3Parser.K_DESC = 45;
CQL3Parser.K_COLUMNFAMILY = 46;
CQL3Parser.K_COMPACT = 47;
CQL3Parser.K_COUNTER = 48;
CQL3Parser.K_CREATE = 49;
CQL3Parser.K_CUSTOM = 50;
CQL3Parser.K_DELETE = 51;
CQL3Parser.K_DROP = 52;
CQL3Parser.K_EXISTS = 53;
CQL3Parser.K_FALSE = 54;
CQL3Parser.K_FROM = 55;
CQL3Parser.K_IF = 56;
CQL3Parser.K_IN = 57;
CQL3Parser.K_INDEX = 58;
CQL3Parser.K_INSERT = 59;
CQL3Parser.K_INTO = 60;
CQL3Parser.K_KEY = 61;
CQL3Parser.K_KEYSPACE = 62;
CQL3Parser.K_NOT = 63;
CQL3Parser.K_ON = 64;
CQL3Parser.K_OPTIONS = 65;
CQL3Parser.K_ORDER = 66;
CQL3Parser.K_BY = 67;
CQL3Parser.K_PRIMARY = 68;
CQL3Parser.K_SELECT = 69;
CQL3Parser.K_SET = 70;
CQL3Parser.K_STATIC = 71;
CQL3Parser.K_STORAGE = 72;
CQL3Parser.K_TABLE = 73;
CQL3Parser.K_TIMESTAMP = 74;
CQL3Parser.K_TRUE = 75;
CQL3Parser.K_TRUNCATE = 76;
CQL3Parser.K_TTL = 77;
CQL3Parser.K_TYPE = 78;
CQL3Parser.K_UNLOGGED = 79;
CQL3Parser.K_UPDATE = 80;
CQL3Parser.K_USE = 81;
CQL3Parser.K_USING = 82;
CQL3Parser.K_VALUES = 83;
CQL3Parser.K_WHERE = 84;
CQL3Parser.K_WITH = 85;
CQL3Parser.IDENTIFIER = 86;
CQL3Parser.STRING = 87;
CQL3Parser.INTEGER = 88;
CQL3Parser.FLOAT = 89;
CQL3Parser.UUID = 90;
CQL3Parser.BLOB = 91;
CQL3Parser.SINGLE_LINE_COMMENT = 92;
CQL3Parser.MULTILINE_COMMENT = 93;
CQL3Parser.WS = 94;

CQL3Parser.RULE_statements = 0;
CQL3Parser.RULE_statement = 1;
CQL3Parser.RULE_dml_statements = 2;
CQL3Parser.RULE_dml_statement = 3;
CQL3Parser.RULE_create_keyspace_stmt = 4;
CQL3Parser.RULE_alter_keyspace_stmt = 5;
CQL3Parser.RULE_drop_keyspace_stmt = 6;
CQL3Parser.RULE_use_stmt = 7;
CQL3Parser.RULE_create_table_stmt = 8;
CQL3Parser.RULE_alter_table_stmt = 9;
CQL3Parser.RULE_alter_table_instruction = 10;
CQL3Parser.RULE_drop_table_stmt = 11;
CQL3Parser.RULE_truncate_table_stmt = 12;
CQL3Parser.RULE_create_index_stmt = 13;
CQL3Parser.RULE_drop_index_stmt = 14;
CQL3Parser.RULE_insert_stmt = 15;
CQL3Parser.RULE_column_names = 16;
CQL3Parser.RULE_column_values = 17;
CQL3Parser.RULE_upsert_options = 18;
CQL3Parser.RULE_upsert_option = 19;
CQL3Parser.RULE_index_name = 20;
CQL3Parser.RULE_index_class = 21;
CQL3Parser.RULE_index_options = 22;
CQL3Parser.RULE_update_stmt = 23;
CQL3Parser.RULE_update_assignments = 24;
CQL3Parser.RULE_update_assignment = 25;
CQL3Parser.RULE_update_conditions = 26;
CQL3Parser.RULE_update_condition = 27;
CQL3Parser.RULE_where_clause = 28;
CQL3Parser.RULE_relation = 29;
CQL3Parser.RULE_delete_stmt = 30;
CQL3Parser.RULE_delete_conditions = 31;
CQL3Parser.RULE_delete_condition = 32;
CQL3Parser.RULE_delete_selections = 33;
CQL3Parser.RULE_delete_selection = 34;
CQL3Parser.RULE_batch_stmt = 35;
CQL3Parser.RULE_batch_options = 36;
CQL3Parser.RULE_batch_option = 37;
CQL3Parser.RULE_table_name = 38;
CQL3Parser.RULE_table_name_noks = 39;
CQL3Parser.RULE_column_name = 40;
CQL3Parser.RULE_table_options = 41;
CQL3Parser.RULE_table_option = 42;
CQL3Parser.RULE_asc_or_desc = 43;
CQL3Parser.RULE_column_definitions = 44;
CQL3Parser.RULE_column_definition = 45;
CQL3Parser.RULE_column_type = 46;
CQL3Parser.RULE_primary_key = 47;
CQL3Parser.RULE_partition_key = 48;
CQL3Parser.RULE_clustering_column = 49;
CQL3Parser.RULE_keyspace_name = 50;
CQL3Parser.RULE_if_not_exists = 51;
CQL3Parser.RULE_if_exists = 52;
CQL3Parser.RULE_constant = 53;
CQL3Parser.RULE_variable = 54;
CQL3Parser.RULE_term = 55;
CQL3Parser.RULE_collection = 56;
CQL3Parser.RULE_map = 57;
CQL3Parser.RULE_set = 58;
CQL3Parser.RULE_list = 59;
CQL3Parser.RULE_function = 60;
CQL3Parser.RULE_properties = 61;
CQL3Parser.RULE_property = 62;
CQL3Parser.RULE_property_name = 63;
CQL3Parser.RULE_property_value = 64;
CQL3Parser.RULE_data_type = 65;
CQL3Parser.RULE_native_type = 66;
CQL3Parser.RULE_collection_type = 67;
CQL3Parser.RULE_bool = 68;

function StatementsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_statements;
    return this;
}

StatementsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
StatementsContext.prototype.constructor = StatementsContext;

StatementsContext.prototype.statement = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(StatementContext);
    } else {
        return this.getTypedRuleContext(StatementContext,i);
    }
};

StatementsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterStatements(this);
	}
};

StatementsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitStatements(this);
	}
};

StatementsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitStatements(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.StatementsContext = StatementsContext;

CQL3Parser.prototype.statements = function() {

    var localctx = new StatementsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 0, CQL3Parser.RULE_statements);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 138;
        this.statement();
        this.state = 147;
        this._errHandler.sync(this);
        var _alt = this._interp.adaptivePredict(this._input,1,this._ctx)
        while(_alt!=2 && _alt!=antlr4.atn.ATN.INVALID_ALT_NUMBER) {
            if(_alt===1) {
                this.state = 140; 
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                do {
                    this.state = 139;
                    this.match(CQL3Parser.T__0);
                    this.state = 142; 
                    this._errHandler.sync(this);
                    _la = this._input.LA(1);
                } while(_la===CQL3Parser.T__0);
                this.state = 144;
                this.statement(); 
            }
            this.state = 149;
            this._errHandler.sync(this);
            _alt = this._interp.adaptivePredict(this._input,1,this._ctx);
        }

        this.state = 151; 
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        do {
            this.state = 150;
            this.match(CQL3Parser.T__0);
            this.state = 153; 
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        } while(_la===CQL3Parser.T__0);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function StatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_statement;
    return this;
}

StatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
StatementContext.prototype.constructor = StatementContext;

StatementContext.prototype.drop_keyspace_stmt = function() {
    return this.getTypedRuleContext(Drop_keyspace_stmtContext,0);
};

StatementContext.prototype.create_keyspace_stmt = function() {
    return this.getTypedRuleContext(Create_keyspace_stmtContext,0);
};

StatementContext.prototype.alter_keyspace_stmt = function() {
    return this.getTypedRuleContext(Alter_keyspace_stmtContext,0);
};

StatementContext.prototype.use_stmt = function() {
    return this.getTypedRuleContext(Use_stmtContext,0);
};

StatementContext.prototype.create_table_stmt = function() {
    return this.getTypedRuleContext(Create_table_stmtContext,0);
};

StatementContext.prototype.alter_table_stmt = function() {
    return this.getTypedRuleContext(Alter_table_stmtContext,0);
};

StatementContext.prototype.drop_table_stmt = function() {
    return this.getTypedRuleContext(Drop_table_stmtContext,0);
};

StatementContext.prototype.truncate_table_stmt = function() {
    return this.getTypedRuleContext(Truncate_table_stmtContext,0);
};

StatementContext.prototype.create_index_stmt = function() {
    return this.getTypedRuleContext(Create_index_stmtContext,0);
};

StatementContext.prototype.drop_index_stmt = function() {
    return this.getTypedRuleContext(Drop_index_stmtContext,0);
};

StatementContext.prototype.insert_stmt = function() {
    return this.getTypedRuleContext(Insert_stmtContext,0);
};

StatementContext.prototype.update_stmt = function() {
    return this.getTypedRuleContext(Update_stmtContext,0);
};

StatementContext.prototype.delete_stmt = function() {
    return this.getTypedRuleContext(Delete_stmtContext,0);
};

StatementContext.prototype.batch_stmt = function() {
    return this.getTypedRuleContext(Batch_stmtContext,0);
};

StatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterStatement(this);
	}
};

StatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitStatement(this);
	}
};

StatementContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitStatement(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.StatementContext = StatementContext;

CQL3Parser.prototype.statement = function() {

    var localctx = new StatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 2, CQL3Parser.RULE_statement);
    try {
        this.state = 169;
        var la_ = this._interp.adaptivePredict(this._input,3,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 155;
            this.drop_keyspace_stmt();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 156;
            this.create_keyspace_stmt();
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 157;
            this.alter_keyspace_stmt();
            break;

        case 4:
            this.enterOuterAlt(localctx, 4);
            this.state = 158;
            this.use_stmt();
            break;

        case 5:
            this.enterOuterAlt(localctx, 5);
            this.state = 159;
            this.create_table_stmt();
            break;

        case 6:
            this.enterOuterAlt(localctx, 6);
            this.state = 160;
            this.alter_table_stmt();
            break;

        case 7:
            this.enterOuterAlt(localctx, 7);
            this.state = 161;
            this.drop_table_stmt();
            break;

        case 8:
            this.enterOuterAlt(localctx, 8);
            this.state = 162;
            this.truncate_table_stmt();
            break;

        case 9:
            this.enterOuterAlt(localctx, 9);
            this.state = 163;
            this.create_index_stmt();
            break;

        case 10:
            this.enterOuterAlt(localctx, 10);
            this.state = 164;
            this.drop_index_stmt();
            break;

        case 11:
            this.enterOuterAlt(localctx, 11);
            this.state = 165;
            this.insert_stmt();
            break;

        case 12:
            this.enterOuterAlt(localctx, 12);
            this.state = 166;
            this.update_stmt();
            break;

        case 13:
            this.enterOuterAlt(localctx, 13);
            this.state = 167;
            this.delete_stmt();
            break;

        case 14:
            this.enterOuterAlt(localctx, 14);
            this.state = 168;
            this.batch_stmt();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Dml_statementsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_dml_statements;
    return this;
}

Dml_statementsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Dml_statementsContext.prototype.constructor = Dml_statementsContext;

Dml_statementsContext.prototype.dml_statement = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Dml_statementContext);
    } else {
        return this.getTypedRuleContext(Dml_statementContext,i);
    }
};

Dml_statementsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDml_statements(this);
	}
};

Dml_statementsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDml_statements(this);
	}
};

Dml_statementsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDml_statements(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Dml_statementsContext = Dml_statementsContext;

CQL3Parser.prototype.dml_statements = function() {

    var localctx = new Dml_statementsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 4, CQL3Parser.RULE_dml_statements);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 171;
        this.dml_statement();
        this.state = 180;
        this._errHandler.sync(this);
        var _alt = this._interp.adaptivePredict(this._input,5,this._ctx)
        while(_alt!=2 && _alt!=antlr4.atn.ATN.INVALID_ALT_NUMBER) {
            if(_alt===1) {
                this.state = 173; 
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                do {
                    this.state = 172;
                    this.match(CQL3Parser.T__0);
                    this.state = 175; 
                    this._errHandler.sync(this);
                    _la = this._input.LA(1);
                } while(_la===CQL3Parser.T__0);
                this.state = 177;
                this.dml_statement(); 
            }
            this.state = 182;
            this._errHandler.sync(this);
            _alt = this._interp.adaptivePredict(this._input,5,this._ctx);
        }

        this.state = 184; 
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        do {
            this.state = 183;
            this.match(CQL3Parser.T__0);
            this.state = 186; 
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        } while(_la===CQL3Parser.T__0);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Dml_statementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_dml_statement;
    return this;
}

Dml_statementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Dml_statementContext.prototype.constructor = Dml_statementContext;

Dml_statementContext.prototype.insert_stmt = function() {
    return this.getTypedRuleContext(Insert_stmtContext,0);
};

Dml_statementContext.prototype.update_stmt = function() {
    return this.getTypedRuleContext(Update_stmtContext,0);
};

Dml_statementContext.prototype.delete_stmt = function() {
    return this.getTypedRuleContext(Delete_stmtContext,0);
};

Dml_statementContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDml_statement(this);
	}
};

Dml_statementContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDml_statement(this);
	}
};

Dml_statementContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDml_statement(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Dml_statementContext = Dml_statementContext;

CQL3Parser.prototype.dml_statement = function() {

    var localctx = new Dml_statementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 6, CQL3Parser.RULE_dml_statement);
    try {
        this.state = 191;
        switch(this._input.LA(1)) {
        case CQL3Parser.K_INSERT:
            this.enterOuterAlt(localctx, 1);
            this.state = 188;
            this.insert_stmt();
            break;
        case CQL3Parser.K_UPDATE:
            this.enterOuterAlt(localctx, 2);
            this.state = 189;
            this.update_stmt();
            break;
        case CQL3Parser.K_DELETE:
            this.enterOuterAlt(localctx, 3);
            this.state = 190;
            this.delete_stmt();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Create_keyspace_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_create_keyspace_stmt;
    return this;
}

Create_keyspace_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Create_keyspace_stmtContext.prototype.constructor = Create_keyspace_stmtContext;

Create_keyspace_stmtContext.prototype.K_CREATE = function() {
    return this.getToken(CQL3Parser.K_CREATE, 0);
};

Create_keyspace_stmtContext.prototype.K_KEYSPACE = function() {
    return this.getToken(CQL3Parser.K_KEYSPACE, 0);
};

Create_keyspace_stmtContext.prototype.keyspace_name = function() {
    return this.getTypedRuleContext(Keyspace_nameContext,0);
};

Create_keyspace_stmtContext.prototype.K_WITH = function() {
    return this.getToken(CQL3Parser.K_WITH, 0);
};

Create_keyspace_stmtContext.prototype.properties = function() {
    return this.getTypedRuleContext(PropertiesContext,0);
};

Create_keyspace_stmtContext.prototype.if_not_exists = function() {
    return this.getTypedRuleContext(If_not_existsContext,0);
};

Create_keyspace_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterCreate_keyspace_stmt(this);
	}
};

Create_keyspace_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitCreate_keyspace_stmt(this);
	}
};

Create_keyspace_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitCreate_keyspace_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Create_keyspace_stmtContext = Create_keyspace_stmtContext;

CQL3Parser.prototype.create_keyspace_stmt = function() {

    var localctx = new Create_keyspace_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 8, CQL3Parser.RULE_create_keyspace_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 193;
        this.match(CQL3Parser.K_CREATE);
        this.state = 194;
        this.match(CQL3Parser.K_KEYSPACE);
        this.state = 196;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 195;
            this.if_not_exists();
        }

        this.state = 198;
        this.keyspace_name();
        this.state = 199;
        this.match(CQL3Parser.K_WITH);
        this.state = 200;
        this.properties();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Alter_keyspace_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_alter_keyspace_stmt;
    return this;
}

Alter_keyspace_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Alter_keyspace_stmtContext.prototype.constructor = Alter_keyspace_stmtContext;

Alter_keyspace_stmtContext.prototype.K_ALTER = function() {
    return this.getToken(CQL3Parser.K_ALTER, 0);
};

Alter_keyspace_stmtContext.prototype.K_KEYSPACE = function() {
    return this.getToken(CQL3Parser.K_KEYSPACE, 0);
};

Alter_keyspace_stmtContext.prototype.keyspace_name = function() {
    return this.getTypedRuleContext(Keyspace_nameContext,0);
};

Alter_keyspace_stmtContext.prototype.K_WITH = function() {
    return this.getToken(CQL3Parser.K_WITH, 0);
};

Alter_keyspace_stmtContext.prototype.properties = function() {
    return this.getTypedRuleContext(PropertiesContext,0);
};

Alter_keyspace_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterAlter_keyspace_stmt(this);
	}
};

Alter_keyspace_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitAlter_keyspace_stmt(this);
	}
};

Alter_keyspace_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitAlter_keyspace_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Alter_keyspace_stmtContext = Alter_keyspace_stmtContext;

CQL3Parser.prototype.alter_keyspace_stmt = function() {

    var localctx = new Alter_keyspace_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 10, CQL3Parser.RULE_alter_keyspace_stmt);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 202;
        this.match(CQL3Parser.K_ALTER);
        this.state = 203;
        this.match(CQL3Parser.K_KEYSPACE);
        this.state = 204;
        this.keyspace_name();
        this.state = 205;
        this.match(CQL3Parser.K_WITH);
        this.state = 206;
        this.properties();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Drop_keyspace_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_drop_keyspace_stmt;
    return this;
}

Drop_keyspace_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Drop_keyspace_stmtContext.prototype.constructor = Drop_keyspace_stmtContext;

Drop_keyspace_stmtContext.prototype.K_DROP = function() {
    return this.getToken(CQL3Parser.K_DROP, 0);
};

Drop_keyspace_stmtContext.prototype.K_KEYSPACE = function() {
    return this.getToken(CQL3Parser.K_KEYSPACE, 0);
};

Drop_keyspace_stmtContext.prototype.keyspace_name = function() {
    return this.getTypedRuleContext(Keyspace_nameContext,0);
};

Drop_keyspace_stmtContext.prototype.if_exists = function() {
    return this.getTypedRuleContext(If_existsContext,0);
};

Drop_keyspace_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDrop_keyspace_stmt(this);
	}
};

Drop_keyspace_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDrop_keyspace_stmt(this);
	}
};

Drop_keyspace_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDrop_keyspace_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Drop_keyspace_stmtContext = Drop_keyspace_stmtContext;

CQL3Parser.prototype.drop_keyspace_stmt = function() {

    var localctx = new Drop_keyspace_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 12, CQL3Parser.RULE_drop_keyspace_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 208;
        this.match(CQL3Parser.K_DROP);
        this.state = 209;
        this.match(CQL3Parser.K_KEYSPACE);
        this.state = 211;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 210;
            this.if_exists();
        }

        this.state = 213;
        this.keyspace_name();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Use_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_use_stmt;
    return this;
}

Use_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Use_stmtContext.prototype.constructor = Use_stmtContext;

Use_stmtContext.prototype.K_USE = function() {
    return this.getToken(CQL3Parser.K_USE, 0);
};

Use_stmtContext.prototype.keyspace_name = function() {
    return this.getTypedRuleContext(Keyspace_nameContext,0);
};

Use_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUse_stmt(this);
	}
};

Use_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUse_stmt(this);
	}
};

Use_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUse_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Use_stmtContext = Use_stmtContext;

CQL3Parser.prototype.use_stmt = function() {

    var localctx = new Use_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 14, CQL3Parser.RULE_use_stmt);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 215;
        this.match(CQL3Parser.K_USE);
        this.state = 216;
        this.keyspace_name();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Create_table_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_create_table_stmt;
    return this;
}

Create_table_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Create_table_stmtContext.prototype.constructor = Create_table_stmtContext;

Create_table_stmtContext.prototype.K_CREATE = function() {
    return this.getToken(CQL3Parser.K_CREATE, 0);
};

Create_table_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Create_table_stmtContext.prototype.column_definitions = function() {
    return this.getTypedRuleContext(Column_definitionsContext,0);
};

Create_table_stmtContext.prototype.K_TABLE = function() {
    return this.getToken(CQL3Parser.K_TABLE, 0);
};

Create_table_stmtContext.prototype.K_COLUMNFAMILY = function() {
    return this.getToken(CQL3Parser.K_COLUMNFAMILY, 0);
};

Create_table_stmtContext.prototype.if_not_exists = function() {
    return this.getTypedRuleContext(If_not_existsContext,0);
};

Create_table_stmtContext.prototype.K_WITH = function() {
    return this.getToken(CQL3Parser.K_WITH, 0);
};

Create_table_stmtContext.prototype.table_options = function() {
    return this.getTypedRuleContext(Table_optionsContext,0);
};

Create_table_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterCreate_table_stmt(this);
	}
};

Create_table_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitCreate_table_stmt(this);
	}
};

Create_table_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitCreate_table_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Create_table_stmtContext = Create_table_stmtContext;

CQL3Parser.prototype.create_table_stmt = function() {

    var localctx = new Create_table_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 16, CQL3Parser.RULE_create_table_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 218;
        this.match(CQL3Parser.K_CREATE);
        this.state = 219;
        _la = this._input.LA(1);
        if(!(_la===CQL3Parser.K_COLUMNFAMILY || _la===CQL3Parser.K_TABLE)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
        this.state = 221;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 220;
            this.if_not_exists();
        }

        this.state = 223;
        this.table_name();
        this.state = 224;
        this.column_definitions();
        this.state = 227;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_WITH) {
            this.state = 225;
            this.match(CQL3Parser.K_WITH);
            this.state = 226;
            this.table_options();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Alter_table_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_alter_table_stmt;
    return this;
}

Alter_table_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Alter_table_stmtContext.prototype.constructor = Alter_table_stmtContext;

Alter_table_stmtContext.prototype.K_ALTER = function() {
    return this.getToken(CQL3Parser.K_ALTER, 0);
};

Alter_table_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Alter_table_stmtContext.prototype.alter_table_instruction = function() {
    return this.getTypedRuleContext(Alter_table_instructionContext,0);
};

Alter_table_stmtContext.prototype.K_TABLE = function() {
    return this.getToken(CQL3Parser.K_TABLE, 0);
};

Alter_table_stmtContext.prototype.K_COLUMNFAMILY = function() {
    return this.getToken(CQL3Parser.K_COLUMNFAMILY, 0);
};

Alter_table_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterAlter_table_stmt(this);
	}
};

Alter_table_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitAlter_table_stmt(this);
	}
};

Alter_table_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitAlter_table_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Alter_table_stmtContext = Alter_table_stmtContext;

CQL3Parser.prototype.alter_table_stmt = function() {

    var localctx = new Alter_table_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 18, CQL3Parser.RULE_alter_table_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 229;
        this.match(CQL3Parser.K_ALTER);
        this.state = 230;
        _la = this._input.LA(1);
        if(!(_la===CQL3Parser.K_COLUMNFAMILY || _la===CQL3Parser.K_TABLE)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
        this.state = 231;
        this.table_name();
        this.state = 232;
        this.alter_table_instruction();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Alter_table_instructionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_alter_table_instruction;
    return this;
}

Alter_table_instructionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Alter_table_instructionContext.prototype.constructor = Alter_table_instructionContext;

Alter_table_instructionContext.prototype.K_ALTER = function() {
    return this.getToken(CQL3Parser.K_ALTER, 0);
};

Alter_table_instructionContext.prototype.column_name = function() {
    return this.getTypedRuleContext(Column_nameContext,0);
};

Alter_table_instructionContext.prototype.K_TYPE = function() {
    return this.getToken(CQL3Parser.K_TYPE, 0);
};

Alter_table_instructionContext.prototype.column_type = function() {
    return this.getTypedRuleContext(Column_typeContext,0);
};

Alter_table_instructionContext.prototype.K_ADD = function() {
    return this.getToken(CQL3Parser.K_ADD, 0);
};

Alter_table_instructionContext.prototype.K_DROP = function() {
    return this.getToken(CQL3Parser.K_DROP, 0);
};

Alter_table_instructionContext.prototype.K_WITH = function() {
    return this.getToken(CQL3Parser.K_WITH, 0);
};

Alter_table_instructionContext.prototype.table_options = function() {
    return this.getTypedRuleContext(Table_optionsContext,0);
};

Alter_table_instructionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterAlter_table_instruction(this);
	}
};

Alter_table_instructionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitAlter_table_instruction(this);
	}
};

Alter_table_instructionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitAlter_table_instruction(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Alter_table_instructionContext = Alter_table_instructionContext;

CQL3Parser.prototype.alter_table_instruction = function() {

    var localctx = new Alter_table_instructionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 20, CQL3Parser.RULE_alter_table_instruction);
    try {
        this.state = 247;
        switch(this._input.LA(1)) {
        case CQL3Parser.K_ALTER:
            this.enterOuterAlt(localctx, 1);
            this.state = 234;
            this.match(CQL3Parser.K_ALTER);
            this.state = 235;
            this.column_name();
            this.state = 236;
            this.match(CQL3Parser.K_TYPE);
            this.state = 237;
            this.column_type();
            break;
        case CQL3Parser.K_ADD:
            this.enterOuterAlt(localctx, 2);
            this.state = 239;
            this.match(CQL3Parser.K_ADD);
            this.state = 240;
            this.column_name();
            this.state = 241;
            this.column_type();
            break;
        case CQL3Parser.K_DROP:
            this.enterOuterAlt(localctx, 3);
            this.state = 243;
            this.match(CQL3Parser.K_DROP);
            this.state = 244;
            this.column_name();
            break;
        case CQL3Parser.K_WITH:
            this.enterOuterAlt(localctx, 4);
            this.state = 245;
            this.match(CQL3Parser.K_WITH);
            this.state = 246;
            this.table_options();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Drop_table_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_drop_table_stmt;
    return this;
}

Drop_table_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Drop_table_stmtContext.prototype.constructor = Drop_table_stmtContext;

Drop_table_stmtContext.prototype.K_DROP = function() {
    return this.getToken(CQL3Parser.K_DROP, 0);
};

Drop_table_stmtContext.prototype.K_TABLE = function() {
    return this.getToken(CQL3Parser.K_TABLE, 0);
};

Drop_table_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Drop_table_stmtContext.prototype.if_exists = function() {
    return this.getTypedRuleContext(If_existsContext,0);
};

Drop_table_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDrop_table_stmt(this);
	}
};

Drop_table_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDrop_table_stmt(this);
	}
};

Drop_table_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDrop_table_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Drop_table_stmtContext = Drop_table_stmtContext;

CQL3Parser.prototype.drop_table_stmt = function() {

    var localctx = new Drop_table_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 22, CQL3Parser.RULE_drop_table_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 249;
        this.match(CQL3Parser.K_DROP);
        this.state = 250;
        this.match(CQL3Parser.K_TABLE);
        this.state = 252;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 251;
            this.if_exists();
        }

        this.state = 254;
        this.table_name();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Truncate_table_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_truncate_table_stmt;
    return this;
}

Truncate_table_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Truncate_table_stmtContext.prototype.constructor = Truncate_table_stmtContext;

Truncate_table_stmtContext.prototype.K_TRUNCATE = function() {
    return this.getToken(CQL3Parser.K_TRUNCATE, 0);
};

Truncate_table_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Truncate_table_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterTruncate_table_stmt(this);
	}
};

Truncate_table_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitTruncate_table_stmt(this);
	}
};

Truncate_table_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitTruncate_table_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Truncate_table_stmtContext = Truncate_table_stmtContext;

CQL3Parser.prototype.truncate_table_stmt = function() {

    var localctx = new Truncate_table_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 24, CQL3Parser.RULE_truncate_table_stmt);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 256;
        this.match(CQL3Parser.K_TRUNCATE);
        this.state = 257;
        this.table_name();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Create_index_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_create_index_stmt;
    return this;
}

Create_index_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Create_index_stmtContext.prototype.constructor = Create_index_stmtContext;

Create_index_stmtContext.prototype.K_CREATE = function() {
    return this.getToken(CQL3Parser.K_CREATE, 0);
};

Create_index_stmtContext.prototype.K_INDEX = function() {
    return this.getToken(CQL3Parser.K_INDEX, 0);
};

Create_index_stmtContext.prototype.K_ON = function() {
    return this.getToken(CQL3Parser.K_ON, 0);
};

Create_index_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Create_index_stmtContext.prototype.column_name = function() {
    return this.getTypedRuleContext(Column_nameContext,0);
};

Create_index_stmtContext.prototype.K_CUSTOM = function() {
    return this.getToken(CQL3Parser.K_CUSTOM, 0);
};

Create_index_stmtContext.prototype.if_not_exists = function() {
    return this.getTypedRuleContext(If_not_existsContext,0);
};

Create_index_stmtContext.prototype.index_name = function() {
    return this.getTypedRuleContext(Index_nameContext,0);
};

Create_index_stmtContext.prototype.K_USING = function() {
    return this.getToken(CQL3Parser.K_USING, 0);
};

Create_index_stmtContext.prototype.index_class = function() {
    return this.getTypedRuleContext(Index_classContext,0);
};

Create_index_stmtContext.prototype.K_WITH = function() {
    return this.getToken(CQL3Parser.K_WITH, 0);
};

Create_index_stmtContext.prototype.index_options = function() {
    return this.getTypedRuleContext(Index_optionsContext,0);
};

Create_index_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterCreate_index_stmt(this);
	}
};

Create_index_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitCreate_index_stmt(this);
	}
};

Create_index_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitCreate_index_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Create_index_stmtContext = Create_index_stmtContext;

CQL3Parser.prototype.create_index_stmt = function() {

    var localctx = new Create_index_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 26, CQL3Parser.RULE_create_index_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 259;
        this.match(CQL3Parser.K_CREATE);
        this.state = 261;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_CUSTOM) {
            this.state = 260;
            this.match(CQL3Parser.K_CUSTOM);
        }

        this.state = 263;
        this.match(CQL3Parser.K_INDEX);
        this.state = 265;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 264;
            this.if_not_exists();
        }

        this.state = 268;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.IDENTIFIER) {
            this.state = 267;
            this.index_name();
        }

        this.state = 270;
        this.match(CQL3Parser.K_ON);
        this.state = 271;
        this.table_name();
        this.state = 272;
        this.match(CQL3Parser.T__1);
        this.state = 273;
        this.column_name();
        this.state = 274;
        this.match(CQL3Parser.T__2);
        this.state = 281;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_USING) {
            this.state = 275;
            this.match(CQL3Parser.K_USING);
            this.state = 276;
            this.index_class();
            this.state = 279;
            _la = this._input.LA(1);
            if(_la===CQL3Parser.K_WITH) {
                this.state = 277;
                this.match(CQL3Parser.K_WITH);
                this.state = 278;
                this.index_options();
            }

        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Drop_index_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_drop_index_stmt;
    return this;
}

Drop_index_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Drop_index_stmtContext.prototype.constructor = Drop_index_stmtContext;

Drop_index_stmtContext.prototype.K_DROP = function() {
    return this.getToken(CQL3Parser.K_DROP, 0);
};

Drop_index_stmtContext.prototype.K_INDEX = function() {
    return this.getToken(CQL3Parser.K_INDEX, 0);
};

Drop_index_stmtContext.prototype.index_name = function() {
    return this.getTypedRuleContext(Index_nameContext,0);
};

Drop_index_stmtContext.prototype.if_exists = function() {
    return this.getTypedRuleContext(If_existsContext,0);
};

Drop_index_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDrop_index_stmt(this);
	}
};

Drop_index_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDrop_index_stmt(this);
	}
};

Drop_index_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDrop_index_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Drop_index_stmtContext = Drop_index_stmtContext;

CQL3Parser.prototype.drop_index_stmt = function() {

    var localctx = new Drop_index_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 28, CQL3Parser.RULE_drop_index_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 283;
        this.match(CQL3Parser.K_DROP);
        this.state = 284;
        this.match(CQL3Parser.K_INDEX);
        this.state = 286;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 285;
            this.if_exists();
        }

        this.state = 288;
        this.index_name();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Insert_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_insert_stmt;
    return this;
}

Insert_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Insert_stmtContext.prototype.constructor = Insert_stmtContext;

Insert_stmtContext.prototype.K_INSERT = function() {
    return this.getToken(CQL3Parser.K_INSERT, 0);
};

Insert_stmtContext.prototype.K_INTO = function() {
    return this.getToken(CQL3Parser.K_INTO, 0);
};

Insert_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Insert_stmtContext.prototype.column_names = function() {
    return this.getTypedRuleContext(Column_namesContext,0);
};

Insert_stmtContext.prototype.K_VALUES = function() {
    return this.getToken(CQL3Parser.K_VALUES, 0);
};

Insert_stmtContext.prototype.column_values = function() {
    return this.getTypedRuleContext(Column_valuesContext,0);
};

Insert_stmtContext.prototype.if_not_exists = function() {
    return this.getTypedRuleContext(If_not_existsContext,0);
};

Insert_stmtContext.prototype.upsert_options = function() {
    return this.getTypedRuleContext(Upsert_optionsContext,0);
};

Insert_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterInsert_stmt(this);
	}
};

Insert_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitInsert_stmt(this);
	}
};

Insert_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitInsert_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Insert_stmtContext = Insert_stmtContext;

CQL3Parser.prototype.insert_stmt = function() {

    var localctx = new Insert_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 30, CQL3Parser.RULE_insert_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 290;
        this.match(CQL3Parser.K_INSERT);
        this.state = 291;
        this.match(CQL3Parser.K_INTO);
        this.state = 292;
        this.table_name();
        this.state = 293;
        this.column_names();
        this.state = 294;
        this.match(CQL3Parser.K_VALUES);
        this.state = 295;
        this.column_values();
        this.state = 297;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 296;
            this.if_not_exists();
        }

        this.state = 300;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_USING) {
            this.state = 299;
            this.upsert_options();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Column_namesContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_column_names;
    return this;
}

Column_namesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Column_namesContext.prototype.constructor = Column_namesContext;

Column_namesContext.prototype.column_name = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Column_nameContext);
    } else {
        return this.getTypedRuleContext(Column_nameContext,i);
    }
};

Column_namesContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterColumn_names(this);
	}
};

Column_namesContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitColumn_names(this);
	}
};

Column_namesContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitColumn_names(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Column_namesContext = Column_namesContext;

CQL3Parser.prototype.column_names = function() {

    var localctx = new Column_namesContext(this, this._ctx, this.state);
    this.enterRule(localctx, 32, CQL3Parser.RULE_column_names);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 302;
        this.match(CQL3Parser.T__1);
        this.state = 303;
        this.column_name();
        this.state = 308;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.T__3) {
            this.state = 304;
            this.match(CQL3Parser.T__3);
            this.state = 305;
            this.column_name();
            this.state = 310;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 311;
        this.match(CQL3Parser.T__2);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Column_valuesContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_column_values;
    return this;
}

Column_valuesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Column_valuesContext.prototype.constructor = Column_valuesContext;

Column_valuesContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

Column_valuesContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterColumn_values(this);
	}
};

Column_valuesContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitColumn_values(this);
	}
};

Column_valuesContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitColumn_values(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Column_valuesContext = Column_valuesContext;

CQL3Parser.prototype.column_values = function() {

    var localctx = new Column_valuesContext(this, this._ctx, this.state);
    this.enterRule(localctx, 34, CQL3Parser.RULE_column_values);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 313;
        this.match(CQL3Parser.T__1);
        this.state = 314;
        this.term();
        this.state = 319;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.T__3) {
            this.state = 315;
            this.match(CQL3Parser.T__3);
            this.state = 316;
            this.term();
            this.state = 321;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 322;
        this.match(CQL3Parser.T__2);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Upsert_optionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_upsert_options;
    return this;
}

Upsert_optionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Upsert_optionsContext.prototype.constructor = Upsert_optionsContext;

Upsert_optionsContext.prototype.K_USING = function() {
    return this.getToken(CQL3Parser.K_USING, 0);
};

Upsert_optionsContext.prototype.upsert_option = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Upsert_optionContext);
    } else {
        return this.getTypedRuleContext(Upsert_optionContext,i);
    }
};

Upsert_optionsContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


Upsert_optionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpsert_options(this);
	}
};

Upsert_optionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpsert_options(this);
	}
};

Upsert_optionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpsert_options(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Upsert_optionsContext = Upsert_optionsContext;

CQL3Parser.prototype.upsert_options = function() {

    var localctx = new Upsert_optionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 36, CQL3Parser.RULE_upsert_options);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 324;
        this.match(CQL3Parser.K_USING);
        this.state = 325;
        this.upsert_option();
        this.state = 330;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.K_AND) {
            this.state = 326;
            this.match(CQL3Parser.K_AND);
            this.state = 327;
            this.upsert_option();
            this.state = 332;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Upsert_optionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_upsert_option;
    return this;
}

Upsert_optionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Upsert_optionContext.prototype.constructor = Upsert_optionContext;

Upsert_optionContext.prototype.K_TIMESTAMP = function() {
    return this.getToken(CQL3Parser.K_TIMESTAMP, 0);
};

Upsert_optionContext.prototype.INTEGER = function() {
    return this.getToken(CQL3Parser.INTEGER, 0);
};

Upsert_optionContext.prototype.K_TTL = function() {
    return this.getToken(CQL3Parser.K_TTL, 0);
};

Upsert_optionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpsert_option(this);
	}
};

Upsert_optionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpsert_option(this);
	}
};

Upsert_optionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpsert_option(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Upsert_optionContext = Upsert_optionContext;

CQL3Parser.prototype.upsert_option = function() {

    var localctx = new Upsert_optionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 38, CQL3Parser.RULE_upsert_option);
    try {
        this.state = 337;
        switch(this._input.LA(1)) {
        case CQL3Parser.K_TIMESTAMP:
            this.enterOuterAlt(localctx, 1);
            this.state = 333;
            this.match(CQL3Parser.K_TIMESTAMP);
            this.state = 334;
            this.match(CQL3Parser.INTEGER);
            break;
        case CQL3Parser.K_TTL:
            this.enterOuterAlt(localctx, 2);
            this.state = 335;
            this.match(CQL3Parser.K_TTL);
            this.state = 336;
            this.match(CQL3Parser.INTEGER);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Index_nameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_index_name;
    return this;
}

Index_nameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Index_nameContext.prototype.constructor = Index_nameContext;

Index_nameContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Index_nameContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterIndex_name(this);
	}
};

Index_nameContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitIndex_name(this);
	}
};

Index_nameContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitIndex_name(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Index_nameContext = Index_nameContext;

CQL3Parser.prototype.index_name = function() {

    var localctx = new Index_nameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 40, CQL3Parser.RULE_index_name);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 339;
        this.match(CQL3Parser.IDENTIFIER);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Index_classContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_index_class;
    return this;
}

Index_classContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Index_classContext.prototype.constructor = Index_classContext;

Index_classContext.prototype.STRING = function() {
    return this.getToken(CQL3Parser.STRING, 0);
};

Index_classContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterIndex_class(this);
	}
};

Index_classContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitIndex_class(this);
	}
};

Index_classContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitIndex_class(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Index_classContext = Index_classContext;

CQL3Parser.prototype.index_class = function() {

    var localctx = new Index_classContext(this, this._ctx, this.state);
    this.enterRule(localctx, 42, CQL3Parser.RULE_index_class);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 341;
        this.match(CQL3Parser.STRING);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Index_optionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_index_options;
    return this;
}

Index_optionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Index_optionsContext.prototype.constructor = Index_optionsContext;

Index_optionsContext.prototype.K_OPTIONS = function() {
    return this.getToken(CQL3Parser.K_OPTIONS, 0);
};

Index_optionsContext.prototype.map = function() {
    return this.getTypedRuleContext(MapContext,0);
};

Index_optionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterIndex_options(this);
	}
};

Index_optionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitIndex_options(this);
	}
};

Index_optionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitIndex_options(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Index_optionsContext = Index_optionsContext;

CQL3Parser.prototype.index_options = function() {

    var localctx = new Index_optionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 44, CQL3Parser.RULE_index_options);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 343;
        this.match(CQL3Parser.K_OPTIONS);
        this.state = 344;
        this.match(CQL3Parser.T__4);
        this.state = 345;
        this.map();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Update_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_update_stmt;
    return this;
}

Update_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Update_stmtContext.prototype.constructor = Update_stmtContext;

Update_stmtContext.prototype.K_UPDATE = function() {
    return this.getToken(CQL3Parser.K_UPDATE, 0);
};

Update_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Update_stmtContext.prototype.K_SET = function() {
    return this.getToken(CQL3Parser.K_SET, 0);
};

Update_stmtContext.prototype.update_assignments = function() {
    return this.getTypedRuleContext(Update_assignmentsContext,0);
};

Update_stmtContext.prototype.K_WHERE = function() {
    return this.getToken(CQL3Parser.K_WHERE, 0);
};

Update_stmtContext.prototype.where_clause = function() {
    return this.getTypedRuleContext(Where_clauseContext,0);
};

Update_stmtContext.prototype.upsert_options = function() {
    return this.getTypedRuleContext(Upsert_optionsContext,0);
};

Update_stmtContext.prototype.update_conditions = function() {
    return this.getTypedRuleContext(Update_conditionsContext,0);
};

Update_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpdate_stmt(this);
	}
};

Update_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpdate_stmt(this);
	}
};

Update_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpdate_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Update_stmtContext = Update_stmtContext;

CQL3Parser.prototype.update_stmt = function() {

    var localctx = new Update_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 46, CQL3Parser.RULE_update_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 347;
        this.match(CQL3Parser.K_UPDATE);
        this.state = 348;
        this.table_name();
        this.state = 350;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_USING) {
            this.state = 349;
            this.upsert_options();
        }

        this.state = 352;
        this.match(CQL3Parser.K_SET);
        this.state = 353;
        this.update_assignments();
        this.state = 354;
        this.match(CQL3Parser.K_WHERE);
        this.state = 355;
        this.where_clause();
        this.state = 357;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 356;
            this.update_conditions();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Update_assignmentsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_update_assignments;
    return this;
}

Update_assignmentsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Update_assignmentsContext.prototype.constructor = Update_assignmentsContext;

Update_assignmentsContext.prototype.update_assignment = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Update_assignmentContext);
    } else {
        return this.getTypedRuleContext(Update_assignmentContext,i);
    }
};

Update_assignmentsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpdate_assignments(this);
	}
};

Update_assignmentsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpdate_assignments(this);
	}
};

Update_assignmentsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpdate_assignments(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Update_assignmentsContext = Update_assignmentsContext;

CQL3Parser.prototype.update_assignments = function() {

    var localctx = new Update_assignmentsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 48, CQL3Parser.RULE_update_assignments);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 359;
        this.update_assignment();
        this.state = 364;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.T__3) {
            this.state = 360;
            this.match(CQL3Parser.T__3);
            this.state = 361;
            this.update_assignment();
            this.state = 366;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Update_assignmentContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_update_assignment;
    return this;
}

Update_assignmentContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Update_assignmentContext.prototype.constructor = Update_assignmentContext;

Update_assignmentContext.prototype.column_name = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Column_nameContext);
    } else {
        return this.getTypedRuleContext(Column_nameContext,i);
    }
};

Update_assignmentContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

Update_assignmentContext.prototype.INTEGER = function() {
    return this.getToken(CQL3Parser.INTEGER, 0);
};

Update_assignmentContext.prototype.set = function() {
    return this.getTypedRuleContext(SetContext,0);
};

Update_assignmentContext.prototype.list = function() {
    return this.getTypedRuleContext(ListContext,0);
};

Update_assignmentContext.prototype.map = function() {
    return this.getTypedRuleContext(MapContext,0);
};

Update_assignmentContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpdate_assignment(this);
	}
};

Update_assignmentContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpdate_assignment(this);
	}
};

Update_assignmentContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpdate_assignment(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Update_assignmentContext = Update_assignmentContext;

CQL3Parser.prototype.update_assignment = function() {

    var localctx = new Update_assignmentContext(this, this._ctx, this.state);
    this.enterRule(localctx, 50, CQL3Parser.RULE_update_assignment);
    var _la = 0; // Token type
    try {
        this.state = 393;
        var la_ = this._interp.adaptivePredict(this._input,30,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 367;
            this.column_name();
            this.state = 368;
            this.match(CQL3Parser.T__4);
            this.state = 369;
            this.term();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 371;
            this.column_name();
            this.state = 372;
            this.match(CQL3Parser.T__4);
            this.state = 373;
            this.column_name();
            this.state = 374;
            _la = this._input.LA(1);
            if(!(_la===CQL3Parser.T__5 || _la===CQL3Parser.T__6)) {
            this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
            this.state = 378;
            switch(this._input.LA(1)) {
            case CQL3Parser.INTEGER:
                this.state = 375;
                this.match(CQL3Parser.INTEGER);
                break;
            case CQL3Parser.T__12:
                this.state = 376;
                this.set();
                break;
            case CQL3Parser.T__7:
                this.state = 377;
                this.list();
                break;
            default:
                throw new antlr4.error.NoViableAltException(this);
            }
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 380;
            this.column_name();
            this.state = 381;
            this.match(CQL3Parser.T__4);
            this.state = 382;
            this.column_name();
            this.state = 383;
            this.match(CQL3Parser.T__5);
            this.state = 384;
            this.map();
            break;

        case 4:
            this.enterOuterAlt(localctx, 4);
            this.state = 386;
            this.column_name();
            this.state = 387;
            this.match(CQL3Parser.T__7);
            this.state = 388;
            this.term();
            this.state = 389;
            this.match(CQL3Parser.T__8);
            this.state = 390;
            this.match(CQL3Parser.T__4);
            this.state = 391;
            this.term();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Update_conditionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_update_conditions;
    return this;
}

Update_conditionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Update_conditionsContext.prototype.constructor = Update_conditionsContext;

Update_conditionsContext.prototype.K_IF = function() {
    return this.getToken(CQL3Parser.K_IF, 0);
};

Update_conditionsContext.prototype.update_condition = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Update_conditionContext);
    } else {
        return this.getTypedRuleContext(Update_conditionContext,i);
    }
};

Update_conditionsContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


Update_conditionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpdate_conditions(this);
	}
};

Update_conditionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpdate_conditions(this);
	}
};

Update_conditionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpdate_conditions(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Update_conditionsContext = Update_conditionsContext;

CQL3Parser.prototype.update_conditions = function() {

    var localctx = new Update_conditionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 52, CQL3Parser.RULE_update_conditions);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 395;
        this.match(CQL3Parser.K_IF);
        this.state = 396;
        this.update_condition();
        this.state = 401;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.K_AND) {
            this.state = 397;
            this.match(CQL3Parser.K_AND);
            this.state = 398;
            this.update_condition();
            this.state = 403;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Update_conditionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_update_condition;
    return this;
}

Update_conditionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Update_conditionContext.prototype.constructor = Update_conditionContext;

Update_conditionContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Update_conditionContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

Update_conditionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterUpdate_condition(this);
	}
};

Update_conditionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitUpdate_condition(this);
	}
};

Update_conditionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitUpdate_condition(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Update_conditionContext = Update_conditionContext;

CQL3Parser.prototype.update_condition = function() {

    var localctx = new Update_conditionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 54, CQL3Parser.RULE_update_condition);
    try {
        this.state = 414;
        var la_ = this._interp.adaptivePredict(this._input,32,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 404;
            this.match(CQL3Parser.IDENTIFIER);
            this.state = 405;
            this.match(CQL3Parser.T__4);
            this.state = 406;
            this.term();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 407;
            this.match(CQL3Parser.IDENTIFIER);
            this.state = 408;
            this.match(CQL3Parser.T__7);
            this.state = 409;
            this.term();
            this.state = 410;
            this.match(CQL3Parser.T__8);
            this.state = 411;
            this.match(CQL3Parser.T__4);
            this.state = 412;
            this.term();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Where_clauseContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_where_clause;
    return this;
}

Where_clauseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Where_clauseContext.prototype.constructor = Where_clauseContext;

Where_clauseContext.prototype.relation = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(RelationContext);
    } else {
        return this.getTypedRuleContext(RelationContext,i);
    }
};

Where_clauseContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


Where_clauseContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterWhere_clause(this);
	}
};

Where_clauseContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitWhere_clause(this);
	}
};

Where_clauseContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitWhere_clause(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Where_clauseContext = Where_clauseContext;

CQL3Parser.prototype.where_clause = function() {

    var localctx = new Where_clauseContext(this, this._ctx, this.state);
    this.enterRule(localctx, 56, CQL3Parser.RULE_where_clause);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 416;
        this.relation();
        this.state = 421;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.K_AND) {
            this.state = 417;
            this.match(CQL3Parser.K_AND);
            this.state = 418;
            this.relation();
            this.state = 423;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function RelationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_relation;
    return this;
}

RelationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
RelationContext.prototype.constructor = RelationContext;

RelationContext.prototype.column_name = function() {
    return this.getTypedRuleContext(Column_nameContext,0);
};

RelationContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

RelationContext.prototype.K_IN = function() {
    return this.getToken(CQL3Parser.K_IN, 0);
};

RelationContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterRelation(this);
	}
};

RelationContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitRelation(this);
	}
};

RelationContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitRelation(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.RelationContext = RelationContext;

CQL3Parser.prototype.relation = function() {

    var localctx = new RelationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 58, CQL3Parser.RULE_relation);
    var _la = 0; // Token type
    try {
        this.state = 447;
        var la_ = this._interp.adaptivePredict(this._input,36,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 424;
            this.column_name();
            this.state = 425;
            this.match(CQL3Parser.T__4);
            this.state = 426;
            this.term();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 428;
            this.column_name();
            this.state = 429;
            this.match(CQL3Parser.K_IN);
            this.state = 430;
            this.match(CQL3Parser.T__1);
            this.state = 439;
            _la = this._input.LA(1);
            if((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << CQL3Parser.T__7) | (1 << CQL3Parser.T__9) | (1 << CQL3Parser.T__11) | (1 << CQL3Parser.T__12))) !== 0) || _la===CQL3Parser.K_FALSE || _la===CQL3Parser.K_TRUE || ((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (CQL3Parser.IDENTIFIER - 86)) | (1 << (CQL3Parser.STRING - 86)) | (1 << (CQL3Parser.INTEGER - 86)) | (1 << (CQL3Parser.FLOAT - 86)) | (1 << (CQL3Parser.UUID - 86)) | (1 << (CQL3Parser.BLOB - 86)))) !== 0)) {
                this.state = 431;
                this.term();
                this.state = 436;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
                while(_la===CQL3Parser.T__3) {
                    this.state = 432;
                    this.match(CQL3Parser.T__3);
                    this.state = 433;
                    this.term();
                    this.state = 438;
                    this._errHandler.sync(this);
                    _la = this._input.LA(1);
                }
            }

            this.state = 441;
            this.match(CQL3Parser.T__2);
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 443;
            this.column_name();
            this.state = 444;
            this.match(CQL3Parser.K_IN);
            this.state = 445;
            this.match(CQL3Parser.T__9);
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Delete_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_delete_stmt;
    return this;
}

Delete_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Delete_stmtContext.prototype.constructor = Delete_stmtContext;

Delete_stmtContext.prototype.K_DELETE = function() {
    return this.getToken(CQL3Parser.K_DELETE, 0);
};

Delete_stmtContext.prototype.K_FROM = function() {
    return this.getToken(CQL3Parser.K_FROM, 0);
};

Delete_stmtContext.prototype.table_name = function() {
    return this.getTypedRuleContext(Table_nameContext,0);
};

Delete_stmtContext.prototype.K_WHERE = function() {
    return this.getToken(CQL3Parser.K_WHERE, 0);
};

Delete_stmtContext.prototype.where_clause = function() {
    return this.getTypedRuleContext(Where_clauseContext,0);
};

Delete_stmtContext.prototype.delete_selections = function() {
    return this.getTypedRuleContext(Delete_selectionsContext,0);
};

Delete_stmtContext.prototype.K_USING = function() {
    return this.getToken(CQL3Parser.K_USING, 0);
};

Delete_stmtContext.prototype.K_TIMESTAMP = function() {
    return this.getToken(CQL3Parser.K_TIMESTAMP, 0);
};

Delete_stmtContext.prototype.INTEGER = function() {
    return this.getToken(CQL3Parser.INTEGER, 0);
};

Delete_stmtContext.prototype.delete_conditions = function() {
    return this.getTypedRuleContext(Delete_conditionsContext,0);
};

Delete_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDelete_stmt(this);
	}
};

Delete_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDelete_stmt(this);
	}
};

Delete_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDelete_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Delete_stmtContext = Delete_stmtContext;

CQL3Parser.prototype.delete_stmt = function() {

    var localctx = new Delete_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 60, CQL3Parser.RULE_delete_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 449;
        this.match(CQL3Parser.K_DELETE);
        this.state = 451;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.IDENTIFIER) {
            this.state = 450;
            this.delete_selections();
        }

        this.state = 453;
        this.match(CQL3Parser.K_FROM);
        this.state = 454;
        this.table_name();
        this.state = 458;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_USING) {
            this.state = 455;
            this.match(CQL3Parser.K_USING);
            this.state = 456;
            this.match(CQL3Parser.K_TIMESTAMP);
            this.state = 457;
            this.match(CQL3Parser.INTEGER);
        }

        this.state = 460;
        this.match(CQL3Parser.K_WHERE);
        this.state = 461;
        this.where_clause();
        this.state = 463;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_IF) {
            this.state = 462;
            this.delete_conditions();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Delete_conditionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_delete_conditions;
    return this;
}

Delete_conditionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Delete_conditionsContext.prototype.constructor = Delete_conditionsContext;

Delete_conditionsContext.prototype.K_IF = function() {
    return this.getToken(CQL3Parser.K_IF, 0);
};

Delete_conditionsContext.prototype.K_EXISTS = function() {
    return this.getToken(CQL3Parser.K_EXISTS, 0);
};

Delete_conditionsContext.prototype.delete_condition = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Delete_conditionContext);
    } else {
        return this.getTypedRuleContext(Delete_conditionContext,i);
    }
};

Delete_conditionsContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


Delete_conditionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDelete_conditions(this);
	}
};

Delete_conditionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDelete_conditions(this);
	}
};

Delete_conditionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDelete_conditions(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Delete_conditionsContext = Delete_conditionsContext;

CQL3Parser.prototype.delete_conditions = function() {

    var localctx = new Delete_conditionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 62, CQL3Parser.RULE_delete_conditions);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 465;
        this.match(CQL3Parser.K_IF);
        this.state = 475;
        switch(this._input.LA(1)) {
        case CQL3Parser.K_EXISTS:
            this.state = 466;
            this.match(CQL3Parser.K_EXISTS);
            break;
        case CQL3Parser.IDENTIFIER:
            this.state = 467;
            this.delete_condition();
            this.state = 472;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while(_la===CQL3Parser.K_AND) {
                this.state = 468;
                this.match(CQL3Parser.K_AND);
                this.state = 469;
                this.delete_condition();
                this.state = 474;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Delete_conditionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_delete_condition;
    return this;
}

Delete_conditionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Delete_conditionContext.prototype.constructor = Delete_conditionContext;

Delete_conditionContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Delete_conditionContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

Delete_conditionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDelete_condition(this);
	}
};

Delete_conditionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDelete_condition(this);
	}
};

Delete_conditionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDelete_condition(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Delete_conditionContext = Delete_conditionContext;

CQL3Parser.prototype.delete_condition = function() {

    var localctx = new Delete_conditionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 64, CQL3Parser.RULE_delete_condition);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 477;
        this.match(CQL3Parser.IDENTIFIER);
        this.state = 482;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.T__7) {
            this.state = 478;
            this.match(CQL3Parser.T__7);
            this.state = 479;
            this.term();
            this.state = 480;
            this.match(CQL3Parser.T__8);
        }

        this.state = 484;
        this.match(CQL3Parser.T__4);
        this.state = 485;
        this.term();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Delete_selectionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_delete_selections;
    return this;
}

Delete_selectionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Delete_selectionsContext.prototype.constructor = Delete_selectionsContext;

Delete_selectionsContext.prototype.delete_selection = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Delete_selectionContext);
    } else {
        return this.getTypedRuleContext(Delete_selectionContext,i);
    }
};

Delete_selectionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDelete_selections(this);
	}
};

Delete_selectionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDelete_selections(this);
	}
};

Delete_selectionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDelete_selections(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Delete_selectionsContext = Delete_selectionsContext;

CQL3Parser.prototype.delete_selections = function() {

    var localctx = new Delete_selectionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 66, CQL3Parser.RULE_delete_selections);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 487;
        this.delete_selection();
        this.state = 492;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.T__3) {
            this.state = 488;
            this.match(CQL3Parser.T__3);
            this.state = 489;
            this.delete_selection();
            this.state = 494;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Delete_selectionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_delete_selection;
    return this;
}

Delete_selectionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Delete_selectionContext.prototype.constructor = Delete_selectionContext;

Delete_selectionContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Delete_selectionContext.prototype.term = function() {
    return this.getTypedRuleContext(TermContext,0);
};

Delete_selectionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterDelete_selection(this);
	}
};

Delete_selectionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitDelete_selection(this);
	}
};

Delete_selectionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitDelete_selection(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Delete_selectionContext = Delete_selectionContext;

CQL3Parser.prototype.delete_selection = function() {

    var localctx = new Delete_selectionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 68, CQL3Parser.RULE_delete_selection);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 495;
        this.match(CQL3Parser.IDENTIFIER);
        this.state = 500;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.T__7) {
            this.state = 496;
            this.match(CQL3Parser.T__7);
            this.state = 497;
            this.term();
            this.state = 498;
            this.match(CQL3Parser.T__8);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Batch_stmtContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_batch_stmt;
    return this;
}

Batch_stmtContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Batch_stmtContext.prototype.constructor = Batch_stmtContext;

Batch_stmtContext.prototype.K_BEGIN = function() {
    return this.getToken(CQL3Parser.K_BEGIN, 0);
};

Batch_stmtContext.prototype.K_BATCH = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_BATCH);
    } else {
        return this.getToken(CQL3Parser.K_BATCH, i);
    }
};


Batch_stmtContext.prototype.dml_statements = function() {
    return this.getTypedRuleContext(Dml_statementsContext,0);
};

Batch_stmtContext.prototype.K_APPLY = function() {
    return this.getToken(CQL3Parser.K_APPLY, 0);
};

Batch_stmtContext.prototype.batch_options = function() {
    return this.getTypedRuleContext(Batch_optionsContext,0);
};

Batch_stmtContext.prototype.K_UNLOGGED = function() {
    return this.getToken(CQL3Parser.K_UNLOGGED, 0);
};

Batch_stmtContext.prototype.K_COUNTER = function() {
    return this.getToken(CQL3Parser.K_COUNTER, 0);
};

Batch_stmtContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterBatch_stmt(this);
	}
};

Batch_stmtContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitBatch_stmt(this);
	}
};

Batch_stmtContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitBatch_stmt(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Batch_stmtContext = Batch_stmtContext;

CQL3Parser.prototype.batch_stmt = function() {

    var localctx = new Batch_stmtContext(this, this._ctx, this.state);
    this.enterRule(localctx, 70, CQL3Parser.RULE_batch_stmt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 502;
        this.match(CQL3Parser.K_BEGIN);
        this.state = 504;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_COUNTER || _la===CQL3Parser.K_UNLOGGED) {
            this.state = 503;
            _la = this._input.LA(1);
            if(!(_la===CQL3Parser.K_COUNTER || _la===CQL3Parser.K_UNLOGGED)) {
            this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        }

        this.state = 506;
        this.match(CQL3Parser.K_BATCH);
        this.state = 508;
        _la = this._input.LA(1);
        if(_la===CQL3Parser.K_USING) {
            this.state = 507;
            this.batch_options();
        }

        this.state = 510;
        this.dml_statements();
        this.state = 511;
        this.match(CQL3Parser.K_APPLY);
        this.state = 512;
        this.match(CQL3Parser.K_BATCH);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Batch_optionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_batch_options;
    return this;
}

Batch_optionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Batch_optionsContext.prototype.constructor = Batch_optionsContext;

Batch_optionsContext.prototype.K_USING = function() {
    return this.getToken(CQL3Parser.K_USING, 0);
};

Batch_optionsContext.prototype.batch_option = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Batch_optionContext);
    } else {
        return this.getTypedRuleContext(Batch_optionContext,i);
    }
};

Batch_optionsContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


Batch_optionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterBatch_options(this);
	}
};

Batch_optionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitBatch_options(this);
	}
};

Batch_optionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitBatch_options(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Batch_optionsContext = Batch_optionsContext;

CQL3Parser.prototype.batch_options = function() {

    var localctx = new Batch_optionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 72, CQL3Parser.RULE_batch_options);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 514;
        this.match(CQL3Parser.K_USING);
        this.state = 515;
        this.batch_option();
        this.state = 520;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.K_AND) {
            this.state = 516;
            this.match(CQL3Parser.K_AND);
            this.state = 517;
            this.batch_option();
            this.state = 522;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Batch_optionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_batch_option;
    return this;
}

Batch_optionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Batch_optionContext.prototype.constructor = Batch_optionContext;

Batch_optionContext.prototype.K_TIMESTAMP = function() {
    return this.getToken(CQL3Parser.K_TIMESTAMP, 0);
};

Batch_optionContext.prototype.INTEGER = function() {
    return this.getToken(CQL3Parser.INTEGER, 0);
};

Batch_optionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterBatch_option(this);
	}
};

Batch_optionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitBatch_option(this);
	}
};

Batch_optionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitBatch_option(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Batch_optionContext = Batch_optionContext;

CQL3Parser.prototype.batch_option = function() {

    var localctx = new Batch_optionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 74, CQL3Parser.RULE_batch_option);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 523;
        this.match(CQL3Parser.K_TIMESTAMP);
        this.state = 524;
        this.match(CQL3Parser.INTEGER);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Table_nameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_table_name;
    return this;
}

Table_nameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Table_nameContext.prototype.constructor = Table_nameContext;

Table_nameContext.prototype.table_name_noks = function() {
    return this.getTypedRuleContext(Table_name_noksContext,0);
};

Table_nameContext.prototype.keyspace_name = function() {
    return this.getTypedRuleContext(Keyspace_nameContext,0);
};

Table_nameContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterTable_name(this);
	}
};

Table_nameContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitTable_name(this);
	}
};

Table_nameContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitTable_name(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Table_nameContext = Table_nameContext;

CQL3Parser.prototype.table_name = function() {

    var localctx = new Table_nameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 76, CQL3Parser.RULE_table_name);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 529;
        var la_ = this._interp.adaptivePredict(this._input,48,this._ctx);
        if(la_===1) {
            this.state = 526;
            this.keyspace_name();
            this.state = 527;
            this.match(CQL3Parser.T__10);

        }
        this.state = 531;
        this.table_name_noks();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Table_name_noksContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_table_name_noks;
    return this;
}

Table_name_noksContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Table_name_noksContext.prototype.constructor = Table_name_noksContext;

Table_name_noksContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Table_name_noksContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterTable_name_noks(this);
	}
};

Table_name_noksContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitTable_name_noks(this);
	}
};

Table_name_noksContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitTable_name_noks(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Table_name_noksContext = Table_name_noksContext;

CQL3Parser.prototype.table_name_noks = function() {

    var localctx = new Table_name_noksContext(this, this._ctx, this.state);
    this.enterRule(localctx, 78, CQL3Parser.RULE_table_name_noks);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 533;
        this.match(CQL3Parser.IDENTIFIER);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Column_nameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_column_name;
    return this;
}

Column_nameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Column_nameContext.prototype.constructor = Column_nameContext;

Column_nameContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Column_nameContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterColumn_name(this);
	}
};

Column_nameContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitColumn_name(this);
	}
};

Column_nameContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitColumn_name(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Column_nameContext = Column_nameContext;

CQL3Parser.prototype.column_name = function() {

    var localctx = new Column_nameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 80, CQL3Parser.RULE_column_name);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 535;
        this.match(CQL3Parser.IDENTIFIER);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Table_optionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_table_options;
    return this;
}

Table_optionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Table_optionsContext.prototype.constructor = Table_optionsContext;

Table_optionsContext.prototype.table_option = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Table_optionContext);
    } else {
        return this.getTypedRuleContext(Table_optionContext,i);
    }
};

Table_optionsContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


Table_optionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterTable_options(this);
	}
};

Table_optionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitTable_options(this);
	}
};

Table_optionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitTable_options(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Table_optionsContext = Table_optionsContext;

CQL3Parser.prototype.table_options = function() {

    var localctx = new Table_optionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 82, CQL3Parser.RULE_table_options);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 537;
        this.table_option();
        this.state = 542;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.K_AND) {
            this.state = 538;
            this.match(CQL3Parser.K_AND);
            this.state = 539;
            this.table_option();
            this.state = 544;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Table_optionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_table_option;
    return this;
}

Table_optionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Table_optionContext.prototype.constructor = Table_optionContext;

Table_optionContext.prototype.property = function() {
    return this.getTypedRuleContext(PropertyContext,0);
};

Table_optionContext.prototype.K_COMPACT = function() {
    return this.getToken(CQL3Parser.K_COMPACT, 0);
};

Table_optionContext.prototype.K_STORAGE = function() {
    return this.getToken(CQL3Parser.K_STORAGE, 0);
};

Table_optionContext.prototype.K_CLUSTERING = function() {
    return this.getToken(CQL3Parser.K_CLUSTERING, 0);
};

Table_optionContext.prototype.K_ORDER = function() {
    return this.getToken(CQL3Parser.K_ORDER, 0);
};

Table_optionContext.prototype.K_BY = function() {
    return this.getToken(CQL3Parser.K_BY, 0);
};

Table_optionContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Table_optionContext.prototype.asc_or_desc = function() {
    return this.getTypedRuleContext(Asc_or_descContext,0);
};

Table_optionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterTable_option(this);
	}
};

Table_optionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitTable_option(this);
	}
};

Table_optionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitTable_option(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Table_optionContext = Table_optionContext;

CQL3Parser.prototype.table_option = function() {

    var localctx = new Table_optionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 84, CQL3Parser.RULE_table_option);
    try {
        this.state = 560;
        var la_ = this._interp.adaptivePredict(this._input,50,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 545;
            this.property();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 546;
            this.match(CQL3Parser.K_COMPACT);
            this.state = 547;
            this.match(CQL3Parser.K_STORAGE);
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 548;
            this.match(CQL3Parser.K_CLUSTERING);
            this.state = 549;
            this.match(CQL3Parser.K_ORDER);
            this.state = 550;
            this.match(CQL3Parser.K_BY);
            this.state = 551;
            this.match(CQL3Parser.IDENTIFIER);
            break;

        case 4:
            this.enterOuterAlt(localctx, 4);
            this.state = 552;
            this.match(CQL3Parser.K_CLUSTERING);
            this.state = 553;
            this.match(CQL3Parser.K_ORDER);
            this.state = 554;
            this.match(CQL3Parser.K_BY);
            this.state = 555;
            this.match(CQL3Parser.T__1);
            this.state = 556;
            this.match(CQL3Parser.IDENTIFIER);
            this.state = 557;
            this.asc_or_desc();
            this.state = 558;
            this.match(CQL3Parser.T__2);
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Asc_or_descContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_asc_or_desc;
    return this;
}

Asc_or_descContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Asc_or_descContext.prototype.constructor = Asc_or_descContext;

Asc_or_descContext.prototype.K_ASC = function() {
    return this.getToken(CQL3Parser.K_ASC, 0);
};

Asc_or_descContext.prototype.K_DESC = function() {
    return this.getToken(CQL3Parser.K_DESC, 0);
};

Asc_or_descContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterAsc_or_desc(this);
	}
};

Asc_or_descContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitAsc_or_desc(this);
	}
};

Asc_or_descContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitAsc_or_desc(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Asc_or_descContext = Asc_or_descContext;

CQL3Parser.prototype.asc_or_desc = function() {

    var localctx = new Asc_or_descContext(this, this._ctx, this.state);
    this.enterRule(localctx, 86, CQL3Parser.RULE_asc_or_desc);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 562;
        _la = this._input.LA(1);
        if(!(_la===CQL3Parser.K_ASC || _la===CQL3Parser.K_DESC)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Column_definitionsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_column_definitions;
    return this;
}

Column_definitionsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Column_definitionsContext.prototype.constructor = Column_definitionsContext;

Column_definitionsContext.prototype.column_definition = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Column_definitionContext);
    } else {
        return this.getTypedRuleContext(Column_definitionContext,i);
    }
};

Column_definitionsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterColumn_definitions(this);
	}
};

Column_definitionsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitColumn_definitions(this);
	}
};

Column_definitionsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitColumn_definitions(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Column_definitionsContext = Column_definitionsContext;

CQL3Parser.prototype.column_definitions = function() {

    var localctx = new Column_definitionsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 88, CQL3Parser.RULE_column_definitions);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 564;
        this.match(CQL3Parser.T__1);
        this.state = 565;
        this.column_definition();
        this.state = 570;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.T__3) {
            this.state = 566;
            this.match(CQL3Parser.T__3);
            this.state = 567;
            this.column_definition();
            this.state = 572;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 573;
        this.match(CQL3Parser.T__2);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Column_definitionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_column_definition;
    return this;
}

Column_definitionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Column_definitionContext.prototype.constructor = Column_definitionContext;

Column_definitionContext.prototype.column_name = function() {
    return this.getTypedRuleContext(Column_nameContext,0);
};

Column_definitionContext.prototype.column_type = function() {
    return this.getTypedRuleContext(Column_typeContext,0);
};

Column_definitionContext.prototype.K_STATIC = function() {
    return this.getToken(CQL3Parser.K_STATIC, 0);
};

Column_definitionContext.prototype.K_PRIMARY = function() {
    return this.getToken(CQL3Parser.K_PRIMARY, 0);
};

Column_definitionContext.prototype.K_KEY = function() {
    return this.getToken(CQL3Parser.K_KEY, 0);
};

Column_definitionContext.prototype.primary_key = function() {
    return this.getTypedRuleContext(Primary_keyContext,0);
};

Column_definitionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterColumn_definition(this);
	}
};

Column_definitionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitColumn_definition(this);
	}
};

Column_definitionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitColumn_definition(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Column_definitionContext = Column_definitionContext;

CQL3Parser.prototype.column_definition = function() {

    var localctx = new Column_definitionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 90, CQL3Parser.RULE_column_definition);
    var _la = 0; // Token type
    try {
        this.state = 587;
        switch(this._input.LA(1)) {
        case CQL3Parser.IDENTIFIER:
            this.enterOuterAlt(localctx, 1);
            this.state = 575;
            this.column_name();
            this.state = 576;
            this.column_type();
            this.state = 578;
            _la = this._input.LA(1);
            if(_la===CQL3Parser.K_STATIC) {
                this.state = 577;
                this.match(CQL3Parser.K_STATIC);
            }

            this.state = 582;
            _la = this._input.LA(1);
            if(_la===CQL3Parser.K_PRIMARY) {
                this.state = 580;
                this.match(CQL3Parser.K_PRIMARY);
                this.state = 581;
                this.match(CQL3Parser.K_KEY);
            }

            break;
        case CQL3Parser.K_PRIMARY:
            this.enterOuterAlt(localctx, 2);
            this.state = 584;
            this.match(CQL3Parser.K_PRIMARY);
            this.state = 585;
            this.match(CQL3Parser.K_KEY);
            this.state = 586;
            this.primary_key();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Column_typeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_column_type;
    return this;
}

Column_typeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Column_typeContext.prototype.constructor = Column_typeContext;

Column_typeContext.prototype.data_type = function() {
    return this.getTypedRuleContext(Data_typeContext,0);
};

Column_typeContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterColumn_type(this);
	}
};

Column_typeContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitColumn_type(this);
	}
};

Column_typeContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitColumn_type(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Column_typeContext = Column_typeContext;

CQL3Parser.prototype.column_type = function() {

    var localctx = new Column_typeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 92, CQL3Parser.RULE_column_type);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 589;
        this.data_type();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Primary_keyContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_primary_key;
    return this;
}

Primary_keyContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Primary_keyContext.prototype.constructor = Primary_keyContext;

Primary_keyContext.prototype.partition_key = function() {
    return this.getTypedRuleContext(Partition_keyContext,0);
};

Primary_keyContext.prototype.clustering_column = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Clustering_columnContext);
    } else {
        return this.getTypedRuleContext(Clustering_columnContext,i);
    }
};

Primary_keyContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterPrimary_key(this);
	}
};

Primary_keyContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitPrimary_key(this);
	}
};

Primary_keyContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitPrimary_key(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Primary_keyContext = Primary_keyContext;

CQL3Parser.prototype.primary_key = function() {

    var localctx = new Primary_keyContext(this, this._ctx, this.state);
    this.enterRule(localctx, 94, CQL3Parser.RULE_primary_key);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 591;
        this.match(CQL3Parser.T__1);
        this.state = 592;
        this.partition_key();
        this.state = 597;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.T__3) {
            this.state = 593;
            this.match(CQL3Parser.T__3);
            this.state = 594;
            this.clustering_column();
            this.state = 599;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 600;
        this.match(CQL3Parser.T__2);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Partition_keyContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_partition_key;
    return this;
}

Partition_keyContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Partition_keyContext.prototype.constructor = Partition_keyContext;

Partition_keyContext.prototype.column_name = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Column_nameContext);
    } else {
        return this.getTypedRuleContext(Column_nameContext,i);
    }
};

Partition_keyContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterPartition_key(this);
	}
};

Partition_keyContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitPartition_key(this);
	}
};

Partition_keyContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitPartition_key(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Partition_keyContext = Partition_keyContext;

CQL3Parser.prototype.partition_key = function() {

    var localctx = new Partition_keyContext(this, this._ctx, this.state);
    this.enterRule(localctx, 96, CQL3Parser.RULE_partition_key);
    var _la = 0; // Token type
    try {
        this.state = 614;
        switch(this._input.LA(1)) {
        case CQL3Parser.IDENTIFIER:
            this.enterOuterAlt(localctx, 1);
            this.state = 602;
            this.column_name();
            break;
        case CQL3Parser.T__1:
            this.enterOuterAlt(localctx, 2);
            this.state = 603;
            this.match(CQL3Parser.T__1);
            this.state = 604;
            this.column_name();
            this.state = 609;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while(_la===CQL3Parser.T__3) {
                this.state = 605;
                this.match(CQL3Parser.T__3);
                this.state = 606;
                this.column_name();
                this.state = 611;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
            this.state = 612;
            this.match(CQL3Parser.T__2);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Clustering_columnContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_clustering_column;
    return this;
}

Clustering_columnContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Clustering_columnContext.prototype.constructor = Clustering_columnContext;

Clustering_columnContext.prototype.column_name = function() {
    return this.getTypedRuleContext(Column_nameContext,0);
};

Clustering_columnContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterClustering_column(this);
	}
};

Clustering_columnContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitClustering_column(this);
	}
};

Clustering_columnContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitClustering_column(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Clustering_columnContext = Clustering_columnContext;

CQL3Parser.prototype.clustering_column = function() {

    var localctx = new Clustering_columnContext(this, this._ctx, this.state);
    this.enterRule(localctx, 98, CQL3Parser.RULE_clustering_column);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 616;
        this.column_name();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Keyspace_nameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_keyspace_name;
    return this;
}

Keyspace_nameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Keyspace_nameContext.prototype.constructor = Keyspace_nameContext;

Keyspace_nameContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Keyspace_nameContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterKeyspace_name(this);
	}
};

Keyspace_nameContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitKeyspace_name(this);
	}
};

Keyspace_nameContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitKeyspace_name(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Keyspace_nameContext = Keyspace_nameContext;

CQL3Parser.prototype.keyspace_name = function() {

    var localctx = new Keyspace_nameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 100, CQL3Parser.RULE_keyspace_name);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 618;
        this.match(CQL3Parser.IDENTIFIER);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function If_not_existsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_if_not_exists;
    return this;
}

If_not_existsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
If_not_existsContext.prototype.constructor = If_not_existsContext;

If_not_existsContext.prototype.K_IF = function() {
    return this.getToken(CQL3Parser.K_IF, 0);
};

If_not_existsContext.prototype.K_NOT = function() {
    return this.getToken(CQL3Parser.K_NOT, 0);
};

If_not_existsContext.prototype.K_EXISTS = function() {
    return this.getToken(CQL3Parser.K_EXISTS, 0);
};

If_not_existsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterIf_not_exists(this);
	}
};

If_not_existsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitIf_not_exists(this);
	}
};

If_not_existsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitIf_not_exists(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.If_not_existsContext = If_not_existsContext;

CQL3Parser.prototype.if_not_exists = function() {

    var localctx = new If_not_existsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 102, CQL3Parser.RULE_if_not_exists);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 620;
        this.match(CQL3Parser.K_IF);
        this.state = 621;
        this.match(CQL3Parser.K_NOT);
        this.state = 622;
        this.match(CQL3Parser.K_EXISTS);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function If_existsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_if_exists;
    return this;
}

If_existsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
If_existsContext.prototype.constructor = If_existsContext;

If_existsContext.prototype.K_IF = function() {
    return this.getToken(CQL3Parser.K_IF, 0);
};

If_existsContext.prototype.K_EXISTS = function() {
    return this.getToken(CQL3Parser.K_EXISTS, 0);
};

If_existsContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterIf_exists(this);
	}
};

If_existsContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitIf_exists(this);
	}
};

If_existsContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitIf_exists(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.If_existsContext = If_existsContext;

CQL3Parser.prototype.if_exists = function() {

    var localctx = new If_existsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 104, CQL3Parser.RULE_if_exists);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 624;
        this.match(CQL3Parser.K_IF);
        this.state = 625;
        this.match(CQL3Parser.K_EXISTS);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ConstantContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_constant;
    return this;
}

ConstantContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ConstantContext.prototype.constructor = ConstantContext;

ConstantContext.prototype.STRING = function() {
    return this.getToken(CQL3Parser.STRING, 0);
};

ConstantContext.prototype.INTEGER = function() {
    return this.getToken(CQL3Parser.INTEGER, 0);
};

ConstantContext.prototype.FLOAT = function() {
    return this.getToken(CQL3Parser.FLOAT, 0);
};

ConstantContext.prototype.bool = function() {
    return this.getTypedRuleContext(BoolContext,0);
};

ConstantContext.prototype.UUID = function() {
    return this.getToken(CQL3Parser.UUID, 0);
};

ConstantContext.prototype.BLOB = function() {
    return this.getToken(CQL3Parser.BLOB, 0);
};

ConstantContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterConstant(this);
	}
};

ConstantContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitConstant(this);
	}
};

ConstantContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitConstant(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.ConstantContext = ConstantContext;

CQL3Parser.prototype.constant = function() {

    var localctx = new ConstantContext(this, this._ctx, this.state);
    this.enterRule(localctx, 106, CQL3Parser.RULE_constant);
    try {
        this.state = 633;
        switch(this._input.LA(1)) {
        case CQL3Parser.STRING:
            this.enterOuterAlt(localctx, 1);
            this.state = 627;
            this.match(CQL3Parser.STRING);
            break;
        case CQL3Parser.INTEGER:
            this.enterOuterAlt(localctx, 2);
            this.state = 628;
            this.match(CQL3Parser.INTEGER);
            break;
        case CQL3Parser.FLOAT:
            this.enterOuterAlt(localctx, 3);
            this.state = 629;
            this.match(CQL3Parser.FLOAT);
            break;
        case CQL3Parser.K_FALSE:
        case CQL3Parser.K_TRUE:
            this.enterOuterAlt(localctx, 4);
            this.state = 630;
            this.bool();
            break;
        case CQL3Parser.UUID:
            this.enterOuterAlt(localctx, 5);
            this.state = 631;
            this.match(CQL3Parser.UUID);
            break;
        case CQL3Parser.BLOB:
            this.enterOuterAlt(localctx, 6);
            this.state = 632;
            this.match(CQL3Parser.BLOB);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function VariableContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_variable;
    return this;
}

VariableContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
VariableContext.prototype.constructor = VariableContext;

VariableContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

VariableContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterVariable(this);
	}
};

VariableContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitVariable(this);
	}
};

VariableContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitVariable(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.VariableContext = VariableContext;

CQL3Parser.prototype.variable = function() {

    var localctx = new VariableContext(this, this._ctx, this.state);
    this.enterRule(localctx, 108, CQL3Parser.RULE_variable);
    try {
        this.state = 638;
        switch(this._input.LA(1)) {
        case CQL3Parser.T__9:
            this.enterOuterAlt(localctx, 1);
            this.state = 635;
            this.match(CQL3Parser.T__9);
            break;
        case CQL3Parser.T__11:
            this.enterOuterAlt(localctx, 2);
            this.state = 636;
            this.match(CQL3Parser.T__11);
            this.state = 637;
            this.match(CQL3Parser.IDENTIFIER);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function TermContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_term;
    return this;
}

TermContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
TermContext.prototype.constructor = TermContext;

TermContext.prototype.constant = function() {
    return this.getTypedRuleContext(ConstantContext,0);
};

TermContext.prototype.collection = function() {
    return this.getTypedRuleContext(CollectionContext,0);
};

TermContext.prototype.variable = function() {
    return this.getTypedRuleContext(VariableContext,0);
};

TermContext.prototype.function = function() {
    return this.getTypedRuleContext(FunctionContext,0);
};

TermContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterTerm(this);
	}
};

TermContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitTerm(this);
	}
};

TermContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitTerm(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.TermContext = TermContext;

CQL3Parser.prototype.term = function() {

    var localctx = new TermContext(this, this._ctx, this.state);
    this.enterRule(localctx, 110, CQL3Parser.RULE_term);
    try {
        this.state = 644;
        switch(this._input.LA(1)) {
        case CQL3Parser.K_FALSE:
        case CQL3Parser.K_TRUE:
        case CQL3Parser.STRING:
        case CQL3Parser.INTEGER:
        case CQL3Parser.FLOAT:
        case CQL3Parser.UUID:
        case CQL3Parser.BLOB:
            this.enterOuterAlt(localctx, 1);
            this.state = 640;
            this.constant();
            break;
        case CQL3Parser.T__7:
        case CQL3Parser.T__12:
            this.enterOuterAlt(localctx, 2);
            this.state = 641;
            this.collection();
            break;
        case CQL3Parser.T__9:
        case CQL3Parser.T__11:
            this.enterOuterAlt(localctx, 3);
            this.state = 642;
            this.variable();
            break;
        case CQL3Parser.IDENTIFIER:
            this.enterOuterAlt(localctx, 4);
            this.state = 643;
            this.function();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function CollectionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_collection;
    return this;
}

CollectionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
CollectionContext.prototype.constructor = CollectionContext;

CollectionContext.prototype.map = function() {
    return this.getTypedRuleContext(MapContext,0);
};

CollectionContext.prototype.set = function() {
    return this.getTypedRuleContext(SetContext,0);
};

CollectionContext.prototype.list = function() {
    return this.getTypedRuleContext(ListContext,0);
};

CollectionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterCollection(this);
	}
};

CollectionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitCollection(this);
	}
};

CollectionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitCollection(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.CollectionContext = CollectionContext;

CQL3Parser.prototype.collection = function() {

    var localctx = new CollectionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 112, CQL3Parser.RULE_collection);
    try {
        this.state = 649;
        var la_ = this._interp.adaptivePredict(this._input,61,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 646;
            this.map();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 647;
            this.set();
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 648;
            this.list();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function MapContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_map;
    return this;
}

MapContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
MapContext.prototype.constructor = MapContext;

MapContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

MapContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterMap(this);
	}
};

MapContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitMap(this);
	}
};

MapContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitMap(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.MapContext = MapContext;

CQL3Parser.prototype.map = function() {

    var localctx = new MapContext(this, this._ctx, this.state);
    this.enterRule(localctx, 114, CQL3Parser.RULE_map);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 651;
        this.match(CQL3Parser.T__12);
        this.state = 665;
        _la = this._input.LA(1);
        if((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << CQL3Parser.T__7) | (1 << CQL3Parser.T__9) | (1 << CQL3Parser.T__11) | (1 << CQL3Parser.T__12))) !== 0) || _la===CQL3Parser.K_FALSE || _la===CQL3Parser.K_TRUE || ((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (CQL3Parser.IDENTIFIER - 86)) | (1 << (CQL3Parser.STRING - 86)) | (1 << (CQL3Parser.INTEGER - 86)) | (1 << (CQL3Parser.FLOAT - 86)) | (1 << (CQL3Parser.UUID - 86)) | (1 << (CQL3Parser.BLOB - 86)))) !== 0)) {
            this.state = 652;
            this.term();
            this.state = 653;
            this.match(CQL3Parser.T__11);
            this.state = 654;
            this.term();
            this.state = 662;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while(_la===CQL3Parser.T__3) {
                this.state = 655;
                this.match(CQL3Parser.T__3);
                this.state = 656;
                this.term();
                this.state = 657;
                this.match(CQL3Parser.T__11);
                this.state = 658;
                this.term();
                this.state = 664;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        }

        this.state = 667;
        this.match(CQL3Parser.T__13);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function SetContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_set;
    return this;
}

SetContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
SetContext.prototype.constructor = SetContext;

SetContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

SetContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterSet(this);
	}
};

SetContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitSet(this);
	}
};

SetContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitSet(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.SetContext = SetContext;

CQL3Parser.prototype.set = function() {

    var localctx = new SetContext(this, this._ctx, this.state);
    this.enterRule(localctx, 116, CQL3Parser.RULE_set);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 669;
        this.match(CQL3Parser.T__12);
        this.state = 678;
        _la = this._input.LA(1);
        if((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << CQL3Parser.T__7) | (1 << CQL3Parser.T__9) | (1 << CQL3Parser.T__11) | (1 << CQL3Parser.T__12))) !== 0) || _la===CQL3Parser.K_FALSE || _la===CQL3Parser.K_TRUE || ((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (CQL3Parser.IDENTIFIER - 86)) | (1 << (CQL3Parser.STRING - 86)) | (1 << (CQL3Parser.INTEGER - 86)) | (1 << (CQL3Parser.FLOAT - 86)) | (1 << (CQL3Parser.UUID - 86)) | (1 << (CQL3Parser.BLOB - 86)))) !== 0)) {
            this.state = 670;
            this.term();
            this.state = 675;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while(_la===CQL3Parser.T__3) {
                this.state = 671;
                this.match(CQL3Parser.T__3);
                this.state = 672;
                this.term();
                this.state = 677;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        }

        this.state = 680;
        this.match(CQL3Parser.T__13);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ListContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_list;
    return this;
}

ListContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ListContext.prototype.constructor = ListContext;

ListContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

ListContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterList(this);
	}
};

ListContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitList(this);
	}
};

ListContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitList(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.ListContext = ListContext;

CQL3Parser.prototype.list = function() {

    var localctx = new ListContext(this, this._ctx, this.state);
    this.enterRule(localctx, 118, CQL3Parser.RULE_list);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 682;
        this.match(CQL3Parser.T__7);
        this.state = 691;
        _la = this._input.LA(1);
        if((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << CQL3Parser.T__7) | (1 << CQL3Parser.T__9) | (1 << CQL3Parser.T__11) | (1 << CQL3Parser.T__12))) !== 0) || _la===CQL3Parser.K_FALSE || _la===CQL3Parser.K_TRUE || ((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (CQL3Parser.IDENTIFIER - 86)) | (1 << (CQL3Parser.STRING - 86)) | (1 << (CQL3Parser.INTEGER - 86)) | (1 << (CQL3Parser.FLOAT - 86)) | (1 << (CQL3Parser.UUID - 86)) | (1 << (CQL3Parser.BLOB - 86)))) !== 0)) {
            this.state = 683;
            this.term();
            this.state = 688;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while(_la===CQL3Parser.T__3) {
                this.state = 684;
                this.match(CQL3Parser.T__3);
                this.state = 685;
                this.term();
                this.state = 690;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        }

        this.state = 693;
        this.match(CQL3Parser.T__8);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function FunctionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_function;
    return this;
}

FunctionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
FunctionContext.prototype.constructor = FunctionContext;

FunctionContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

FunctionContext.prototype.term = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TermContext);
    } else {
        return this.getTypedRuleContext(TermContext,i);
    }
};

FunctionContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterFunction(this);
	}
};

FunctionContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitFunction(this);
	}
};

FunctionContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitFunction(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.FunctionContext = FunctionContext;

CQL3Parser.prototype.function = function() {

    var localctx = new FunctionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 120, CQL3Parser.RULE_function);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 695;
        this.match(CQL3Parser.IDENTIFIER);
        this.state = 696;
        this.match(CQL3Parser.T__1);
        this.state = 705;
        _la = this._input.LA(1);
        if((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << CQL3Parser.T__7) | (1 << CQL3Parser.T__9) | (1 << CQL3Parser.T__11) | (1 << CQL3Parser.T__12))) !== 0) || _la===CQL3Parser.K_FALSE || _la===CQL3Parser.K_TRUE || ((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (CQL3Parser.IDENTIFIER - 86)) | (1 << (CQL3Parser.STRING - 86)) | (1 << (CQL3Parser.INTEGER - 86)) | (1 << (CQL3Parser.FLOAT - 86)) | (1 << (CQL3Parser.UUID - 86)) | (1 << (CQL3Parser.BLOB - 86)))) !== 0)) {
            this.state = 697;
            this.term();
            this.state = 702;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while(_la===CQL3Parser.T__3) {
                this.state = 698;
                this.match(CQL3Parser.T__3);
                this.state = 699;
                this.term();
                this.state = 704;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        }

        this.state = 707;
        this.match(CQL3Parser.T__2);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PropertiesContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_properties;
    return this;
}

PropertiesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PropertiesContext.prototype.constructor = PropertiesContext;

PropertiesContext.prototype.property = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(PropertyContext);
    } else {
        return this.getTypedRuleContext(PropertyContext,i);
    }
};

PropertiesContext.prototype.K_AND = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(CQL3Parser.K_AND);
    } else {
        return this.getToken(CQL3Parser.K_AND, i);
    }
};


PropertiesContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterProperties(this);
	}
};

PropertiesContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitProperties(this);
	}
};

PropertiesContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitProperties(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.PropertiesContext = PropertiesContext;

CQL3Parser.prototype.properties = function() {

    var localctx = new PropertiesContext(this, this._ctx, this.state);
    this.enterRule(localctx, 122, CQL3Parser.RULE_properties);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 709;
        this.property();
        this.state = 714;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===CQL3Parser.K_AND) {
            this.state = 710;
            this.match(CQL3Parser.K_AND);
            this.state = 711;
            this.property();
            this.state = 716;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PropertyContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_property;
    return this;
}

PropertyContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PropertyContext.prototype.constructor = PropertyContext;

PropertyContext.prototype.property_name = function() {
    return this.getTypedRuleContext(Property_nameContext,0);
};

PropertyContext.prototype.property_value = function() {
    return this.getTypedRuleContext(Property_valueContext,0);
};

PropertyContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterProperty(this);
	}
};

PropertyContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitProperty(this);
	}
};

PropertyContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitProperty(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.PropertyContext = PropertyContext;

CQL3Parser.prototype.property = function() {

    var localctx = new PropertyContext(this, this._ctx, this.state);
    this.enterRule(localctx, 124, CQL3Parser.RULE_property);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 717;
        this.property_name();
        this.state = 718;
        this.match(CQL3Parser.T__4);
        this.state = 719;
        this.property_value();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Property_nameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_property_name;
    return this;
}

Property_nameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Property_nameContext.prototype.constructor = Property_nameContext;

Property_nameContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Property_nameContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterProperty_name(this);
	}
};

Property_nameContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitProperty_name(this);
	}
};

Property_nameContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitProperty_name(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Property_nameContext = Property_nameContext;

CQL3Parser.prototype.property_name = function() {

    var localctx = new Property_nameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 126, CQL3Parser.RULE_property_name);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 721;
        this.match(CQL3Parser.IDENTIFIER);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Property_valueContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_property_value;
    return this;
}

Property_valueContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Property_valueContext.prototype.constructor = Property_valueContext;

Property_valueContext.prototype.IDENTIFIER = function() {
    return this.getToken(CQL3Parser.IDENTIFIER, 0);
};

Property_valueContext.prototype.constant = function() {
    return this.getTypedRuleContext(ConstantContext,0);
};

Property_valueContext.prototype.map = function() {
    return this.getTypedRuleContext(MapContext,0);
};

Property_valueContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterProperty_value(this);
	}
};

Property_valueContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitProperty_value(this);
	}
};

Property_valueContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitProperty_value(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Property_valueContext = Property_valueContext;

CQL3Parser.prototype.property_value = function() {

    var localctx = new Property_valueContext(this, this._ctx, this.state);
    this.enterRule(localctx, 128, CQL3Parser.RULE_property_value);
    try {
        this.state = 726;
        switch(this._input.LA(1)) {
        case CQL3Parser.IDENTIFIER:
            this.enterOuterAlt(localctx, 1);
            this.state = 723;
            this.match(CQL3Parser.IDENTIFIER);
            break;
        case CQL3Parser.K_FALSE:
        case CQL3Parser.K_TRUE:
        case CQL3Parser.STRING:
        case CQL3Parser.INTEGER:
        case CQL3Parser.FLOAT:
        case CQL3Parser.UUID:
        case CQL3Parser.BLOB:
            this.enterOuterAlt(localctx, 2);
            this.state = 724;
            this.constant();
            break;
        case CQL3Parser.T__12:
            this.enterOuterAlt(localctx, 3);
            this.state = 725;
            this.map();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Data_typeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_data_type;
    return this;
}

Data_typeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Data_typeContext.prototype.constructor = Data_typeContext;

Data_typeContext.prototype.native_type = function() {
    return this.getTypedRuleContext(Native_typeContext,0);
};

Data_typeContext.prototype.collection_type = function() {
    return this.getTypedRuleContext(Collection_typeContext,0);
};

Data_typeContext.prototype.STRING = function() {
    return this.getToken(CQL3Parser.STRING, 0);
};

Data_typeContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterData_type(this);
	}
};

Data_typeContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitData_type(this);
	}
};

Data_typeContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitData_type(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Data_typeContext = Data_typeContext;

CQL3Parser.prototype.data_type = function() {

    var localctx = new Data_typeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 130, CQL3Parser.RULE_data_type);
    try {
        this.state = 731;
        switch(this._input.LA(1)) {
        case CQL3Parser.T__14:
        case CQL3Parser.T__15:
        case CQL3Parser.T__16:
        case CQL3Parser.T__17:
        case CQL3Parser.T__18:
        case CQL3Parser.T__19:
        case CQL3Parser.T__20:
        case CQL3Parser.T__21:
        case CQL3Parser.T__22:
        case CQL3Parser.T__23:
        case CQL3Parser.T__24:
        case CQL3Parser.T__25:
        case CQL3Parser.T__26:
        case CQL3Parser.T__27:
        case CQL3Parser.T__28:
        case CQL3Parser.T__29:
        case CQL3Parser.T__30:
            this.enterOuterAlt(localctx, 1);
            this.state = 728;
            this.native_type();
            break;
        case CQL3Parser.T__31:
        case CQL3Parser.T__34:
        case CQL3Parser.T__35:
            this.enterOuterAlt(localctx, 2);
            this.state = 729;
            this.collection_type();
            break;
        case CQL3Parser.STRING:
            this.enterOuterAlt(localctx, 3);
            this.state = 730;
            this.match(CQL3Parser.STRING);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Native_typeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_native_type;
    return this;
}

Native_typeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Native_typeContext.prototype.constructor = Native_typeContext;


Native_typeContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterNative_type(this);
	}
};

Native_typeContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitNative_type(this);
	}
};

Native_typeContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitNative_type(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Native_typeContext = Native_typeContext;

CQL3Parser.prototype.native_type = function() {

    var localctx = new Native_typeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 132, CQL3Parser.RULE_native_type);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 733;
        _la = this._input.LA(1);
        if(!((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << CQL3Parser.T__14) | (1 << CQL3Parser.T__15) | (1 << CQL3Parser.T__16) | (1 << CQL3Parser.T__17) | (1 << CQL3Parser.T__18) | (1 << CQL3Parser.T__19) | (1 << CQL3Parser.T__20) | (1 << CQL3Parser.T__21) | (1 << CQL3Parser.T__22) | (1 << CQL3Parser.T__23) | (1 << CQL3Parser.T__24) | (1 << CQL3Parser.T__25) | (1 << CQL3Parser.T__26) | (1 << CQL3Parser.T__27) | (1 << CQL3Parser.T__28) | (1 << CQL3Parser.T__29) | (1 << CQL3Parser.T__30))) !== 0))) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function Collection_typeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_collection_type;
    return this;
}

Collection_typeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
Collection_typeContext.prototype.constructor = Collection_typeContext;

Collection_typeContext.prototype.native_type = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(Native_typeContext);
    } else {
        return this.getTypedRuleContext(Native_typeContext,i);
    }
};

Collection_typeContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterCollection_type(this);
	}
};

Collection_typeContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitCollection_type(this);
	}
};

Collection_typeContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitCollection_type(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.Collection_typeContext = Collection_typeContext;

CQL3Parser.prototype.collection_type = function() {

    var localctx = new Collection_typeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 134, CQL3Parser.RULE_collection_type);
    try {
        this.state = 752;
        switch(this._input.LA(1)) {
        case CQL3Parser.T__31:
            this.enterOuterAlt(localctx, 1);
            this.state = 735;
            this.match(CQL3Parser.T__31);
            this.state = 736;
            this.match(CQL3Parser.T__32);
            this.state = 737;
            this.native_type();
            this.state = 738;
            this.match(CQL3Parser.T__33);
            break;
        case CQL3Parser.T__34:
            this.enterOuterAlt(localctx, 2);
            this.state = 740;
            this.match(CQL3Parser.T__34);
            this.state = 741;
            this.match(CQL3Parser.T__32);
            this.state = 742;
            this.native_type();
            this.state = 743;
            this.match(CQL3Parser.T__33);
            break;
        case CQL3Parser.T__35:
            this.enterOuterAlt(localctx, 3);
            this.state = 745;
            this.match(CQL3Parser.T__35);
            this.state = 746;
            this.match(CQL3Parser.T__32);
            this.state = 747;
            this.native_type();
            this.state = 748;
            this.match(CQL3Parser.T__3);
            this.state = 749;
            this.native_type();
            this.state = 750;
            this.match(CQL3Parser.T__33);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function BoolContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = CQL3Parser.RULE_bool;
    return this;
}

BoolContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
BoolContext.prototype.constructor = BoolContext;

BoolContext.prototype.K_TRUE = function() {
    return this.getToken(CQL3Parser.K_TRUE, 0);
};

BoolContext.prototype.K_FALSE = function() {
    return this.getToken(CQL3Parser.K_FALSE, 0);
};

BoolContext.prototype.enterRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.enterBool(this);
	}
};

BoolContext.prototype.exitRule = function(listener) {
    if(listener instanceof CQL3Listener ) {
        listener.exitBool(this);
	}
};

BoolContext.prototype.accept = function(visitor) {
    if ( visitor instanceof CQL3Visitor ) {
        return visitor.visitBool(this);
    } else {
        return visitor.visitChildren(this);
    }
};




CQL3Parser.BoolContext = BoolContext;

CQL3Parser.prototype.bool = function() {

    var localctx = new BoolContext(this, this._ctx, this.state);
    this.enterRule(localctx, 136, CQL3Parser.RULE_bool);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 754;
        _la = this._input.LA(1);
        if(!(_la===CQL3Parser.K_FALSE || _la===CQL3Parser.K_TRUE)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


exports.CQL3Parser = CQL3Parser;
