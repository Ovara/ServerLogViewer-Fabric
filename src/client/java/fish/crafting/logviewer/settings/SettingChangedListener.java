package fish.crafting.logviewer.settings;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class SettingChangedListener<T> {

    private SettingChangedListener(){

    }

    public static <T> SettingChangedListener<T> onAppliedIndividual(Consumer<T> value){
        return new OnApplyIndividual<>() {
            @Override
            public void onChanged(T newValue) {
                value.accept(newValue);
            }
        };
    }
    public static <T> SettingChangedListener<T> onAppliedBatch(Runnable task){
        return new OnApplyBatch<>() {
            @Override
            public void onChanged() {
                task.run();
            }
        };
    }


    public static <T> SettingChangedListener<T> instant(Consumer<T> value){
        return new Immediate<>() {

            @Override
            public void onChanged(T newValue) {
                value.accept(newValue);
            }
        };
    }

    public abstract static class OnApplyIndividual<T> extends SettingChangedListener<T> {
        public abstract void onChanged(T newValue);
    }

    public abstract static class Immediate<T> extends SettingChangedListener<T> {
        public abstract void onChanged(T newValue);
    }

    public abstract static class OnApplyBatch<T> extends SettingChangedListener<T>{
        public abstract void onChanged();
    }

}
