// Generated from VirtData.g4 by ANTLR 4.9.2
package io.nosqlbench.virtdata.lang.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link VirtDataParser}.
 */
public interface VirtDataListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#virtdataRecipe}.
	 * @param ctx the parse tree
	 */
	void enterVirtdataRecipe(VirtDataParser.VirtdataRecipeContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#virtdataRecipe}.
	 * @param ctx the parse tree
	 */
	void exitVirtdataRecipe(VirtDataParser.VirtdataRecipeContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#virtdataFlow}.
	 * @param ctx the parse tree
	 */
	void enterVirtdataFlow(VirtDataParser.VirtdataFlowContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#virtdataFlow}.
	 * @param ctx the parse tree
	 */
	void exitVirtdataFlow(VirtDataParser.VirtdataFlowContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(VirtDataParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(VirtDataParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#virtdataCall}.
	 * @param ctx the parse tree
	 */
	void enterVirtdataCall(VirtDataParser.VirtdataCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#virtdataCall}.
	 * @param ctx the parse tree
	 */
	void exitVirtdataCall(VirtDataParser.VirtdataCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void enterLvalue(VirtDataParser.LvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#lvalue}.
	 * @param ctx the parse tree
	 */
	void exitLvalue(VirtDataParser.LvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#inputType}.
	 * @param ctx the parse tree
	 */
	void enterInputType(VirtDataParser.InputTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#inputType}.
	 * @param ctx the parse tree
	 */
	void exitInputType(VirtDataParser.InputTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#funcName}.
	 * @param ctx the parse tree
	 */
	void enterFuncName(VirtDataParser.FuncNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#funcName}.
	 * @param ctx the parse tree
	 */
	void exitFuncName(VirtDataParser.FuncNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#outputType}.
	 * @param ctx the parse tree
	 */
	void enterOutputType(VirtDataParser.OutputTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#outputType}.
	 * @param ctx the parse tree
	 */
	void exitOutputType(VirtDataParser.OutputTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#arg}.
	 * @param ctx the parse tree
	 */
	void enterArg(VirtDataParser.ArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#arg}.
	 * @param ctx the parse tree
	 */
	void exitArg(VirtDataParser.ArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#ref}.
	 * @param ctx the parse tree
	 */
	void enterRef(VirtDataParser.RefContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#ref}.
	 * @param ctx the parse tree
	 */
	void exitRef(VirtDataParser.RefContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(VirtDataParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(VirtDataParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#stringValue}.
	 * @param ctx the parse tree
	 */
	void enterStringValue(VirtDataParser.StringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#stringValue}.
	 * @param ctx the parse tree
	 */
	void exitStringValue(VirtDataParser.StringValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#longValue}.
	 * @param ctx the parse tree
	 */
	void enterLongValue(VirtDataParser.LongValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#longValue}.
	 * @param ctx the parse tree
	 */
	void exitLongValue(VirtDataParser.LongValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#doubleValue}.
	 * @param ctx the parse tree
	 */
	void enterDoubleValue(VirtDataParser.DoubleValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#doubleValue}.
	 * @param ctx the parse tree
	 */
	void exitDoubleValue(VirtDataParser.DoubleValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#integerValue}.
	 * @param ctx the parse tree
	 */
	void enterIntegerValue(VirtDataParser.IntegerValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#integerValue}.
	 * @param ctx the parse tree
	 */
	void exitIntegerValue(VirtDataParser.IntegerValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#floatValue}.
	 * @param ctx the parse tree
	 */
	void enterFloatValue(VirtDataParser.FloatValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#floatValue}.
	 * @param ctx the parse tree
	 */
	void exitFloatValue(VirtDataParser.FloatValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#booleanValue}.
	 * @param ctx the parse tree
	 */
	void enterBooleanValue(VirtDataParser.BooleanValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#booleanValue}.
	 * @param ctx the parse tree
	 */
	void exitBooleanValue(VirtDataParser.BooleanValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link VirtDataParser#specend}.
	 * @param ctx the parse tree
	 */
	void enterSpecend(VirtDataParser.SpecendContext ctx);
	/**
	 * Exit a parse tree produced by {@link VirtDataParser#specend}.
	 * @param ctx the parse tree
	 */
	void exitSpecend(VirtDataParser.SpecendContext ctx);
}