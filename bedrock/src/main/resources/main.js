import { mod } from '../../../build/distributions/bedrock.js'
import { serverSystem } from '../../../build/processedResources/generated-config.js'

const lucky = mod.lucky.bedrock;

// the server runs this as soon as the scripts are all fully loaded
serverSystem.initialize = function() {
    // turn on logging of information, warnings, and errors
    const scriptLoggerConfig = this.createEventData("minecraft:script_logger_config");
    scriptLoggerConfig.data.log_errors = true;
    scriptLoggerConfig.data.log_information = true;
    scriptLoggerConfig.data.log_warnings = true;
    this.broadcastEvent("minecraft:script_logger_config", scriptLoggerConfig);

    this.listenForEvent("minecraft:player_destroyed_block", () => {this.log("_received event")});
    lucky.initServer(server, this);
};

let globals = {
  didBreak: false,
};

serverSystem.update = function() {
    if (!globals.didBreak) return;
    globals.didBreak = false;

}

// This is just a helper function that simplifies logging data to the console.
serverSystem.log = function(...items) {
    const chatEvent = this.createEventData("minecraft:display_chat_event");
    chatEvent.data.message = items.map(obj => JSON.stringify(obj)).join(", ");
    this.broadcastEvent("minecraft:display_chat_event", chatEvent);
}
