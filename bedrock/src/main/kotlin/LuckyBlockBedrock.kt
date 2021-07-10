package mod.lucky.bedrock

import mod.lucky.common.attribute.*

@JsName("sayHello")
fun sayHello(loc: String) {
    if (isNumType(AttrType.STRING)) return

    console.log("Saying Hello from $loc")
}
