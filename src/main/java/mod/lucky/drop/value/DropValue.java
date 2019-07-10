package mod.lucky.drop.value;

import mod.lucky.Lucky;
import mod.lucky.drop.func.DropProcessData;
import net.minecraft.nbt.CompoundNBT;

public class DropValue {
    private String rawValue = null;
    private Object value = null;
    private Class valueType;
    private boolean needsInitialize = false;

    public DropValue(Object value) {
        this.value = value;
    }

    public DropValue(String rawValue, Class valueType) {
        this.valueType = valueType;
        this.rawValue = rawValue;
        if (HashVariables.containsHashVariables(rawValue)) {
            this.needsInitialize = true;
        } else {
            try {
                this.value = ValueParser.getObject(rawValue, valueType, null);
            } catch (Exception e) {
                Lucky.error(e, "Error processing value: " + this.rawValue);
            }
        }
    }

    public boolean initialize(DropProcessData processData) {
        if (this.needsInitialize) {
            try {
                this.value = ValueParser.getObject(this.rawValue, this.valueType, processData);
                return true;
            } catch (Exception e) {
                Lucky.error(e, "Error processing value: " + this.rawValue);
            }
        }
        return false;
    }

    public boolean needsInitialize() {
        return this.needsInitialize;
    }

    public String getRawValue() {
        return this.rawValue;
    }

    public Object getValue() {
        return this.value;
    }

    public int getValueInt() {
        return (Integer) this.value;
    }

    public String getValueString() {
        return (String) this.value;
    }

    public boolean getValueBoolean() {
        return (Boolean) this.value;
    }

    public float getValueFloat() {
        return (Float) this.value;
    }

    public CompoundNBT getValueNBT() {
        return (CompoundNBT) this.value;
    }

    public Class getValueType() {
        return this.valueType;
    }

    public void setValue(Object object) {
        this.value = object;
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT mainTag = new CompoundNBT();
        if (this.value != null) mainTag.put("value", ValueParser.getNBTBaseFromValue(this.value));
        if (this.rawValue != null) mainTag.putString("rawValue", this.rawValue);
        if (this.valueType != null)
            mainTag.putString("valueType", ValueParser.classTypeToString.inverse().get(this.valueType));
        mainTag.putBoolean("needsInitialize", this.needsInitialize);
        return mainTag;
    }

    public void readFromNBT(CompoundNBT tagCompound) {
        if (tagCompound.contains("value"))
            this.value = ValueParser.getValueFromNBTBase(tagCompound.get("value"));
        if (tagCompound.contains("rawValue")) this.rawValue = tagCompound.getString("rawValue");
        if (tagCompound.contains("valueType"))
            this.valueType = ValueParser.classTypeToString.get(tagCompound.getString("valueType"));
        this.needsInitialize = tagCompound.getBoolean("needsInitialize");
    }

    public DropValue copy() {
        DropValue dropValue = new DropValue(this.value);
        dropValue.rawValue = this.rawValue;
        dropValue.valueType = this.valueType;
        dropValue.needsInitialize = this.needsInitialize;
        return dropValue;
    }

    @Override
    public String toString() {
        if (this.value != null) return this.value.toString();
        if (this.rawValue != null) return this.rawValue;
        return "error";
    }
}
