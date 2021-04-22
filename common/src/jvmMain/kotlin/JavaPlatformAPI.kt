package mod.lucky.java

import mod.lucky.common.PlatformAPI
import mod.lucky.common.attribute.EvalError
import javax.script.ScriptEngineManager
import javax.script.ScriptException

object JavaPlatformAPI : PlatformAPI {
    init {
        registerJavaTemplateVars()
    }

    override fun evalJS(script: String): Any {
        val scriptEngine = ScriptEngineManager(null).getEngineByName("JavaScript")
        return try {
            scriptEngine.eval(script)
        } catch (e: ScriptException) {
            throw EvalError("Error running script '$script': $e")
        }
    }
}