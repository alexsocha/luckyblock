const serverSystem = server.registerSystem(0, 0);

serverSystem.registerEventData("lucky:${blockId}_config", {
    "drops": ${drops},
    "dropStructures": ${dropStructures},
    "luck": 0,
    "variants": {
        "lucky:unlucky_block": {
            "luck": -50,
        },
        "lucky:very_lucky_block": {
            "luck": 50,
        }
    }
});

module.exports = {
    "serverSystem": serverSystem
};
