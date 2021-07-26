const systemServer = server.registerSystem(0, 0);

const blockDrops = [
    "custom drop1",
    "custom drop2",
]

systemServer.registerEventData("lucky:lucky_block_custom_config", {
    "drops": blockDrops,
    "luck": 0,
})