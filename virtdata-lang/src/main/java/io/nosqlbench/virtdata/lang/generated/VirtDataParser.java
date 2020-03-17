// Generated from VirtData.g4 by ANTLR 4.8
package io.nosqlbench.virtdata.lang.generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VirtDataParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, LONG=7, DOUBLE=8, INTEGER=9, 
		FLOAT=10, BOOLEAN=11, NEWLINE=12, COMPOSE=13, TYPEARROW=14, ASSIGN=15, 
		SSTRING_LITERAL=16, DSTRING_LITERAL=17, ID=18, IDPART=19, WS=20;
	public static final int
		RULE_virtdataRecipe = 0, RULE_virtdataFlow = 1, RULE_expression = 2, RULE_virtdataCall = 3, 
		RULE_lvalue = 4, RULE_inputType = 5, RULE_funcName = 6, RULE_outputType = 7, 
		RULE_arg = 8, RULE_ref = 9, RULE_value = 10, RULE_stringValue = 11, RULE_longValue = 12, 
		RULE_doubleValue = 13, RULE_integerValue = 14, RULE_floatValue = 15, RULE_booleanValue = 16, 
		RULE_specend = 17;
	private static String[] makeRuleNames() {
		return new String[] {
			"virtdataRecipe", "virtdataFlow", "expression", "virtdataCall", "lvalue", 
			"inputType", "funcName", "outputType", "arg", "ref", "value", "stringValue", 
			"longValue", "doubleValue", "integerValue", "floatValue", "booleanValue", 
			"specend"
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

	@Override
	public String getGrammarFileName() { return "VirtData.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public VirtDataParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class VirtdataRecipeContext extends ParserRuleContext {
		public List<VirtdataFlowContext> virtdataFlow() {
			return getRuleContexts(VirtdataFlowContext.class);
		}
		public VirtdataFlowContext virtdataFlow(int i) {
			return getRuleContext(VirtdataFlowContext.class,i);
		}
		public TerminalNode EOF() { return getToken(VirtDataParser.EOF, 0); }
		public List<SpecendContext> specend() {
			return getRuleContexts(SpecendContext.class);
		}
		public SpecendContext specend(int i) {
			return getRuleContext(SpecendContext.class,i);
		}
		public VirtdataRecipeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_virtdataRecipe; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterVirtdataRecipe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitVirtdataRecipe(this);
		}
	}

	public final VirtdataRecipeContext virtdataRecipe() throws RecognitionException {
		VirtdataRecipeContext _localctx = new VirtdataRecipeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_virtdataRecipe);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			virtdataFlow();
			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5 || _la==NEWLINE) {
				{
				{
				setState(37);
				specend();
				setState(39);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMPOSE || _la==ID) {
					{
					setState(38);
					virtdataFlow();
					}
				}

				}
				}
				setState(45);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(46);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VirtdataFlowContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode COMPOSE() { return getToken(VirtDataParser.COMPOSE, 0); }
		public VirtdataFlowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_virtdataFlow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterVirtdataFlow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitVirtdataFlow(this);
		}
	}

	public final VirtdataFlowContext virtdataFlow() throws RecognitionException {
		VirtdataFlowContext _localctx = new VirtdataFlowContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_virtdataFlow);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMPOSE) {
				{
				setState(48);
				match(COMPOSE);
				}
			}

			setState(51);
			expression();
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(52);
				match(T__0);
				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(53);
					expression();
					}
				}

				}
				}
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public VirtdataCallContext virtdataCall() {
			return getRuleContext(VirtdataCallContext.class,0);
		}
		public LvalueContext lvalue() {
			return getRuleContext(LvalueContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(VirtDataParser.ASSIGN, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(61);
				lvalue();
				setState(62);
				match(ASSIGN);
				}
				break;
			}
			setState(66);
			virtdataCall();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VirtdataCallContext extends ParserRuleContext {
		public FuncNameContext funcName() {
			return getRuleContext(FuncNameContext.class,0);
		}
		public InputTypeContext inputType() {
			return getRuleContext(InputTypeContext.class,0);
		}
		public List<TerminalNode> TYPEARROW() { return getTokens(VirtDataParser.TYPEARROW); }
		public TerminalNode TYPEARROW(int i) {
			return getToken(VirtDataParser.TYPEARROW, i);
		}
		public OutputTypeContext outputType() {
			return getRuleContext(OutputTypeContext.class,0);
		}
		public List<ArgContext> arg() {
			return getRuleContexts(ArgContext.class);
		}
		public ArgContext arg(int i) {
			return getRuleContext(ArgContext.class,i);
		}
		public VirtdataCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_virtdataCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterVirtdataCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitVirtdataCall(this);
		}
	}

	public final VirtdataCallContext virtdataCall() throws RecognitionException {
		VirtdataCallContext _localctx = new VirtdataCallContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_virtdataCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(68);
				inputType();
				setState(69);
				match(TYPEARROW);
				}
				break;
			}
			{
			setState(73);
			funcName();
			setState(74);
			match(T__1);
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << LONG) | (1L << DOUBLE) | (1L << INTEGER) | (1L << FLOAT) | (1L << BOOLEAN) | (1L << SSTRING_LITERAL) | (1L << DSTRING_LITERAL) | (1L << ID))) != 0)) {
				{
				setState(75);
				arg();
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__2) {
					{
					{
					setState(76);
					match(T__2);
					setState(77);
					arg();
					}
					}
					setState(82);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(85);
			match(T__3);
			}
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TYPEARROW) {
				{
				setState(87);
				match(TYPEARROW);
				setState(88);
				outputType();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LvalueContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VirtDataParser.ID, 0); }
		public LvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterLvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitLvalue(this);
		}
	}

	public final LvalueContext lvalue() throws RecognitionException {
		LvalueContext _localctx = new LvalueContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_lvalue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InputTypeContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VirtDataParser.ID, 0); }
		public InputTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inputType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterInputType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitInputType(this);
		}
	}

	public final InputTypeContext inputType() throws RecognitionException {
		InputTypeContext _localctx = new InputTypeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_inputType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FuncNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VirtDataParser.ID, 0); }
		public FuncNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterFuncName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitFuncName(this);
		}
	}

	public final FuncNameContext funcName() throws RecognitionException {
		FuncNameContext _localctx = new FuncNameContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_funcName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OutputTypeContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VirtDataParser.ID, 0); }
		public OutputTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_outputType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterOutputType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitOutputType(this);
		}
	}

	public final OutputTypeContext outputType() throws RecognitionException {
		OutputTypeContext _localctx = new OutputTypeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_outputType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArgContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public VirtdataCallContext virtdataCall() {
			return getRuleContext(VirtdataCallContext.class,0);
		}
		public RefContext ref() {
			return getRuleContext(RefContext.class,0);
		}
		public ArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitArg(this);
		}
	}

	public final ArgContext arg() throws RecognitionException {
		ArgContext _localctx = new ArgContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_arg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LONG:
			case DOUBLE:
			case INTEGER:
			case FLOAT:
			case BOOLEAN:
			case SSTRING_LITERAL:
			case DSTRING_LITERAL:
				{
				setState(99);
				value();
				}
				break;
			case ID:
				{
				setState(100);
				virtdataCall();
				}
				break;
			case T__4:
				{
				setState(101);
				ref();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RefContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VirtDataParser.ID, 0); }
		public RefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitRef(this);
		}
	}

	public final RefContext ref() throws RecognitionException {
		RefContext _localctx = new RefContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_ref);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(104);
			match(T__4);
			setState(105);
			match(ID);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public FloatValueContext floatValue() {
			return getRuleContext(FloatValueContext.class,0);
		}
		public DoubleValueContext doubleValue() {
			return getRuleContext(DoubleValueContext.class,0);
		}
		public IntegerValueContext integerValue() {
			return getRuleContext(IntegerValueContext.class,0);
		}
		public LongValueContext longValue() {
			return getRuleContext(LongValueContext.class,0);
		}
		public StringValueContext stringValue() {
			return getRuleContext(StringValueContext.class,0);
		}
		public BooleanValueContext booleanValue() {
			return getRuleContext(BooleanValueContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitValue(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FLOAT:
				{
				setState(107);
				floatValue();
				}
				break;
			case DOUBLE:
				{
				setState(108);
				doubleValue();
				}
				break;
			case INTEGER:
				{
				setState(109);
				integerValue();
				}
				break;
			case LONG:
				{
				setState(110);
				longValue();
				}
				break;
			case SSTRING_LITERAL:
			case DSTRING_LITERAL:
				{
				setState(111);
				stringValue();
				}
				break;
			case BOOLEAN:
				{
				setState(112);
				booleanValue();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringValueContext extends ParserRuleContext {
		public TerminalNode SSTRING_LITERAL() { return getToken(VirtDataParser.SSTRING_LITERAL, 0); }
		public TerminalNode DSTRING_LITERAL() { return getToken(VirtDataParser.DSTRING_LITERAL, 0); }
		public StringValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterStringValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitStringValue(this);
		}
	}

	public final StringValueContext stringValue() throws RecognitionException {
		StringValueContext _localctx = new StringValueContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_stringValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			_la = _input.LA(1);
			if ( !(_la==SSTRING_LITERAL || _la==DSTRING_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LongValueContext extends ParserRuleContext {
		public TerminalNode LONG() { return getToken(VirtDataParser.LONG, 0); }
		public LongValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_longValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterLongValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitLongValue(this);
		}
	}

	public final LongValueContext longValue() throws RecognitionException {
		LongValueContext _localctx = new LongValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_longValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(LONG);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DoubleValueContext extends ParserRuleContext {
		public TerminalNode DOUBLE() { return getToken(VirtDataParser.DOUBLE, 0); }
		public DoubleValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_doubleValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterDoubleValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitDoubleValue(this);
		}
	}

	public final DoubleValueContext doubleValue() throws RecognitionException {
		DoubleValueContext _localctx = new DoubleValueContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_doubleValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(DOUBLE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegerValueContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(VirtDataParser.INTEGER, 0); }
		public IntegerValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterIntegerValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitIntegerValue(this);
		}
	}

	public final IntegerValueContext integerValue() throws RecognitionException {
		IntegerValueContext _localctx = new IntegerValueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_integerValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			match(INTEGER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FloatValueContext extends ParserRuleContext {
		public TerminalNode FLOAT() { return getToken(VirtDataParser.FLOAT, 0); }
		public FloatValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterFloatValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitFloatValue(this);
		}
	}

	public final FloatValueContext floatValue() throws RecognitionException {
		FloatValueContext _localctx = new FloatValueContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_floatValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			match(FLOAT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanValueContext extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(VirtDataParser.BOOLEAN, 0); }
		public BooleanValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterBooleanValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitBooleanValue(this);
		}
	}

	public final BooleanValueContext booleanValue() throws RecognitionException {
		BooleanValueContext _localctx = new BooleanValueContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_booleanValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			match(BOOLEAN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecendContext extends ParserRuleContext {
		public List<TerminalNode> NEWLINE() { return getTokens(VirtDataParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(VirtDataParser.NEWLINE, i);
		}
		public SpecendContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specend; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).enterSpecend(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VirtDataListener ) ((VirtDataListener)listener).exitSpecend(this);
		}
	}

	public final SpecendContext specend() throws RecognitionException {
		SpecendContext _localctx = new SpecendContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_specend);
		try {
			int _alt;
			setState(139);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(127);
				match(T__5);
				setState(129); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(128);
						match(NEWLINE);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(131); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
				match(T__5);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(135); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(134);
						match(NEWLINE);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(137); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\26\u0090\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\2\5\2*\n\2\7\2,\n\2\f\2\16\2/\13\2\3\2\3\2\3\3\5"+
		"\3\64\n\3\3\3\3\3\3\3\5\39\n\3\7\3;\n\3\f\3\16\3>\13\3\3\4\3\4\3\4\5\4"+
		"C\n\4\3\4\3\4\3\5\3\5\3\5\5\5J\n\5\3\5\3\5\3\5\3\5\3\5\7\5Q\n\5\f\5\16"+
		"\5T\13\5\5\5V\n\5\3\5\3\5\3\5\3\5\5\5\\\n\5\3\6\3\6\3\7\3\7\3\b\3\b\3"+
		"\t\3\t\3\n\3\n\3\n\5\ni\n\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\5\f"+
		"t\n\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3"+
		"\23\6\23\u0084\n\23\r\23\16\23\u0085\3\23\3\23\6\23\u008a\n\23\r\23\16"+
		"\23\u008b\5\23\u008e\n\23\3\23\2\2\24\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \"$\2\3\3\2\22\23\2\u0092\2&\3\2\2\2\4\63\3\2\2\2\6B\3\2\2\2\b"+
		"I\3\2\2\2\n]\3\2\2\2\f_\3\2\2\2\16a\3\2\2\2\20c\3\2\2\2\22h\3\2\2\2\24"+
		"j\3\2\2\2\26s\3\2\2\2\30u\3\2\2\2\32w\3\2\2\2\34y\3\2\2\2\36{\3\2\2\2"+
		" }\3\2\2\2\"\177\3\2\2\2$\u008d\3\2\2\2&-\5\4\3\2\')\5$\23\2(*\5\4\3\2"+
		")(\3\2\2\2)*\3\2\2\2*,\3\2\2\2+\'\3\2\2\2,/\3\2\2\2-+\3\2\2\2-.\3\2\2"+
		"\2.\60\3\2\2\2/-\3\2\2\2\60\61\7\2\2\3\61\3\3\2\2\2\62\64\7\17\2\2\63"+
		"\62\3\2\2\2\63\64\3\2\2\2\64\65\3\2\2\2\65<\5\6\4\2\668\7\3\2\2\679\5"+
		"\6\4\28\67\3\2\2\289\3\2\2\29;\3\2\2\2:\66\3\2\2\2;>\3\2\2\2<:\3\2\2\2"+
		"<=\3\2\2\2=\5\3\2\2\2><\3\2\2\2?@\5\n\6\2@A\7\21\2\2AC\3\2\2\2B?\3\2\2"+
		"\2BC\3\2\2\2CD\3\2\2\2DE\5\b\5\2E\7\3\2\2\2FG\5\f\7\2GH\7\20\2\2HJ\3\2"+
		"\2\2IF\3\2\2\2IJ\3\2\2\2JK\3\2\2\2KL\5\16\b\2LU\7\4\2\2MR\5\22\n\2NO\7"+
		"\5\2\2OQ\5\22\n\2PN\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2SV\3\2\2\2TR"+
		"\3\2\2\2UM\3\2\2\2UV\3\2\2\2VW\3\2\2\2WX\7\6\2\2X[\3\2\2\2YZ\7\20\2\2"+
		"Z\\\5\20\t\2[Y\3\2\2\2[\\\3\2\2\2\\\t\3\2\2\2]^\7\24\2\2^\13\3\2\2\2_"+
		"`\7\24\2\2`\r\3\2\2\2ab\7\24\2\2b\17\3\2\2\2cd\7\24\2\2d\21\3\2\2\2ei"+
		"\5\26\f\2fi\5\b\5\2gi\5\24\13\2he\3\2\2\2hf\3\2\2\2hg\3\2\2\2i\23\3\2"+
		"\2\2jk\7\7\2\2kl\7\24\2\2l\25\3\2\2\2mt\5 \21\2nt\5\34\17\2ot\5\36\20"+
		"\2pt\5\32\16\2qt\5\30\r\2rt\5\"\22\2sm\3\2\2\2sn\3\2\2\2so\3\2\2\2sp\3"+
		"\2\2\2sq\3\2\2\2sr\3\2\2\2t\27\3\2\2\2uv\t\2\2\2v\31\3\2\2\2wx\7\t\2\2"+
		"x\33\3\2\2\2yz\7\n\2\2z\35\3\2\2\2{|\7\13\2\2|\37\3\2\2\2}~\7\f\2\2~!"+
		"\3\2\2\2\177\u0080\7\r\2\2\u0080#\3\2\2\2\u0081\u0083\7\b\2\2\u0082\u0084"+
		"\7\16\2\2\u0083\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0083\3\2\2\2"+
		"\u0085\u0086\3\2\2\2\u0086\u008e\3\2\2\2\u0087\u008e\7\b\2\2\u0088\u008a"+
		"\7\16\2\2\u0089\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b\u0089\3\2\2\2"+
		"\u008b\u008c\3\2\2\2\u008c\u008e\3\2\2\2\u008d\u0081\3\2\2\2\u008d\u0087"+
		"\3\2\2\2\u008d\u0089\3\2\2\2\u008e%\3\2\2\2\21)-\638<BIRU[hs\u0085\u008b"+
		"\u008d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}