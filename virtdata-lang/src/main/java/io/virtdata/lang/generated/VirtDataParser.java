// Generated from VirtData.g4 by ANTLR 4.7.1
package io.virtdata.lang.generated;
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
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, LONG=7, DOUBLE=8, INTEGER=9, 
		FLOAT=10, NEWLINE=11, COMPOSE=12, TYPEARROW=13, ASSIGN=14, SSTRING_LITERAL=15, 
		DSTRING_LITERAL=16, ID=17, IDPART=18, WS=19;
	public static final int
		RULE_virtdataRecipe = 0, RULE_virtdataFlow = 1, RULE_expression = 2, RULE_virtdataCall = 3, 
		RULE_lvalue = 4, RULE_inputType = 5, RULE_funcName = 6, RULE_outputType = 7, 
		RULE_arg = 8, RULE_ref = 9, RULE_value = 10, RULE_stringValue = 11, RULE_longValue = 12, 
		RULE_doubleValue = 13, RULE_integerValue = 14, RULE_floatValue = 15, RULE_specend = 16;
	public static final String[] ruleNames = {
		"virtdataRecipe", "virtdataFlow", "expression", "virtdataCall", "lvalue", 
		"inputType", "funcName", "outputType", "arg", "ref", "value", "stringValue", 
		"longValue", "doubleValue", "integerValue", "floatValue", "specend"
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
			setState(34);
			virtdataFlow();
			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5 || _la==NEWLINE) {
				{
				{
				setState(35);
				specend();
				setState(37);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMPOSE || _la==ID) {
					{
					setState(36);
					virtdataFlow();
					}
				}

				}
				}
				setState(43);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(44);
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
			setState(47);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMPOSE) {
				{
				setState(46);
				match(COMPOSE);
				}
			}

			setState(49);
			expression();
			setState(56);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(50);
				match(T__0);
				setState(52);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(51);
					expression();
					}
				}

				}
				}
				setState(58);
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
			setState(62);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(59);
				lvalue();
				setState(60);
				match(ASSIGN);
				}
				break;
			}
			setState(64);
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
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(66);
				inputType();
				setState(67);
				match(TYPEARROW);
				}
				break;
			}
			{
			setState(71);
			funcName();
			setState(72);
			match(T__1);
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << LONG) | (1L << DOUBLE) | (1L << INTEGER) | (1L << FLOAT) | (1L << SSTRING_LITERAL) | (1L << DSTRING_LITERAL) | (1L << ID))) != 0)) {
				{
				setState(73);
				arg();
				setState(78);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__2) {
					{
					{
					setState(74);
					match(T__2);
					setState(75);
					arg();
					}
					}
					setState(80);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(83);
			match(T__3);
			}
			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TYPEARROW) {
				{
				setState(85);
				match(TYPEARROW);
				setState(86);
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
			setState(89);
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
			setState(100);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LONG:
			case DOUBLE:
			case INTEGER:
			case FLOAT:
			case SSTRING_LITERAL:
			case DSTRING_LITERAL:
				{
				setState(97);
				value();
				}
				break;
			case ID:
				{
				setState(98);
				virtdataCall();
				}
				break;
			case T__4:
				{
				setState(99);
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
			setState(102);
			match(T__4);
			setState(103);
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
			setState(110);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FLOAT:
				{
				setState(105);
				floatValue();
				}
				break;
			case DOUBLE:
				{
				setState(106);
				doubleValue();
				}
				break;
			case INTEGER:
				{
				setState(107);
				integerValue();
				}
				break;
			case LONG:
				{
				setState(108);
				longValue();
				}
				break;
			case SSTRING_LITERAL:
			case DSTRING_LITERAL:
				{
				setState(109);
				stringValue();
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
			setState(112);
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
			setState(114);
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
			setState(116);
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
			setState(118);
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
			setState(120);
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
		enterRule(_localctx, 32, RULE_specend);
		try {
			int _alt;
			setState(134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(122);
				match(T__5);
				setState(124); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(123);
						match(NEWLINE);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(126); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(128);
				match(T__5);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(130); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(129);
						match(NEWLINE);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(132); 
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\25\u008b\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\2\5\2(\n\2\7\2*\n\2\f\2\16\2-\13\2\3\2\3\2\3\3\5\3\62\n\3\3"+
		"\3\3\3\3\3\5\3\67\n\3\7\39\n\3\f\3\16\3<\13\3\3\4\3\4\3\4\5\4A\n\4\3\4"+
		"\3\4\3\5\3\5\3\5\5\5H\n\5\3\5\3\5\3\5\3\5\3\5\7\5O\n\5\f\5\16\5R\13\5"+
		"\5\5T\n\5\3\5\3\5\3\5\3\5\5\5Z\n\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n"+
		"\3\n\3\n\5\ng\n\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\5\fq\n\f\3\r\3\r"+
		"\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\6\22\177\n\22\r\22"+
		"\16\22\u0080\3\22\3\22\6\22\u0085\n\22\r\22\16\22\u0086\5\22\u0089\n\22"+
		"\3\22\2\2\23\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"\2\3\3\2\21\22\2"+
		"\u008d\2$\3\2\2\2\4\61\3\2\2\2\6@\3\2\2\2\bG\3\2\2\2\n[\3\2\2\2\f]\3\2"+
		"\2\2\16_\3\2\2\2\20a\3\2\2\2\22f\3\2\2\2\24h\3\2\2\2\26p\3\2\2\2\30r\3"+
		"\2\2\2\32t\3\2\2\2\34v\3\2\2\2\36x\3\2\2\2 z\3\2\2\2\"\u0088\3\2\2\2$"+
		"+\5\4\3\2%\'\5\"\22\2&(\5\4\3\2\'&\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)%\3\2"+
		"\2\2*-\3\2\2\2+)\3\2\2\2+,\3\2\2\2,.\3\2\2\2-+\3\2\2\2./\7\2\2\3/\3\3"+
		"\2\2\2\60\62\7\16\2\2\61\60\3\2\2\2\61\62\3\2\2\2\62\63\3\2\2\2\63:\5"+
		"\6\4\2\64\66\7\3\2\2\65\67\5\6\4\2\66\65\3\2\2\2\66\67\3\2\2\2\679\3\2"+
		"\2\28\64\3\2\2\29<\3\2\2\2:8\3\2\2\2:;\3\2\2\2;\5\3\2\2\2<:\3\2\2\2=>"+
		"\5\n\6\2>?\7\20\2\2?A\3\2\2\2@=\3\2\2\2@A\3\2\2\2AB\3\2\2\2BC\5\b\5\2"+
		"C\7\3\2\2\2DE\5\f\7\2EF\7\17\2\2FH\3\2\2\2GD\3\2\2\2GH\3\2\2\2HI\3\2\2"+
		"\2IJ\5\16\b\2JS\7\4\2\2KP\5\22\n\2LM\7\5\2\2MO\5\22\n\2NL\3\2\2\2OR\3"+
		"\2\2\2PN\3\2\2\2PQ\3\2\2\2QT\3\2\2\2RP\3\2\2\2SK\3\2\2\2ST\3\2\2\2TU\3"+
		"\2\2\2UV\7\6\2\2VY\3\2\2\2WX\7\17\2\2XZ\5\20\t\2YW\3\2\2\2YZ\3\2\2\2Z"+
		"\t\3\2\2\2[\\\7\23\2\2\\\13\3\2\2\2]^\7\23\2\2^\r\3\2\2\2_`\7\23\2\2`"+
		"\17\3\2\2\2ab\7\23\2\2b\21\3\2\2\2cg\5\26\f\2dg\5\b\5\2eg\5\24\13\2fc"+
		"\3\2\2\2fd\3\2\2\2fe\3\2\2\2g\23\3\2\2\2hi\7\7\2\2ij\7\23\2\2j\25\3\2"+
		"\2\2kq\5 \21\2lq\5\34\17\2mq\5\36\20\2nq\5\32\16\2oq\5\30\r\2pk\3\2\2"+
		"\2pl\3\2\2\2pm\3\2\2\2pn\3\2\2\2po\3\2\2\2q\27\3\2\2\2rs\t\2\2\2s\31\3"+
		"\2\2\2tu\7\t\2\2u\33\3\2\2\2vw\7\n\2\2w\35\3\2\2\2xy\7\13\2\2y\37\3\2"+
		"\2\2z{\7\f\2\2{!\3\2\2\2|~\7\b\2\2}\177\7\r\2\2~}\3\2\2\2\177\u0080\3"+
		"\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0089\3\2\2\2\u0082\u0089"+
		"\7\b\2\2\u0083\u0085\7\r\2\2\u0084\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086"+
		"\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089\3\2\2\2\u0088|\3\2\2\2"+
		"\u0088\u0082\3\2\2\2\u0088\u0084\3\2\2\2\u0089#\3\2\2\2\21\'+\61\66:@"+
		"GPSYfp\u0080\u0086\u0088";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}