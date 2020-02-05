package mod.lucky.util;

import net.minecraft.nbt.*;

public class ObfHelper {
    public static StringNBT createStringNBT(String v) {
        return StringNBT.func_229705_a_(v);
    }
    public static IntNBT createIntNBT(int v) {
        return IntNBT.func_229692_a_(v);
    }
    public static FloatNBT createFloatNBT(float v) {
        return FloatNBT.func_229689_a_(v);
    }
    public static DoubleNBT createDoubleNBT(double v) {
        return DoubleNBT.func_229684_a_(v);
    }
    public static ByteNBT createByteNBT(byte v) {
        return ByteNBT.func_229671_a_(v);
    }
    public static ShortNBT createShortNBT(short v) {
        return ShortNBT.func_229701_a_(v);
    }
}
