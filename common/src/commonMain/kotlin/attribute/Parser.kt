package mod.lucky.common.attribute

data class ParserContext(
    val templateVarSpecs: Map<String, TemplateVarSpec>,
    // compatibility
    val escapeTemplatesInKeys: Map<String, Boolean> = emptyMap(),
    val escapeTemplatesInType: AttrType = AttrType.LIST,
)

fun isDecimalStr(value: String): Boolean {
    return value.endsWith("f", ignoreCase = true) || value.endsWith("d", ignoreCase = true) || value.contains('.')
}

fun indexOfUnnested(value: String, startIndex: Int, searchChar: Char): Int? {
    var bracketTier = 0
    var inQuotes = false
    for (i in startIndex until value.length) {
        val charCanceled = i > 0 && value[i - 1] == '\\'
        if (!charCanceled) {
            if (value[i] == '"') {
                inQuotes = !inQuotes
            }
            if ((value[i] == '(' || value[i] == '[' || value[i] == '{') && !inQuotes) {
                bracketTier++
            }
            if ((value[i] == ')' || value[i] == ']' || value[i] == '}') && !inQuotes) {
                bracketTier--
            }
        }
        val isNested = bracketTier != 0 || inQuotes
        if (!isNested && value[i] == searchChar) return i
    }
    return null
}

fun splitBracketString(value: String, separator: Char): List<String> {
    val splitPoints = generateSequence(-1) {
        prev -> indexOfUnnested(value, prev + 1, separator)
    }.toList() + listOf(value.length)

    return (0 until splitPoints.size - 1).map {
        value.substring(splitPoints[it] + 1, splitPoints[it + 1]).trim()
    }.toList()
}

fun splitLines(lines: List<String>): List<String> {
    val newLines = ArrayList<String>()
    var combinedLine = ""
    var prevLine: String? = null
    for (lineInit in lines) {
        // remove whitespace
        val line = lineInit.trim()

        // ignore blank lines and comments comments
        if (line == "" || line.startsWith("/")) continue

        // join lines ending with '\', brackets, commas, semicolons,
        // or starting with closed brackets
        val isContinuation = (prevLine != null && (prevLine.last() in listOf('\\', '(', '[', ',', ';') || line.first() in listOf(')', ']')))

        val lineContents = if (line.endsWith("\\")) line.substring(0 until line.length - 1).trim() else line
        if (!isContinuation && combinedLine != "") {
            newLines.add(combinedLine)
            combinedLine = lineContents
        } else {
            combinedLine += lineContents
        }
        prevLine = line
    }
    if (combinedLine != "") newLines.add(combinedLine)

    return newLines
}

fun parseSectionedLines(lines: List<String>): Map<String, List<String>> {
    val parsedLines = splitLines(lines)
    val sections = HashMap<String, ArrayList<String>>()
    var curSection = "default"
    for (line in parsedLines) {
        if (line.startsWith('>')) {
            curSection = line.substring(1).trim()
        } else {
            if (curSection !in sections) sections[curSection] = ArrayList()
            sections[curSection]!!.add(line)
        }
    }
    return sections
}

fun parseExpr(exprStr: String): Double? {
    var newExpr = exprStr.trim(' ')
    var prefixSign = ""
    if (newExpr.startsWith("-") || newExpr.startsWith("+")) {
        prefixSign = newExpr.substring(0, 1)
        newExpr = newExpr.substring(1)
    }
    val operator: Char? = when {
        newExpr.contains("+") -> '+'
        newExpr.contains("-") -> '-'
        newExpr.contains("*") -> '*'
        newExpr.contains("/") -> '/'
        else -> null
    }

    try {
        if (operator != null) {
            val splitString = splitBracketString(newExpr, operator)
            return if (splitString.size >= 2) {
                val num1 = (prefixSign + splitString[0]).toDouble()
                val num2 = (splitString[1]).toDouble()
                when (operator) {
                    '+' -> num1 + num2
                    '-' -> num1 - num2
                    '*' -> num1 * num2
                    '/' -> num1 / num2
                    else -> null
                }
            } else null
        }
        return (prefixSign + newExpr).toDouble()
    } catch (e: NumberFormatException) {
        return null
    }
}

fun escapeTemplates(value: String): String {
    // auto cancel template variables
    var s = value
    s = s.replace("#", "'#'")
    s = s.replace("''#''", "'#'")
    s = s.replace("['#']", "#")
    return s
}

fun unescapeSpecialChars(value: String): String {
    var s = value
    s = s.replace("\\\\t", "\t")
    s = s.replace("\\\\b", "\b")
    s = s.replace("\\\\n", "\n")
    s = s.replace("\\\\r", "\r")
    return s
}

fun unescapeAll(value: String): String {
    var s = value
    s = unescapeSpecialChars(s)
    s = s.replace("'#'", "#")
    s = s.replace("'@'", "@")
    s = s.replace("'$'", "'\u00A7'")
    s = s.replace("$", "\u00A7")
    s = s.replace("'\u00A7'", "$")
    return s
}

fun parseNum(numStr: String): ValueAttr? {
    val (newNumStr, numType) = when {
        numStr.endsWith("d", ignoreCase = true) -> Pair(numStr.dropLast(1), AttrType.DOUBLE)
        numStr.endsWith("f", ignoreCase = true) -> Pair(numStr.dropLast(1), AttrType.FLOAT)
        numStr.endsWith("s", ignoreCase = true) -> Pair(numStr.dropLast(1), AttrType.SHORT)
        numStr.endsWith("b", ignoreCase = true) -> Pair(numStr.dropLast(1), AttrType.BYTE)
        numStr.endsWith("l", ignoreCase = true) -> Pair(numStr.dropLast(1), AttrType.LONG)
        isDecimalStr(numStr) -> Pair(numStr, AttrType.DOUBLE)
        else -> Pair(numStr, AttrType.INT)
    }
    val parsedNum = parseExpr(newNumStr)
    return parsedNum?.let { ValueAttr(numType, castNum(numType, it)) }
}

fun containsTemplateVars(value: String, context: ParserContext): Boolean {
    for (k in context.templateVarSpecs.keys) if (value.contains("#$k")) return true
    return false
}

private fun parseTemplateVar(value: String, context: ParserContext): TemplateVar {
    val bracketIndex = value.indexOf('(')
    val hasArgs = value.endsWith(')')
    val name = if (hasArgs) value.slice(1 until bracketIndex) else value.slice(1 until value.length)
    val spec = context.templateVarSpecs[name]
        ?: throw ParserError("Unknown template variable '$name'")

    if (hasArgs) {
        val argsStr = value.slice((bracketIndex + 1)..(value.length - 2))
        val argStrList = splitBracketString(argsStr, ',')

        if (!spec.argRange.contains(argStrList.size)) {
            throw ParserError("Wrong number of arguments for template variable '$name', expected ${spec.argRange} matching '${spec.args.joinToString(", ")}'")
        }

        val parsedArgs = argStrList.mapIndexed { i, v ->
            val argSpec = if (i in spec.args.indices) spec.args[i].second else spec.args[0].second
            parseAttr(v, argSpec, context)
        }
        return TemplateVar(name, ListAttr(parsedArgs))
    }
    return TemplateVar(name, ListAttr())
}

private fun parseTemplateVars(value: String, context: ParserContext): List<Pair<String?, TemplateVar?>> {
    if (!value.contains('#')) return listOf(Pair(value, null))

    val templateList = ArrayList<Pair<String?, TemplateVar?>>()
    val points = ArrayList<Int>()
    val sortedTemplateKeys = context.templateVarSpecs.keys.sortedDescending()

    var templateEndpoint = -1
    while (true) {
        var minPoint = -1
        var templateName: String? = null
        for (k in sortedTemplateKeys) {
            val point = value.indexOf("#$k", templateEndpoint + 1)
            if (point != -1 && (minPoint == -1 || point < minPoint)) {
                minPoint = point
                templateName = k
            }
        }

        if (templateName == null) {
            val remainingStr = value.substring(templateEndpoint + 1 until value.length)
            if (remainingStr.isNotEmpty()) templateList.add(Pair(unescapeAll(remainingStr), null))
            break
        }

        points.add(minPoint)

        val strPartBefore = value.substring(templateEndpoint + 1, points.last())
        if (strPartBefore.isNotEmpty()) templateList.add(Pair(unescapeAll(strPartBefore), null))

        val hasArgs = value.getOrNull(minPoint + templateName.length + 1) == '('
        templateEndpoint =
            if (hasArgs) indexOfUnnested(value, minPoint + templateName.length + 1, ')') ?: value.length
            else minPoint + templateName.length

        val templateVar = parseTemplateVar(value.substring(minPoint..templateEndpoint), context)
        templateList.add(Pair(null, templateVar))
    }
    return templateList
}

class ParserError(message: String) : Exception(message)

@Throws(ParserError::class)
fun parseAttr(value: String, spec: AttrSpec?, context: ParserContext, parentKey: String? = null, parentType: AttrType? = null): Attr {
    val isTemplate = value.startsWith("#") || value.startsWith("[#]") // compatibility

    // parse list
    val shouldParseList = value.startsWith("[")
        || (spec is ListSpec && !isTemplate)
        || (spec is ListSpec && value.startsWith('(')) // handle tuples
    if (shouldParseList) {
        val innerStr = if (value.startsWith("[") || value.startsWith("("))
            value.substring(1 until value.length - 1) else value
        val contents = if (innerStr == "") emptyList() else splitBracketString(innerStr, ',')
        val children = ArrayList<Attr>()
        contents.forEachIndexed { i, v ->
            val childSpec = if (spec is ListSpec) (if (i in spec.elements.indices) spec.elements[i] else spec.elements[0]) else null
            val childAttr = parseAttr(v, childSpec, context, parentKey = parentKey, parentType = AttrType.LIST)
            children.add(childAttr)
        }
        return ListAttr(children)
    }

    // parse dict
    val shouldParseDict = value.startsWith("(") || (spec is DictSpec && !isTemplate)
    if (shouldParseDict) {
        val innerStr = if (value.startsWith("(")) value.substring(1 until value.length - 1) else value
        val contents = if (innerStr == "") emptyList() else splitBracketString(innerStr, ',')

        val lowerKeyMap = if (spec !is DictSpec) emptyMap() else spec.children
            .mapValues { (k, _) -> k }
            .mapKeys { (k, _) -> k.toLowerCase() }

        val children = HashMap<String, Attr>()
        contents.forEach {
            if ("=" !in it) throw ParserError("Invalid dictionary entry '$it' for attribute '$value'")
            val childKey = it.substring(0, it.indexOf("=")).trim()
            val lowerChildKey = childKey.toLowerCase()
            val newChildKey = lowerKeyMap[lowerChildKey] ?: childKey
            val v = it.substring(it.indexOf("=") + 1).trim()

            val childSpec = if (spec is DictSpec) spec.children[newChildKey] else null
            children[newChildKey] = parseAttr(v, childSpec, context, parentKey = newChildKey, parentType = AttrType.DICT)
        }
        return DictAttr(children)
    }


    val specType = (spec as? ValueSpec)?.type

    val isInQuotes = value.startsWith("\"") && value.endsWith("\"")
    val shouldEscapeTemplates = parentKey != null && context.escapeTemplatesInKeys[parentKey] == true && context.escapeTemplatesInType == parentType
    val unquotedStr = (if (isInQuotes) value.substring(1 until value.length - 1).replace("\\\"", "\"") else value).let {
        // compatibility
        if (shouldEscapeTemplates) escapeTemplates(it) else it
    }

    // parse templates
    if (containsTemplateVars(unquotedStr, context)) {
        val templateVars = parseTemplateVars(unquotedStr, context)
        return TemplateAttr(spec, templateVars)
    }

    // compound types should have been parsed by now
    if (spec is DictSpec || spec is ListSpec)
        throw ParserError("Can't parse '$value' as a list or dict")

    // parse string
    if ((specType == AttrType.STRING) || (specType == null && isInQuotes)) {
        return ValueAttr(AttrType.STRING, unescapeAll(unquotedStr))
    }

    // parse boolean
    if (specType == AttrType.BOOLEAN || specType == null) {
        if (value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)) {
            return ValueAttr(AttrType.BOOLEAN, value.equals("true", ignoreCase = true))
        }
    }

    // parse int/byte byte array
    val strArray = splitBracketString(value, ':')
    val arrayChildSpec = when (specType) {
        AttrType.BYTE_ARRAY -> ValueSpec(AttrType.BYTE)
        else -> ValueSpec(AttrType.INT)
    }
    val numArray = if (strArray.size > 1) try {
        strArray.map {
            val childAttr = parseAttr(it, arrayChildSpec, context, parentKey = parentKey, parentType = arrayChildSpec.type)
            (childAttr as? ValueAttr)?.value as Number
        }
    } catch(e: ParserError) { null } else null

    if (specType == AttrType.BYTE_ARRAY || specType == AttrType.INT_ARRAY || (specType == null && numArray != null)) {
        if (numArray == null) throw ParserError("Can't parse '$value' as a typed array")

        if (strArray.any { it.endsWith("b", ignoreCase = true) }) {
            val byteArray = ByteArray(strArray.size)
            numArray.forEachIndexed { i, v -> byteArray[i] = v.toInt().toByte() }
            return ValueAttr(AttrType.BYTE_ARRAY, byteArray)
        }

        val intArray = IntArray(strArray.size)
        numArray.forEachIndexed { i, v -> intArray[i] = v.toInt() }
        return ValueAttr(AttrType.INT_ARRAY, intArray)
    }

    // parse numbers
    val parsedNum = parseNum(value)
    if ((specType != null && isNumType(specType)) || parsedNum != null) {
        if (parsedNum == null) throw ParserError("Can't parse '$value' as a number")

        val type = specType ?: parsedNum.type
        return ValueAttr(type, castNum(type, parsedNum.value as Number))
    }

    // parse string without quotes
    if (specType == null) return ValueAttr(AttrType.STRING, unescapeAll(unquotedStr))
    else throw ParserError("Can't parse '$value' as type '{$spec.type}'")
}

fun parseSingleKey(value: String, key: String, spec: AttrSpec?, context: ParserContext): Attr? {
    val innerStr = if (value.startsWith("(")) value.substring(1 until value.length - 1) else value
    val contents = splitBracketString(innerStr, ',')
    contents.forEach {
        if ("=" !in it) {
            throw ParserError("Invalid dictionary entry '$it' for attribute '$value'")
        }
        val childKey = it.substring(0, it.indexOf("=")).trim()
        val v = it.substring(it.indexOf("=") + 1).trim()

        if (childKey.equals(key, ignoreCase = true)) return parseAttr(v, spec, context, parentKey = key, parentType=AttrType.DICT)
    }
    return null
}