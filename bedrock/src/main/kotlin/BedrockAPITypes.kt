package mod.lucky.bedrock

external class MCBlockPos {
    val x: Int
    val y: Int
    val z: Int
}

external class MCVecPos {
    var x: Double
    var y: Double
    var z: Double
}

external class MCBlock {}
external class MCEntity {}

external class MCQuery {
    val query_id: Int
}


external class MCTickingArea {}

external class MCTickWorldComponent {
    val ticking_area: MCTickingArea
}
typealias MCWorld = MCTickWorldComponent

external class MCPlayer {
}

external class MCComponent<T> {
    var data: T
}

external class MCEvent<T> {
    val data: T
}

external class MCPlayerDestroyedBlockEvent {
    val block_identifier: String
    val block_position: MCBlockPos
    val player: MCPlayer
}

external class MCServer {
    val level: Any
}

external class MCServerSystem {
    fun log(vararg args: Any?)
    fun registerEventData(name: String, eventData: Any)
    fun registerQuery(componentName: String, field1: String, field2: String, field3: String): MCQuery
    fun addFilterToQuery(query: MCQuery, componentName: String)
    fun getEntitiesFromQuery(query: MCQuery, field1Min: Int, field1Max: Int, field2Min: Int, field2Max: Int, field3Min: Int, field3Max: Int): Array<MCEntity>
    fun destroyEntity(entity: MCEntity)
    fun <T> createEventData(name: String): MCEvent<T>
    fun broadcastEvent(name: String, eventData: Any)
    fun createEntity(entityType: String, entityId: String): MCEntity
    fun <T> listenForEvent(name: String, fn: (eventData: MCEvent<T>) -> Unit)
    fun registerComponent(name: String, dataSpec: Any)
    fun <T> getComponent(obj: Any, componentName: String): MCComponent<T>?
    fun hasComponent(obj: Any, componentName: String): Boolean
    fun getLevel(): Any
    fun <T> createComponent(obj: Any, componentName: String): MCComponent<T>
    fun getBlock(tickingArea: MCTickingArea, blockPos: MCBlockPos): MCBlock
    fun <T> applyComponentChanges(obj: Any, component: MCComponent<T>)
}
