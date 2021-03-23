package mod.lucky.common.attribute

data class EvalContext(
    val templateVarFns: Map<String, TemplateVarFn>,
    val templateContext: Any?,
)

class EvalError(message: String) : Exception(message)

fun evalTemplateVar(templateVar: TemplateVar, context: EvalContext): Attr {
    val fn = context.templateVarFns[templateVar.name]
        ?: throw EvalError("Missing eval function for template variable '${templateVar.name}'")

    return fn(templateVar, context.templateContext)
}

fun evalTemplateAttr(templateAttr: TemplateAttr, context: EvalContext): Attr {
    val parts = templateAttr.templateVars.map { (s, t) ->
        if (s != null) ValueAttr(AttrType.STRING, s)
        else {
            val args = evalAttr(t!!.args, context) as ListAttr
            evalTemplateVar(t.copy(args=args), context)
        }
    }

    if (parts.size == 1) {
        if (templateAttr.spec == null) return parts[0]
        return castAttr(templateAttr.spec, parts[0])
            ?: throw EvalError("Can't cast '${parts[0]}' to type '${templateAttr.spec}'")
    }

    // if there are multiple templates, e.g. #posX + #posY, join the result to a string
    val joinedStr = parts.joinToString("") { attrToString(it) }

    // only value attributes can be combined
    val specType = (templateAttr.spec as? ValueSpec)?.type

    // evaluate as string
    if (specType == AttrType.STRING) return ValueAttr(AttrType.STRING, joinedStr)

    // evaluate as number
    val numAttr = parseNum(joinedStr)
    if ((specType != null && isNumType(specType)) || (specType == null && numAttr != null)) {
        if (numAttr == null) throw EvalError("Expected template '$joinedStr' to evaluate to a number")
        if (specType != null) return castAttr(templateAttr.spec, numAttr)!!
        return numAttr
    }

    // evaluate as string by default
    if (templateAttr.spec != null)
        throw EvalError("Template '$joinedStr' can't be evaluated to type '${templateAttr.spec}'")

    return ValueAttr(AttrType.STRING, joinedStr)
}

fun evalAttr(attr: Attr, context: EvalContext): Attr {
    if (!needsEval(attr)) return attr

    if (attr is DictAttr) return DictAttr(attr.children.mapValues { (_, v) -> evalAttr(v, context) })
    if (attr is ListAttr) return ListAttr(attr.children.map { evalAttr(it, context) })
    if (attr is TemplateAttr) return evalTemplateAttr(attr, context)
    return attr
}

fun parseEvalAttr(valueStr: String, spec: AttrSpec, parserContext: ParserContext, evalContext: EvalContext): Attr {
    return evalAttr(parseAttr(valueStr, spec, parserContext), evalContext)
}

fun parseEvalValue(valueStr: String, type: AttrType, parserContext: ParserContext, evalContext: EvalContext): Any {
    return (evalAttr(parseAttr(valueStr, ValueSpec(type), parserContext), evalContext) as ValueAttr).value
}