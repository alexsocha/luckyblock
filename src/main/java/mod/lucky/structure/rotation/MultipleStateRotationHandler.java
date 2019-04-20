package mod.lucky.structure.rotation;

import net.minecraft.block.state.IBlockState;

public class MultipleStateRotationHandler extends StateRotationHandler {
    private StateRotationHandler rotationHandlers[];

    public MultipleStateRotationHandler(StateRotationHandler... rotationHandlers) {
        this.rotationHandlers = new StateRotationHandler[rotationHandlers.length];
        for (int i = 0; i < rotationHandlers.length; i++)
            this.rotationHandlers[i] = rotationHandlers[i];
    }

    @Override
    public IBlockState rotate(IBlockState state, int rotation) {
        for (int i = 0; i < this.rotationHandlers.length; i++) {
            IBlockState curState = this.rotationHandlers[i].rotate(state, rotation);
            if (curState != state) return curState;
        }
        return state;
    }
}
