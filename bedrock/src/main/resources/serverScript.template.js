const serverSystem = server.registerSystem(0, 0);

serverSystem.registerEventData("lucky:lucky_block_config", {
    "drops": $drops,
    "structures": $structures,
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
