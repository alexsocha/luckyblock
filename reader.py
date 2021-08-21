from nbtlib import load

# filename = "luckycow.mcstructure"
filename = "bedrock/build/processedResources/generated-structures/mystructure:lucky_block_drop_1.1.mcstructure"
# filename = "simple.mcstructure"

#nbt_tag = load(filename, gzipped=False, byteorder="little")
#print(nbt_tag)

with open(filename) as f:
    array = [ord(b) for b in f.read()]
    print(array)

